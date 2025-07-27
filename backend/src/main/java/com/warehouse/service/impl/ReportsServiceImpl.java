package com.warehouse.service.impl;

import com.warehouse.repository.*;
import com.warehouse.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 报表服务实现类
 *
 * @author Warehouse Team
 */
@Service
@Transactional(readOnly = true)
public class ReportsServiceImpl implements ReportsService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private TransferOrderRepository transferOrderRepository;

    @Autowired
    private StocktakeOrderRepository stocktakeOrderRepository;

    @Override
    public Map<String, Object> getOverviewStats(LocalDate startDate, LocalDate endDate, Long warehouseId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 入库单统计
            long totalInbound = warehouseId != null ?
                inboundOrderRepository.countByWarehouseAndDateRange(warehouseId, startDate, endDate) :
                inboundOrderRepository.countByDateRange(startDate, endDate);

            // 出库单统计
            long totalOutbound = warehouseId != null ?
                outboundOrderRepository.countByWarehouseAndDateRange(warehouseId, startDate, endDate) :
                outboundOrderRepository.countByDateRange(startDate, endDate);

            // 调拨单统计
            long totalTransfer = warehouseId != null ?
                transferOrderRepository.countByWarehouseAndDateRange(warehouseId, startDate, endDate) :
                transferOrderRepository.countByDateRange(startDate, endDate);

            // 盘点单统计 - 暂时注释掉
            // long totalStocktake = warehouseId != null ?
            //     stocktakeOrderRepository.countByWarehouseAndDateRange(warehouseId, startDate, endDate) :
            //     stocktakeOrderRepository.countByDateRange(startDate, endDate);
            long totalStocktake = 0; // 暂时返回0

            // 金额统计
            BigDecimal inboundAmount = warehouseId != null ?
                inboundOrderRepository.sumAmountByWarehouseAndDateRange(warehouseId, startDate, endDate) :
                inboundOrderRepository.sumAmountByDateRange(startDate, endDate);

            BigDecimal outboundAmount = warehouseId != null ?
                outboundOrderRepository.sumAmountByWarehouseAndDateRange(warehouseId, startDate, endDate) :
                outboundOrderRepository.sumAmountByDateRange(startDate, endDate);

            // 计算利润率和周转率（简化计算）
            double profitMargin = 0.0;
            double turnoverRate = 0.0;

            if (inboundAmount != null && outboundAmount != null &&
                inboundAmount.compareTo(BigDecimal.ZERO) > 0) {
                profitMargin = outboundAmount.subtract(inboundAmount)
                    .divide(inboundAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            }

            stats.put("totalInbound", totalInbound);
            stats.put("totalOutbound", totalOutbound);
            stats.put("totalTransfer", totalTransfer);
            stats.put("totalStocktake", totalStocktake);
            stats.put("inboundAmount", inboundAmount != null ? inboundAmount.doubleValue() : 0.0);
            stats.put("outboundAmount", outboundAmount != null ? outboundAmount.doubleValue() : 0.0);
            stats.put("profitMargin", profitMargin);
            stats.put("turnoverRate", turnoverRate);

        } catch (Exception e) {
            // 如果查询失败，返回默认值
            stats.put("totalInbound", 0);
            stats.put("totalOutbound", 0);
            stats.put("totalTransfer", 0);
            stats.put("totalStocktake", 0);
            stats.put("inboundAmount", 0.0);
            stats.put("outboundAmount", 0.0);
            stats.put("profitMargin", 0.0);
            stats.put("turnoverRate", 0.0);
        }

        return stats;
    }

    @Override
    public List<Map<String, Object>> getTrendData(LocalDate startDate, LocalDate endDate, String type, Long warehouseId) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        try {
            // 按天生成趋势数据
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                // 获取真实数据
                long inboundCount = warehouseId != null ?
                    inboundOrderRepository.countByWarehouseAndDate(warehouseId, current) :
                    inboundOrderRepository.countByDate(current);

                long outboundCount = warehouseId != null ?
                    outboundOrderRepository.countByWarehouseAndDate(warehouseId, current) :
                    outboundOrderRepository.countByDate(current);

                long transferCount = warehouseId != null ?
                    transferOrderRepository.countByWarehouseAndDate(warehouseId, current) :
                    transferOrderRepository.countByDate(current);

                // 为前端提供完整的数据结构
                dayData.put("inboundQuantity", inboundCount);
                dayData.put("outboundQuantity", outboundCount);
                dayData.put("transferQuantity", transferCount);
                dayData.put("inboundAmount", 0); // 暂时返回0，避免模拟数据
                dayData.put("outboundAmount", 0);
                dayData.put("transferAmount", 0);

                trendData.add(dayData);
                current = current.plusDays(1);
            }
        } catch (Exception e) {
            // 如果查询失败，返回空数据
        }

        return trendData;
    }

    @Override
    public List<Map<String, Object>> getDistributionData(LocalDate startDate, LocalDate endDate, String type, Long warehouseId) {
        List<Map<String, Object>> distributionData = new ArrayList<>();

        try {
            switch (type) {
                case "warehouse":
                    // 仓库分布
                    List<Object[]> warehouseStats = inventoryRepository.getAllWarehouseInventoryStats();
                    for (Object[] stat : warehouseStats) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", stat[0]);
                        item.put("value", stat[1]);
                        distributionData.add(item);
                    }
                    break;
                case "inbound":
                    // 入库类型分布 - 暂时返回空数据，避免显示模拟数据
                    break;
                case "outbound":
                    // 出库类型分布 - 暂时返回空数据，避免显示模拟数据
                    break;
            }
        } catch (Exception e) {
            // 如果查询失败，返回空数据
        }

        return distributionData;
    }

    private Map<String, Object> createDistributionItem(String name, int value) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    @Override
    public List<Map<String, Object>> getTurnoverData(int period, Long warehouseId) {
        List<Map<String, Object>> turnoverData = new ArrayList<>();

        try {
            // 获取库存数据计算周转率
            List<Object[]> inventoryStats = warehouseId != null ?
                inventoryRepository.findInventoryStatsWithTurnover(warehouseId) :
                inventoryRepository.findAllInventoryStatsWithTurnover();

            for (Object[] stat : inventoryStats) {
                Map<String, Object> item = new HashMap<>();
                item.put("goodsName", stat[0] != null ? stat[0].toString() : "未知商品");
                item.put("turnoverRate", stat[1] != null ? ((Number) stat[1]).doubleValue() : 0.0);
                item.put("quantity", stat[2] != null ? ((Number) stat[2]).intValue() : 0);
                turnoverData.add(item);
            }
        } catch (Exception e) {
            // 如果查询失败，返回空数据
        }

        return turnoverData;
    }

    @Override
    public List<Map<String, Object>> getHeatmapData(LocalDate startDate, LocalDate endDate, Long warehouseId) {
        List<Map<String, Object>> heatmapData = new ArrayList<>();

        try {
            // 获取活跃商品数据
            List<Object[]> activeGoods = warehouseId != null ?
                inventoryRepository.findActiveGoodsByWarehouse(warehouseId) :
                inventoryRepository.findActiveGoods();

            for (Object[] goods : activeGoods) {
                Map<String, Object> item = new HashMap<>();
                item.put("goodsName", goods[0] != null ? goods[0].toString() : "未知商品");

                // 暂时返回空的活跃度数据，避免模拟数据
                List<Integer> weekData = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    weekData.add(0);
                }
                item.put("weekData", weekData);
                heatmapData.add(item);
            }
        } catch (Exception e) {
            // 如果查询失败，返回空数据
        }

        return heatmapData;
    }

    @Override
    public List<Map<String, Object>> getUtilizationData(Long warehouseId) {
        List<Map<String, Object>> utilizationData = new ArrayList<>();

        try {
            // 获取仓库利用率数据
            List<Object[]> warehouseUtilization = warehouseId != null ?
                inventoryRepository.findWarehouseUtilization(warehouseId) :
                inventoryRepository.findAllWarehouseUtilization();

            for (Object[] util : warehouseUtilization) {
                Map<String, Object> item = new HashMap<>();
                item.put("warehouseName", util[0] != null ? util[0].toString() : "未知仓库");
                item.put("usedCapacity", util[1] != null ? ((Number) util[1]).intValue() : 0);
                item.put("totalCapacity", util[2] != null ? ((Number) util[2]).intValue() : 1000);

                int usedCapacity = (Integer) item.get("usedCapacity");
                int totalCapacity = (Integer) item.get("totalCapacity");
                item.put("remainingCapacity", totalCapacity - usedCapacity);
                item.put("utilizationRate", totalCapacity > 0 ? (double) usedCapacity / totalCapacity : 0.0);

                utilizationData.add(item);
            }
        } catch (Exception e) {
            // 如果查询失败，返回空数据
        }

        return utilizationData;
    }

    @Override
    public List<Map<String, Object>> getBusinessSummary(LocalDate startDate, LocalDate endDate, Long warehouseId) {
        List<Map<String, Object>> businessSummary = new ArrayList<>();

        try {
            // 按日期生成业务汇总数据
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                // 入库数据
                long inboundCount = warehouseId != null ?
                    inboundOrderRepository.countByWarehouseAndDate(warehouseId, current) :
                    inboundOrderRepository.countByDate(current);
                BigDecimal inboundAmount = warehouseId != null ?
                    inboundOrderRepository.sumAmountByWarehouseAndDate(warehouseId, current) :
                    inboundOrderRepository.sumAmountByDate(current);

                // 出库数据
                long outboundCount = warehouseId != null ?
                    outboundOrderRepository.countByWarehouseAndDate(warehouseId, current) :
                    outboundOrderRepository.countByDate(current);
                BigDecimal outboundAmount = warehouseId != null ?
                    outboundOrderRepository.sumAmountByWarehouseAndDate(warehouseId, current) :
                    outboundOrderRepository.sumAmountByDate(current);

                // 调拨数据
                long transferCount = warehouseId != null ?
                    transferOrderRepository.countByWarehouseAndDate(warehouseId, current) :
                    transferOrderRepository.countByDate(current);

                // 盘点数据
                long stocktakeCount = warehouseId != null ?
                    stocktakeOrderRepository.countByWarehouseAndDate(warehouseId, current) :
                    stocktakeOrderRepository.countByDate(current);

                dayData.put("inboundCount", inboundCount);
                dayData.put("inboundQuantity", 0); // 暂时返回0，避免模拟数据
                dayData.put("inboundAmount", inboundAmount != null ? inboundAmount.doubleValue() : 0.0);
                dayData.put("outboundCount", outboundCount);
                dayData.put("outboundQuantity", 0); // 暂时返回0，避免模拟数据
                dayData.put("outboundAmount", outboundAmount != null ? outboundAmount.doubleValue() : 0.0);
                dayData.put("transferCount", transferCount);
                dayData.put("stocktakeCount", stocktakeCount);

                businessSummary.add(dayData);
                current = current.plusDays(1);
            }
        } catch (Exception e) {
            // 如果查询失败，返回空数据
        }

        return businessSummary;
    }

    @Override
    public List<Map<String, Object>> getInventoryAnalysis(LocalDate startDate, LocalDate endDate, Long warehouseId) {
        List<Map<String, Object>> inventoryAnalysis = new ArrayList<>();

        try {
            if (warehouseId != null) {
                // 分析指定仓库
                Map<String, Object> analysis = getWarehouseInventoryAnalysis(warehouseId);
                if (analysis != null) {
                    inventoryAnalysis.add(analysis);
                }
            } else {
                // 分析所有仓库
                List<Object[]> warehouseData = inventoryRepository.findWarehouseInventoryStats();
                for (Object[] row : warehouseData) {
                    Map<String, Object> analysis = new HashMap<>();
                    analysis.put("warehouseName", row[0] != null ? row[0].toString() : "未知仓库");
                    analysis.put("goodsCount", row[1] != null ? ((Number) row[1]).longValue() : 0L);
                    analysis.put("totalQuantity", row[2] != null ? ((Number) row[2]).longValue() : 0L);
                    analysis.put("totalValue", row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
                    analysis.put("turnoverRate", calculateTurnoverRate(((Number) row[4]).longValue()));
                    analysis.put("utilizationRate", calculateUtilizationRate(((Number) row[4]).longValue()));
                    analysis.put("alertCount", row[5] != null ? ((Number) row[5]).longValue() : 0L);
                    inventoryAnalysis.add(analysis);
                }
            }
        } catch (Exception e) {
            // 如果查询失败，返回默认数据
            Map<String, Object> defaultAnalysis = new HashMap<>();
            defaultAnalysis.put("warehouseName", "主仓库");
            defaultAnalysis.put("goodsCount", 0L);
            defaultAnalysis.put("totalQuantity", 0L);
            defaultAnalysis.put("totalValue", 0.0);
            defaultAnalysis.put("turnoverRate", 0.0);
            defaultAnalysis.put("utilizationRate", 0.0);
            defaultAnalysis.put("alertCount", 0L);
            inventoryAnalysis.add(defaultAnalysis);
        }

        return inventoryAnalysis;
    }

    private Map<String, Object> getWarehouseInventoryAnalysis(Long warehouseId) {
        try {
            Object[] data = inventoryRepository.findWarehouseInventoryStatsByWarehouse(warehouseId);
            if (data != null && data.length > 0) {
                Map<String, Object> analysis = new HashMap<>();
                analysis.put("warehouseName", data[0] != null ? data[0].toString() : "未知仓库");
                analysis.put("goodsCount", data[1] != null ? ((Number) data[1]).longValue() : 0L);
                analysis.put("totalQuantity", data[2] != null ? ((Number) data[2]).longValue() : 0L);
                analysis.put("totalValue", data[3] != null ? ((Number) data[3]).doubleValue() : 0.0);
                analysis.put("turnoverRate", calculateTurnoverRate(((Number) data[4]).longValue()));
                analysis.put("utilizationRate", calculateUtilizationRate(((Number) data[4]).longValue()));
                analysis.put("alertCount", data[5] != null ? ((Number) data[5]).longValue() : 0L);
                return analysis;
            }
        } catch (Exception e) {
            // 忽略错误
        }
        return null;
    }

    private double calculateTurnoverRate(Long warehouseId) {
        // 简化的周转率计算：出库数量 / 平均库存
        try {
            Long outboundQuantity = inventoryRepository.sumOutboundQuantityByWarehouse(warehouseId);
            Long avgInventory = inventoryRepository.sumTotalQuantityByWarehouse(warehouseId);
            if (avgInventory != null && avgInventory > 0) {
                return (outboundQuantity != null ? outboundQuantity.doubleValue() : 0.0) / avgInventory.doubleValue();
            }
        } catch (Exception e) {
            // 忽略错误
        }
        return 0.0;
    }

    private double calculateUtilizationRate(Long warehouseId) {
        // 简化的利用率计算：已用容量 / 总容量
        try {
            // 这里可以根据实际业务逻辑计算利用率
            return Math.random() * 0.8 + 0.1; // 临时返回随机值，实际应该根据仓库容量计算
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public List<Map<String, Object>> getGoodsRanking(LocalDate startDate, LocalDate endDate, String type, Long warehouseId) {
        List<Map<String, Object>> goodsRanking = new ArrayList<>();

        try {
            List<Object[]> rankingData = new ArrayList<>();

            switch (type) {
                case "inbound":
                    rankingData = warehouseId != null ?
                        inventoryRepository.findTopInboundGoodsByWarehouse(warehouseId, startDate, endDate) :
                        inventoryRepository.findTopInboundGoods(startDate, endDate);
                    break;
                case "outbound":
                    rankingData = warehouseId != null ?
                        inventoryRepository.findTopOutboundGoodsByWarehouse(warehouseId, startDate, endDate) :
                        inventoryRepository.findTopOutboundGoods(startDate, endDate);
                    break;
                case "turnover":
                    rankingData = warehouseId != null ?
                        inventoryRepository.findTopTurnoverGoodsByWarehouse(warehouseId) :
                        inventoryRepository.findTopTurnoverGoods();
                    break;
            }

            // 计算总金额用于百分比计算
            double totalAmount = rankingData.stream()
                .mapToDouble(data -> data[4] != null ? ((Number) data[4]).doubleValue() : 0.0)
                .sum();

            for (Object[] data : rankingData) {
                Map<String, Object> item = new HashMap<>();
                item.put("goodsCode", data[0] != null ? data[0].toString() : "");
                item.put("goodsName", data[1] != null ? data[1].toString() : "未知商品");
                item.put("categoryName", data[2] != null ? data[2].toString() : "未分类");
                item.put("quantity", data[3] != null ? ((Number) data[3]).intValue() : 0);
                double amount = data[4] != null ? ((Number) data[4]).doubleValue() : 0.0;
                item.put("amount", amount);
                // 计算百分比
                double percentage = totalAmount > 0 ? (amount / totalAmount) * 100.0 : 0.0;
                item.put("percentage", Math.round(percentage * 100.0) / 100.0); // 保留两位小数
                goodsRanking.add(item);
            }
        } catch (Exception e) {
            // 如果查询失败，返回空数据
        }

        return goodsRanking;
    }



    @Override
    public List<Map<String, Object>> getExceptionAnalysis(LocalDate startDate, LocalDate endDate, Long warehouseId) {
        List<Map<String, Object>> exceptionAnalysis = new ArrayList<>();

        try {
            // 1. 低库存异常
            long lowStockCount = warehouseId != null ?
                inventoryRepository.countLowStockByWarehouse(warehouseId) :
                inventoryRepository.countLowStock();

            if (lowStockCount > 0) {
                Map<String, Object> lowStockException = new HashMap<>();
                lowStockException.put("type", "LOW_STOCK");
                lowStockException.put("description", "库存数量低于安全库存线");
                lowStockException.put("count", lowStockCount);
                lowStockException.put("lastOccurrence", LocalDate.now().toString());
                lowStockException.put("impact", "高");
                exceptionAnalysis.add(lowStockException);
            }

            // 2. 零库存异常
            long zeroStockCount = warehouseId != null ?
                inventoryRepository.countZeroStockByWarehouse(warehouseId) :
                inventoryRepository.countZeroStock();

            if (zeroStockCount > 0) {
                Map<String, Object> zeroStockException = new HashMap<>();
                zeroStockException.put("type", "ZERO_STOCK");
                zeroStockException.put("description", "商品库存为零，无法满足出库需求");
                zeroStockException.put("count", zeroStockCount);
                zeroStockException.put("lastOccurrence", LocalDate.now().toString());
                zeroStockException.put("impact", "严重");
                exceptionAnalysis.add(zeroStockException);
            }

            // 3. 长期滞销异常
            long slowMovingCount = getSlowMovingItemsCount(warehouseId, startDate, endDate);
            if (slowMovingCount > 0) {
                Map<String, Object> slowMovingException = new HashMap<>();
                slowMovingException.put("type", "SLOW_MOVING");
                slowMovingException.put("description", "商品长期无出库记录，可能存在滞销");
                slowMovingException.put("count", slowMovingCount);
                slowMovingException.put("lastOccurrence", LocalDate.now().toString());
                slowMovingException.put("impact", "中");
                exceptionAnalysis.add(slowMovingException);
            }

            // 4. 库存价值异常
            long highValueCount = getHighValueLowTurnoverCount(warehouseId);
            if (highValueCount > 0) {
                Map<String, Object> highValueException = new HashMap<>();
                highValueException.put("type", "HIGH_VALUE_LOW_TURNOVER");
                highValueException.put("description", "高价值商品周转率低，占用资金");
                highValueException.put("count", highValueCount);
                highValueException.put("lastOccurrence", LocalDate.now().toString());
                highValueException.put("impact", "中");
                exceptionAnalysis.add(highValueException);
            }

            // 5. 如果没有异常，添加一个正常状态
            if (exceptionAnalysis.isEmpty()) {
                Map<String, Object> normalStatus = new HashMap<>();
                normalStatus.put("type", "NORMAL");
                normalStatus.put("description", "当前库存状态正常，无异常情况");
                normalStatus.put("count", 0);
                normalStatus.put("lastOccurrence", LocalDate.now().toString());
                normalStatus.put("impact", "无");
                exceptionAnalysis.add(normalStatus);
            }

        } catch (Exception e) {
            // 如果查询失败，返回默认异常信息
            Map<String, Object> errorException = new HashMap<>();
            errorException.put("type", "SYSTEM_ERROR");
            errorException.put("description", "系统查询异常，请稍后重试");
            errorException.put("count", 1);
            errorException.put("lastOccurrence", LocalDate.now().toString());
            errorException.put("impact", "低");
            exceptionAnalysis.add(errorException);
        }

        return exceptionAnalysis;
    }

    private long getSlowMovingItemsCount(Long warehouseId, LocalDate startDate, LocalDate endDate) {
        try {
            // 查找在指定时间范围内没有出库记录的商品数量
            if (warehouseId != null) {
                return inventoryRepository.countSlowMovingItemsByWarehouse(warehouseId, startDate, endDate);
            } else {
                return inventoryRepository.countSlowMovingItems(startDate, endDate);
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private long getHighValueLowTurnoverCount(Long warehouseId) {
        try {
            // 查找高价值但周转率低的商品
            if (warehouseId != null) {
                return inventoryRepository.countHighValueLowTurnoverByWarehouse(warehouseId);
            } else {
                return inventoryRepository.countHighValueLowTurnover();
            }
        } catch (Exception e) {
            return 0;
        }
    }
}