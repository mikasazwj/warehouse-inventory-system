package com.warehouse.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 设置默认缓存配置
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats());
            
        // 预定义缓存名称
        cacheManager.setCacheNames(Arrays.asList(
            "dashboard-stats",      // 仪表盘统计数据 - 5分钟
            "dashboard-alerts",     // 告警信息 - 2分钟
            "dashboard-todos",      // 待办事项 - 5分钟
            "user-permissions",     // 用户权限 - 30分钟
            "user-warehouses",      // 用户仓库 - 30分钟
            "goods-categories",     // 货物分类 - 1小时
            "warehouses",           // 仓库列表 - 1小时
            "inventory-stats",      // 库存统计 - 10分钟
            "business-trend"        // 业务趋势 - 15分钟
        ));
        
        return cacheManager;
    }

    /**
     * 仪表盘统计数据缓存 - 5分钟过期
     */
    @Bean("dashboardStatsCache")
    public Caffeine<Object, Object> dashboardStatsCache() {
        return Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats();
    }

    /**
     * 告警信息缓存 - 2分钟过期
     */
    @Bean("dashboardAlertsCache")
    public Caffeine<Object, Object> dashboardAlertsCache() {
        return Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .recordStats();
    }

    /**
     * 用户权限缓存 - 30分钟过期
     */
    @Bean("userPermissionsCache")
    public Caffeine<Object, Object> userPermissionsCache() {
        return Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats();
    }

    /**
     * 基础数据缓存 - 1小时过期
     */
    @Bean("basicDataCache")
    public Caffeine<Object, Object> basicDataCache() {
        return Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .recordStats();
    }

    /**
     * 业务趋势缓存 - 15分钟过期
     */
    @Bean("businessTrendCache")
    public Caffeine<Object, Object> businessTrendCache() {
        return Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .recordStats();
    }
}
