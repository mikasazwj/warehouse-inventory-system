package com.warehouse.config;

import com.warehouse.entity.User;
import com.warehouse.entity.Warehouse;
import com.warehouse.enums.UserRole;
import com.warehouse.repository.UserRepository;
import com.warehouse.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化组件
 * 在应用启动时自动创建管理员用户
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("开始初始化数据...");

        initializeWarehouses();
        initializeAdminUser();

        logger.info("数据初始化完成");
    }

    /**
     * 初始化仓库数据
     */
    private void initializeWarehouses() {
        try {
            // 检查是否已存在仓库
            if (warehouseRepository.count() > 0) {
                logger.info("仓库数据已存在，跳过初始化");
                return;
            }

            // 创建默认仓库
            Warehouse warehouse1 = new Warehouse();
            warehouse1.setCode("WH001");
            warehouse1.setName("主仓库");
            warehouse1.setAddress("北京市朝阳区");
            warehouse1.setContactPerson("张三");
            warehouse1.setContactPhone("13800138001");
            warehouse1.setArea(1000.0);
            warehouse1.setCapacity(5000.0);
            warehouse1.setEnabled(true);

            Warehouse warehouse2 = new Warehouse();
            warehouse2.setCode("WH002");
            warehouse2.setName("分仓库");
            warehouse2.setAddress("上海市浦东新区");
            warehouse2.setContactPerson("李四");
            warehouse2.setContactPhone("13800138002");
            warehouse2.setArea(800.0);
            warehouse2.setCapacity(3000.0);
            warehouse2.setEnabled(true);

            warehouseRepository.save(warehouse1);
            warehouseRepository.save(warehouse2);

            logger.info("✅ 默认仓库数据创建成功");

        } catch (Exception e) {
            logger.error("❌ 创建仓库数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 初始化管理员用户
     */
    private void initializeAdminUser() {
        try {
            // 检查是否已存在tsadmin用户
            if (userRepository.findByUsername("tsadmin").isPresent()) {
                logger.info("管理员用户 tsadmin 已存在，跳过初始化");
                return;
            }

            // 创建管理员用户
            User admin = new User();
            admin.setUsername("tsadmin");
            admin.setPassword("$2a$10$SpQhyC8tC7nImv.HZHItRuoya2eOWGSV0nGKtUVxgJGmfRftmqAhy");
            admin.setRealName("系统管理员");
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setEnabled(true);
            admin.setAccountNonExpired(true);
            admin.setAccountNonLocked(true);
            admin.setCredentialsNonExpired(true);
            admin.setLoginCount(0);

            userRepository.save(admin);
            logger.info("✅ 管理员用户 tsadmin 创建成功");
            logger.info("🔑 登录信息: 用户名=tsadmin, 密码=mikasa2030");

        } catch (Exception e) {
            logger.error("❌ 创建管理员用户失败: {}", e.getMessage(), e);
        }
    }
}
