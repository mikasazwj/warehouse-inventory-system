package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 仪表板控制器
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表板统计数据
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = dashboardService.getDashboardStats();
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取趋势数据
     */
    @GetMapping("/trend")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Map<String, Object>> getTrendData(@RequestParam(defaultValue = "30") int period) {
        try {
            Map<String, Object> trend = dashboardService.getBusinessTrend(period);
            return ApiResponse.success(trend);
        } catch (Exception e) {
            return ApiResponse.error("获取趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取仓库分布数据
     */
    @GetMapping("/warehouse-distribution")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Map<String, Object>> getWarehouseDistribution() {
        try {
            Map<String, Object> distribution = dashboardService.getWarehouseDistribution();
            return ApiResponse.success(distribution);
        } catch (Exception e) {
            return ApiResponse.error("获取仓库分布数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取告警信息
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Map<String, Object>> getAlerts() {
        try {
            Map<String, Object> alerts = dashboardService.getInventoryAlerts();
            return ApiResponse.success(alerts);
        } catch (Exception e) {
            return ApiResponse.error("获取告警信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取待办事项
     */
    @GetMapping("/todos")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Map<String, Object>> getTodos() {
        try {
            Map<String, Object> todos = dashboardService.getTodoList();
            return ApiResponse.success(todos);
        } catch (Exception e) {
            return ApiResponse.error("获取待办事项失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近活动
     */
    @GetMapping("/recent-activities")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Map<String, Object>> getRecentActivities() {
        try {
            Map<String, Object> activities = dashboardService.getRecentActivities();
            return ApiResponse.success(activities);
        } catch (Exception e) {
            return ApiResponse.error("获取最近活动失败: " + e.getMessage());
        }
    }
}
