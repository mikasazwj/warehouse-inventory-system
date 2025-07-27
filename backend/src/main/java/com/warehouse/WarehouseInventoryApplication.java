package com.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 库房库存管理系统主启动类
 *
 * @author Warehouse Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories
@EntityScan
@EnableJpaAuditing
public class WarehouseInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseInventoryApplication.class, args);
    }
}
