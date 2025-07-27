package com.warehouse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控配置 - 简化版本
 */
@Configuration
@EnableScheduling
public class PerformanceMonitorConfig {

    @Autowired
    private DataSource dataSource;

    // 简单的性能计数器
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong slowQueries = new AtomicLong(0);

    /**
     * 简单的数据库健康检查
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void databaseHealthCheck() {
        try (Connection connection = dataSource.getConnection()) {
            long start = System.currentTimeMillis();
            connection.isValid(5);
            long duration = System.currentTimeMillis() - start;

            if (duration > 1000) {
                System.out.println("Warning: Database connection check took " + duration + "ms");
            }
        } catch (Exception e) {
            System.err.println("Database health check failed: " + e.getMessage());
        }
    }

}
