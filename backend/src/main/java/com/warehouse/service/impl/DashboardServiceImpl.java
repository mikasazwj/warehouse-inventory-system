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
    private InventoryService inventoryService;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取当前用户权限
        Long userWarehouseId = getCurrentUserWarehouseId();
        
        try {
            // 暂时返回空数据，避免模拟数据的百分比显示
            stats.put("totalGoods", 0);
            stats.put("totalInventory", 0);
            stats.put("pendingOrders", 0);
            stats.put("alertCount", 0);

            // 移除百分比数据
            stats.put("goodsGrowth", 0);
            stats.put("inventoryGrowth", 0);
            stats.put("ordersGrowth", 0);
            stats.put("alertGrowth", 0);

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
            Long userWarehouseId = getCurrentUserWarehouseId();
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(period - 1);
            
            for (int i = 0; i < period; i++) {
                LocalDate currentDate = startDate.plusDays(i);
                Map<String, Object> dayData = new HashMap<>();
                
                // 获取当天的入库、出库、调拨数据
                long inboundCount;
                long outboundCount;
                
                if (userWarehouseId != null) {
                    inboundCount = inboundOrderRepository.countByDateAndWarehouse(currentDate, userWarehouseId);
                    outboundCount = outboundOrderRepository.countByDateAndWarehouse(currentDate, userWarehouseId);
                } else {
                    inboundCount = inboundOrderRepository.countByDate(currentDate);
                    outboundCount = outboundOrderRepository.countByDate(currentDate);
                }
                
                dayData.put("date", currentDate.format(DateTimeFormatter.ofPattern("MM-dd")));
                dayData.put("inbound", inboundCount);
                dayData.put("outbound", outboundCount);
                dayData.put("transfer", 0); // 暂时设为0，后续可以添加调拨数据
                
                trendData.add(dayData);
            }
            
        } catch (Exception e) {
            // 如果查询失败，返回空数据
            System.err.println("查询趋势数据失败: " + e.getMessage());
        }
        
        result.put("data", trendData);
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

                // 获取用户有权限的仓库
                User user = userRepository.findByUsernameAndDeletedFalse(userDetails.getUsername())
                    .orElse(null);

                if (user != null && !user.getWarehouses().isEmpty()) {
                    return user.getWarehouses().iterator().next().getId();
                }
            }
        } catch (Exception e) {
            // 忽略异常，返回null
        }
        return null;
    }
}
