package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 报表控制器
 *
 * @author Warehouse Team
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    /**
     * 获取概览统计
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Map<String, Object>> getOverviewStats(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long warehouseId) {

        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            Map<String, Object> stats = reportsService.getOverviewStats(start, end, warehouseId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            // 如果解析日期失败或查询失败，返回默认值
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalInbound", 0);
            stats.put("totalOutbound", 0);
            stats.put("totalTransfer", 0);
            stats.put("totalStocktake", 0);
            stats.put("inboundAmount", 0.0);
            stats.put("outboundAmount", 0.0);
            stats.put("profitMargin", 0.0);
            stats.put("turnoverRate", 0.0);
            return ApiResponse.success(stats);
        }
    }

    /**
     * 获取趋势数据
     */
    @GetMapping("/trend")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getTrendData(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String type,
            @RequestParam(required = false) Long warehouseId) {

        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            List<Map<String, Object>> trendData = reportsService.getTrendData(start, end, type, warehouseId);
            return ApiResponse.success(trendData);
        } catch (Exception e) {
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 获取分布数据
     */
    @GetMapping("/distribution")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getDistributionData(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String type,
            @RequestParam(required = false) Long warehouseId) {

        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            List<Map<String, Object>> distributionData = reportsService.getDistributionData(start, end, type, warehouseId);
            return ApiResponse.success(distributionData);
        } catch (Exception e) {
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 获取周转数据
     */
    @GetMapping("/turnover")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getTurnoverData(
            @RequestParam int period,
            @RequestParam(required = false) Long warehouseId) {
        
        try {
            List<Map<String, Object>> turnoverData = reportsService.getTurnoverData(period, warehouseId);
            return ApiResponse.success(turnoverData);
        } catch (Exception e) {
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 获取热力图数据
     */
    @GetMapping("/heatmap")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getHeatmapData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long warehouseId) {
        
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now();

            List<Map<String, Object>> heatmapData = reportsService.getHeatmapData(start, end, warehouseId);
            return ApiResponse.success(heatmapData);
        } catch (Exception e) {
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 获取利用率数据
     */
    @GetMapping("/utilization")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getUtilizationData(
            @RequestParam(required = false) Long warehouseId) {
        
        try {
            List<Map<String, Object>> utilizationData = reportsService.getUtilizationData(warehouseId);
            return ApiResponse.success(utilizationData);
        } catch (Exception e) {
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 获取业务汇总
     */
    @GetMapping("/business-summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getBusinessSummary(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long warehouseId) {
        
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            List<Map<String, Object>> businessSummary = reportsService.getBusinessSummary(start, end, warehouseId);
            return ApiResponse.success(businessSummary);
        } catch (Exception e) {
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 获取库存分析
     */
    @GetMapping("/inventory-analysis")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getInventoryAnalysis(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long warehouseId) {
        
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            List<Map<String, Object>> inventoryAnalysis = reportsService.getInventoryAnalysis(start, end, warehouseId);
            return ApiResponse.success(inventoryAnalysis);
        } catch (Exception e) {
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 获取货物排行
     */
    @GetMapping("/goods-ranking")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getGoodsRanking(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String type,
            @RequestParam(required = false) Long warehouseId) {
        
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            List<Map<String, Object>> goodsRanking = reportsService.getGoodsRanking(start, end, type, warehouseId);
            return ApiResponse.success(goodsRanking);
        } catch (Exception e) {
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 获取异常分析
     */
    @GetMapping("/exception-analysis")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getExceptionAnalysis(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long warehouseId) {
        
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            List<Map<String, Object>> exceptionAnalysis = reportsService.getExceptionAnalysis(start, end, warehouseId);
            return ApiResponse.success(exceptionAnalysis);
        } catch (Exception e) {
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 导出报表
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> exportReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long warehouseId) {
        
        // 这里应该实现实际的导出逻辑
        return ApiResponse.success("报表导出功能待实现");
    }
}
