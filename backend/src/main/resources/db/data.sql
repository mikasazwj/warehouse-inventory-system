-- 库房库存管理系统 - 本地开发环境测试数据

-- 插入默认管理员用户（密码：admin123）
INSERT INTO users (id, username, password, real_name, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, login_count, created_time, updated_time, created_by, updated_by, deleted, version)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '系统管理员', 'ROLE_ADMIN', true, true, true, true, 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'system', 'system', false, 0);

-- 插入测试仓库
INSERT INTO warehouses (id, name, code, address, contact_person, contact_phone, description, enabled, created_time, updated_time, created_by, updated_by, deleted, version)
VALUES (1, '主仓库', 'WH001', '北京市朝阳区测试地址1号', '张三', '13800138001', '主要仓库', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'admin', 'admin', false, 0);

INSERT INTO warehouses (id, name, code, address, contact_person, contact_phone, description, enabled, created_time, updated_time, created_by, updated_by, deleted, version)
VALUES (2, '分仓库', 'WH002', '北京市海淀区测试地址2号', '李四', '13800138002', '分支仓库', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'admin', 'admin', false, 0);

-- 插入货物分类
INSERT INTO goods_categories (id, name, code, parent_id, level, sort_order, description, created_time, updated_time, created_by, updated_by, deleted, version)
VALUES (1, '电子产品', 'CAT001', NULL, 1, 1, '各类电子产品', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'admin', 'admin', false, 0);

INSERT INTO goods_categories (id, name, code, parent_id, level, sort_order, description, created_time, updated_time, created_by, updated_by, deleted, version)
VALUES (2, '手机', 'CAT002', 1, 2, 1, '智能手机', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'admin', 'admin', false, 0);

INSERT INTO goods_categories (id, name, code, parent_id, level, sort_order, description, created_time, updated_time, created_by, updated_by, deleted, version)
VALUES (3, '电脑', 'CAT003', 1, 2, 2, '台式机和笔记本', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'admin', 'admin', false, 0);

-- 插入测试货物
INSERT INTO goods (id, name, code, barcode, category_id, specification, unit, purchase_price, sale_price, description, enabled, created_time, updated_time, created_by, updated_by, deleted, version)
VALUES (1, 'iPhone 15', 'GOODS001', '1234567890123', 2, '128GB 黑色', '台', 5000.00, 6000.00, '苹果手机', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'admin', 'admin', false, 0);

INSERT INTO goods (id, name, code, barcode, category_id, specification, unit, purchase_price, sale_price, description, enabled, created_time, updated_time, created_by, updated_by, deleted, version)
VALUES (2, 'MacBook Pro', 'GOODS002', '1234567890124', 3, '13寸 M3芯片', '台', 12000.00, 15000.00, '苹果笔记本', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'admin', 'admin', false, 0);

-- 插入初始库存
INSERT INTO inventories (id, warehouse_id, goods_id, quantity, available_quantity, reserved_quantity, production_date, expiry_date, created_time, updated_time, created_by, updated_by, deleted, version)
VALUES (1, 1, 1, 100.000, 100.000, 0.000, '2024-01-01', '2026-01-01', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'admin', 'admin', false, 0);

INSERT INTO inventories (id, warehouse_id, goods_id, quantity, available_quantity, reserved_quantity, production_date, expiry_date, created_time, updated_time, created_by, updated_by, deleted, version)
VALUES (2, 1, 2, 50.000, 50.000, 0.000, '2024-01-01', '2026-01-01', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'admin', 'admin', false, 0);

-- 为管理员分配仓库权限
INSERT INTO user_warehouses (user_id, warehouse_id) VALUES (1, 1);
INSERT INTO user_warehouses (user_id, warehouse_id) VALUES (1, 2);
