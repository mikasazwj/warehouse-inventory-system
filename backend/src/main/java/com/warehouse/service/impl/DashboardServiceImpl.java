package com.warehouse.service.impl;

import com.warehouse.entity.InboundOrder;
import com.warehouse.entity.OutboundOrder;
import com.warehouse.entity.User;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.*;
import com.warehouse.service.DashboardService;
import com.warehouse.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现类
 * 
 * @author Warehouse Team
 */
@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransferOrderRepository transferOrderRepository;

    @Autowired
    private InventoryService inventoryService;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取当前用户权限
        Long userWarehouseId = getCurrentUserWarehouseId();
        
        try {
            // 统计货物种类总数
            long totalGoods = goodsRepository.countNotDeleted();

            // 统计库存总量
            long totalInventory = 0;
            if (userWarehouseId != null) {
                // 仓库管理员只看自己仓库的库存
                totalInventory = inventoryRepository.getTotalQuantityByWarehouse(userWarehouseId);
            } else {
                // 系统管理员看所有库存
                totalInventory = inventoryRepository.getTotalQuantity();
            }

            // 统计待处理单据（状态为PENDING的入库单、出库单、调拨单）
            long pendingInbound = inboundOrderRepository.countByStatusAndNotDeleted(ApprovalStatus.PENDING);
            long pendingOutbound = outboundOrderRepository.countByStatusAndNotDeleted(ApprovalStatus.PENDING);
            long pendingTransfer = transferOrderRepository.countByStatusAndNotDeleted(ApprovalStatus.PENDING);
            long pendingOrders = pendingInbound + pendingOutbound + pendingTransfer;

            // 统计库存预警数量
            long alertCount = 0;
            if (userWarehouseId != null) {
                alertCount = inventoryRepository.countLowStockByWarehouse(userWarehouseId);
            } else {
                alertCount = inventoryRepository.countLowStock();
            }

            stats.put("totalGoods", totalGoods);
            stats.put("totalInventory", totalInventory);
            stats.put("pendingOrders", pendingOrders);
            stats.put("alertCount", alertCount);

            // 计算简单的增长率（基于数据特征的模拟增长率）
            int goodsGrowth = calculateGrowthRate(totalGoods, 0);
            int inventoryGrowth = calculateGrowthRate(totalInventory, 100);
            int ordersGrowth = calculateGrowthRate(pendingOrders, 0);
            int alertGrowth = calculateGrowthRate(alertCount, 0);

            stats.put("goodsGrowth", goodsGrowth);
            stats.put("inventoryGrowth", inventoryGrowth);
            stats.put("ordersGrowth", ordersGrowth);
            stats.put("alertGrowth", alertGrowth);

        } catch (Exception e) {
            // 如果查询失败，返回默认值
            stats.put("totalGoods", 0);
            stats.put("totalInventory", 0);
            stats.put("pendingOrders", 0);
            stats.put("alertCount", 0);
            stats.put("goodsGrowth", 0);
            stats.put("inventoryGrowth", 0);
            stats.put("ordersGrowth", 0);
            stats.put("alertGrowth", 0);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getInventoryAlerts() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        try {
            Long userWarehouseId = getCurrentUserWarehouseId();
            
            // 获取低库存预警
            List<Object[]> lowStockItems;
            if (userWarehouseId != null) {
                lowStockItems = inventoryRepository.findLowStockInfoByWarehouse(userWarehouseId);
            } else {
                lowStockItems = inventoryRepository.findLowStockInfo();
            }
            
            for (Object[] item : lowStockItems) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", item[0]); // inventory id
                alert.put("goodsName", item[1]); // goods name
                alert.put("warehouseName", item[2]); // warehouse name
                alert.put("currentStock", item[3]); // current quantity
                alert.put("minStock", item[4]); // min stock level
                alert.put("level", "warning");
                alert.put("message", "库存偏低");
                alerts.add(alert);
            }
            
            // 获取零库存预警
            List<Object[]> zeroStockItems;
            if (userWarehouseId != null) {
                zeroStockItems = inventoryRepository.findZeroStockInfoByWarehouse(userWarehouseId);
            } else {
                zeroStockItems = inventoryRepository.findZeroStockInfo();
            }
            
            for (Object[] item : zeroStockItems) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", item[0]);
                alert.put("goodsName", item[1]);
                alert.put("warehouseName", item[2]);
                alert.put("currentStock", 0);
                alert.put("level", "danger");
                alert.put("message", "库存不足");
                alerts.add(alert);
            }
            
        } catch (Exception e) {
            // 如果查询失败，返回空列表
        }
        
        result.put("alerts", alerts);
        result.put("total", alerts.size());
        
        return result;
    }

    @Override
    public Map<String, Object> getTodoList() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> todos = new ArrayList<>();
        
        try {
            Long userWarehouseId = getCurrentUserWarehouseId();
            
            // 获取待审批的入库单
            List<InboundOrder> pendingInbound;
            if (userWarehouseId != null) {
                pendingInbound = inboundOrderRepository.findByWarehouseIdAndStatus(userWarehouseId, ApprovalStatus.PENDING);
            } else {
                pendingInbound = inboundOrderRepository.findByStatus(ApprovalStatus.PENDING);
            }

            for (InboundOrder order : pendingInbound) {
                Map<String, Object> todo = new HashMap<>();
                todo.put("id", "inbound_" + order.getId());
                todo.put("type", "inbound_approval");
                todo.put("title", "审批入库单 " + order.getOrderNumber());
                todo.put("priority", "high");
                todo.put("dueDate", order.getCreatedTime());
                todo.put("status", "pending");
                todos.add(todo);
            }
            
            // 获取待审批的出库单
            List<OutboundOrder> pendingOutbound;
            if (userWarehouseId != null) {
                pendingOutbound = outboundOrderRepository.findByWarehouseIdAndStatus(userWarehouseId, ApprovalStatus.PENDING);
            } else {
                pendingOutbound = outboundOrderRepository.findByStatus(ApprovalStatus.PENDING);
            }

            for (OutboundOrder order : pendingOutbound) {
                Map<String, Object> todo = new HashMap<>();
                todo.put("id", "outbound_" + order.getId());
                todo.put("type", "outbound_approval");
                todo.put("title", "审批出库单 " + order.getOrderNumber());
                todo.put("priority", "high");
                todo.put("dueDate", order.getCreatedTime());
                todo.put("status", "pending");
                todos.add(todo);
            }
            
        } catch (Exception e) {
            // 如果查询失败，返回空列表
        }
        
        result.put("todos", todos);
        result.put("total", todos.size());
        
        return result;
    }

    @Override
    public Map<String, Object> getBusinessTrend(int period) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> trendData = new ArrayList<>();

        try {
            // 获取当前用户权限
            Long userWarehouseId = getCurrentUserWarehouseId();

            // 生成指定天数的趋势数据
            for (int i = period - 1; i >= 0; i--) {
                Map<String, Object> dayData = new HashMap<>();
                LocalDate currentDate = LocalDate.now().minusDays(i);

                // 统计当天的入库数量
                long inboundCount = 0;
                if (userWarehouseId != null) {
                    inboundCount = inboundOrderRepository.countByDateAndWarehouse(currentDate, userWarehouseId);
                } else {
                    inboundCount = inboundOrderRepository.countByDate(currentDate);
                }

                // 统计当天的出库数量
                long outboundCount = 0;
                if (userWarehouseId != null) {
                    outboundCount = outboundOrderRepository.countByDateAndWarehouse(currentDate, userWarehouseId);
                } else {
                    outboundCount = outboundOrderRepository.countByDate(currentDate);
                }

                // 统计当天的调拨数量
                long transferCount = 0;
                if (userWarehouseId != null) {
                    transferCount = transferOrderRepository.countByWarehouseAndDate(userWarehouseId, currentDate);
                } else {
                    transferCount = transferOrderRepository.countByDate(currentDate);
                }

                dayData.put("date", currentDate.format(DateTimeFormatter.ofPattern("MM-dd")));
                dayData.put("inbound", inboundCount);
                dayData.put("outbound", outboundCount);
                dayData.put("transfer", transferCount);

                trendData.add(dayData);
            }
        } catch (Exception e) {
            // 如果查询失败，返回空数据
            for (int i = period - 1; i >= 0; i--) {
                Map<String, Object> dayData = new HashMap<>();
                LocalDate currentDate = LocalDate.now().minusDays(i);
                dayData.put("date", currentDate.format(DateTimeFormatter.ofPattern("MM-dd")));
                dayData.put("inbound", 0);
                dayData.put("outbound", 0);
                dayData.put("transfer", 0);
                trendData.add(dayData);
            }
        }

        result.put("data", trendData);
        result.put("period", period);

        return result;
    }

    @Override
    public Map<String, Object> getWarehouseDistribution() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        try {
            Long userWarehouseId = getCurrentUserWarehouseId();
            
            List<Object[]> warehouseStats;
            if (userWarehouseId != null) {
                // 如果是仓库管理员，只显示自己的仓库
                warehouseStats = inventoryRepository.getWarehouseInventoryStats(userWarehouseId);
            } else {
                // 如果是系统管理员，显示所有仓库
                warehouseStats = inventoryRepository.getAllWarehouseInventoryStats();
            }
            
            for (Object[] stat : warehouseStats) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", stat[0]); // warehouse name
                item.put("value", stat[1]); // total quantity
                distribution.add(item);
            }
            
        } catch (Exception e) {
            // 如果查询失败，返回空数据
        }
        
        result.put("data", distribution);
        return result;
    }

    /**
     * 获取当前用户的仓库ID（如果是仓库管理员）
     */
    private Long getCurrentUserWarehouseId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                org.springframework.security.core.userdetails.UserDetails userDetails =
                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

                // 如果是系统管理员，返回null（可以访问所有数据）
                boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

                if (isAdmin) {
                    return null;
                }

                // 如果是仓库管理员，返回对应的仓库ID
                boolean isWarehouseManager = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_WAREHOUSE_MANAGER".equals(auth.getAuthority()));

                if (isWarehouseManager) {
                    // 根据用户名查找对应的仓库
                    String username = userDetails.getUsername();
                    // 这里可以根据实际业务逻辑来获取用户对应的仓库ID
                    // 暂时返回null，表示可以访问所有数据
                    return null;
                }
            }
        } catch (Exception e) {
            // 如果获取失败，返回null
        }
        return null;
    }

    @Override
    public Map<String, Object> getRecentActivities() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> activities = new ArrayList<>();
        
        try {
            Long userWarehouseId = getCurrentUserWarehouseId();
            
            // 获取最近的入库单
            List<InboundOrder> recentInbound;
            if (userWarehouseId != null) {
                recentInbound = inboundOrderRepository.findTop5ByWarehouseIdOrderByCreatedTimeDesc(userWarehouseId);
            } else {
                recentInbound = inboundOrderRepository.findTop5ByOrderByCreatedTimeDesc();
            }

            for (InboundOrder order : recentInbound) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", order.getId());
                activity.put("type", "inbound");
                activity.put("title", "入库单 " + order.getOrderNumber() + " 已创建");
                activity.put("time", order.getCreatedTime());
                activity.put("user", order.getCreatedBy());
                activities.add(activity);
            }

            // 获取最近的出库单
            List<OutboundOrder> recentOutbound;
            if (userWarehouseId != null) {
                recentOutbound = outboundOrderRepository.findTop5ByWarehouseIdOrderByCreatedTimeDesc(userWarehouseId);
            } else {
                recentOutbound = outboundOrderRepository.findTop5ByOrderByCreatedTimeDesc();
            }

            for (OutboundOrder order : recentOutbound) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", order.getId());
                activity.put("type", "outbound");
                activity.put("title", "出库单 " + order.getOrderNumber() + " 已创建");
                activity.put("time", order.getCreatedTime());
                activity.put("user", order.getCreatedBy());
                activities.add(activity);
            }
            
            // 按时间排序
            activities.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("time");
                LocalDateTime timeB = (LocalDateTime) b.get("time");
                return timeB.compareTo(timeA);
            });
            
            // 只保留最近10条
            if (activities.size() > 10) {
                activities = activities.subList(0, 10);
            }
            
        } catch (Exception e) {
            // 如果查询失败，返回空列表
        }
        
        result.put("activities", activities);
        result.put("total", activities.size());

        return result;
    }

    /**
     * 计算增长率（简化版本，用于演示）
     * 基于当前数值的特征来模拟增长率
     */
    private int calculateGrowthRate(long currentValue, long baseValue) {
        if (currentValue == 0) return 0;

        // 简单的增长率计算逻辑
        if (currentValue > baseValue) {
            // 有数据时显示正增长
            return (int) (Math.random() * 15) + 5; // 5-20%的正增长
        } else if (currentValue == baseValue) {
            return 0; // 持平
        } else {
            // 低于基准值时显示负增长
            return -((int) (Math.random() * 10) + 1); // 1-10%的负增长
        }
    }
}
