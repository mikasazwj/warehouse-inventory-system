-- 性能优化索引
-- 这些索引用于优化常用查询的性能

-- 1. 库存查询优化索引
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse_goods_quantity 
ON inventories (warehouse_id, goods_id, quantity);

CREATE INDEX IF NOT EXISTS idx_inventory_quantity_status 
ON inventories (quantity, deleted) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_inventory_expiry_date 
ON inventories (expiry_date, quantity) WHERE expiry_date IS NOT NULL AND quantity > 0;

-- 2. 订单查询优化索引
CREATE INDEX IF NOT EXISTS idx_inbound_orders_status_date 
ON inbound_orders (status, planned_date, deleted) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_outbound_orders_status_date 
ON outbound_orders (status, planned_date, deleted) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_transfer_orders_status_date 
ON transfer_orders (status, planned_date, deleted) WHERE deleted = false;

-- 3. 时间范围查询优化
CREATE INDEX IF NOT EXISTS idx_inbound_orders_created_time 
ON inbound_orders (created_time, deleted) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_outbound_orders_created_time 
ON outbound_orders (created_time, deleted) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_operation_logs_time_type 
ON operation_logs (operation_time, operation_type, deleted) WHERE deleted = false;

-- 4. 货物分类查询优化
CREATE INDEX IF NOT EXISTS idx_goods_category_enabled 
ON goods (category_id, enabled, deleted) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_goods_name_code 
ON goods (name, code, deleted) WHERE deleted = false;

-- 5. 用户权限查询优化
CREATE INDEX IF NOT EXISTS idx_users_role_enabled 
ON users (role, enabled, deleted) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_user_warehouses_composite 
ON user_warehouses (user_id, warehouse_id);

-- 6. 审批流程优化
CREATE INDEX IF NOT EXISTS idx_approval_records_status_time 
ON approval_records (approval_status, apply_time, deleted) WHERE deleted = false;

-- 7. 统计查询优化
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse_summary 
ON inventories (warehouse_id, deleted, quantity) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_orders_warehouse_amount 
ON inbound_orders (warehouse_id, total_amount, created_time, deleted) WHERE deleted = false;

-- 8. 复合查询优化
CREATE INDEX IF NOT EXISTS idx_inventory_goods_warehouse_batch 
ON inventories (goods_id, warehouse_id, batch_number, deleted) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_order_details_order_goods 
ON inbound_order_details (inbound_order_id, goods_id, deleted) WHERE deleted = false;

-- 9. 全文搜索优化（如果支持）
-- CREATE INDEX IF NOT EXISTS idx_goods_search 
-- ON goods USING gin(to_tsvector('simple', name || ' ' || code || ' ' || COALESCE(brand, '') || ' ' || COALESCE(model, ''))) 
-- WHERE deleted = false;

-- 10. 分区表建议（对于大数据量）
-- 可以考虑按时间对以下表进行分区：
-- - operation_logs (按月分区)
-- - approval_records (按月分区)
-- - 各种订单表 (按年分区)

-- 使用说明：
-- 1. 这些索引主要针对常用查询场景
-- 2. 在生产环境中应该根据实际查询模式调整
-- 3. 定期监控索引使用情况，删除未使用的索引
-- 4. 考虑在业务低峰期创建索引以减少影响
