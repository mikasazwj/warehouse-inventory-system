package com.warehouse.service;

import java.util.Map;

/**
 * 仪表盘服务接口
 * 
 * @author Warehouse Team
 */
public interface DashboardService {

    /**
     * 获取仪表盘统计数据
     */
    Map<String, Object> getDashboardStats();

    /**
     * 获取库存预警列表
     */
    Map<String, Object> getInventoryAlerts();

    /**
     * 获取待办事项列表
     */
    Map<String, Object> getTodoList();

    /**
     * 获取业务趋势数据
     */
    Map<String, Object> getBusinessTrend(int period);

    /**
     * 获取仓库分布数据
     */
    Map<String, Object> getWarehouseDistribution();

    /**
     * 获取最近活动
     */
    Map<String, Object> getRecentActivities();
}
