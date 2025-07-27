package com.warehouse.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 报表服务接口
 * 
 * @author Warehouse Team
 */
public interface ReportsService {

    /**
     * 获取概览统计
     */
    Map<String, Object> getOverviewStats(LocalDate startDate, LocalDate endDate, Long warehouseId);

    /**
     * 获取趋势数据
     */
    List<Map<String, Object>> getTrendData(LocalDate startDate, LocalDate endDate, String type, Long warehouseId);

    /**
     * 获取分布数据
     */
    List<Map<String, Object>> getDistributionData(LocalDate startDate, LocalDate endDate, String type, Long warehouseId);

    /**
     * 获取周转数据
     */
    List<Map<String, Object>> getTurnoverData(int period, Long warehouseId);

    /**
     * 获取热力图数据
     */
    List<Map<String, Object>> getHeatmapData(LocalDate startDate, LocalDate endDate, Long warehouseId);

    /**
     * 获取利用率数据
     */
    List<Map<String, Object>> getUtilizationData(Long warehouseId);

    /**
     * 获取业务汇总
     */
    List<Map<String, Object>> getBusinessSummary(LocalDate startDate, LocalDate endDate, Long warehouseId);

    /**
     * 获取库存分析
     */
    List<Map<String, Object>> getInventoryAnalysis(LocalDate startDate, LocalDate endDate, Long warehouseId);

    /**
     * 获取货物排行
     */
    List<Map<String, Object>> getGoodsRanking(LocalDate startDate, LocalDate endDate, String type, Long warehouseId);

    /**
     * 获取异常分析
     */
    List<Map<String, Object>> getExceptionAnalysis(LocalDate startDate, LocalDate endDate, Long warehouseId);
}
