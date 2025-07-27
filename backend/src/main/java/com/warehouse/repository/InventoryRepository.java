package com.warehouse.repository;

import com.warehouse.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 库存数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * 根据仓库和货物查找库存
     */
    Optional<Inventory> findByWarehouseIdAndGoodsIdAndDeletedFalse(Long warehouseId, Long goodsId);

    /**
     * 根据仓库和货物和批次查找库存
     */
    Optional<Inventory> findByWarehouseIdAndGoodsIdAndBatchNumberAndDeletedFalse(Long warehouseId, Long goodsId, String batchNumber);

    /**
     * 根据仓库ID查找所有库存
     */
    List<Inventory> findByWarehouseIdAndDeletedFalseOrderByGoodsCode(Long warehouseId);

    /**
     * 根据货物ID查找所有库存
     */
    List<Inventory> findByGoodsIdAndDeletedFalseOrderByWarehouseCode(Long goodsId);

    /**
     * 根据仓库ID查找有库存的记录
     */
    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.quantity > 0 AND i.deleted = false ORDER BY i.goods.code")
    List<Inventory> findByWarehouseIdWithStock(@Param("warehouseId") Long warehouseId);

    /**
     * 根据货物ID查找有库存的记录
     */
    @Query("SELECT i FROM Inventory i WHERE i.goods.id = :goodsId AND i.quantity > 0 AND i.deleted = false ORDER BY i.warehouse.code")
    List<Inventory> findByGoodsIdWithStock(@Param("goodsId") Long goodsId);

    /**
     * 查找低库存商品
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.quantity > 0 AND i.quantity < i.goods.minStock AND i.goods.minStock > 0 ORDER BY i.quantity ASC")
    List<Inventory> findLowStockInventories();

    /**
     * 查找零库存商品
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.quantity = 0")
    List<Inventory> findZeroStockInventories();

    /**
     * 根据仓库查找低库存商品
     */
    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.quantity > 0 AND i.quantity < i.goods.minStock AND i.goods.minStock > 0 ORDER BY i.quantity ASC")
    List<Inventory> findLowStockInventoriesByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 查找即将过期的库存
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.expiryDate IS NOT NULL AND i.expiryDate <= :date AND i.quantity > 0 ORDER BY i.expiryDate ASC")
    List<Inventory> findNearExpiryInventories(@Param("date") LocalDate date);

    /**
     * 根据仓库查找即将过期的库存
     */
    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.expiryDate IS NOT NULL AND i.expiryDate <= :date AND i.quantity > 0 ORDER BY i.expiryDate ASC")
    List<Inventory> findNearExpiryInventoriesByWarehouse(@Param("warehouseId") Long warehouseId, @Param("date") LocalDate date);

    /**
     * 查找已过期的库存 - H2/MySQL兼容版本
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.expiryDate IS NOT NULL AND i.expiryDate < CURRENT_DATE AND i.quantity > 0 ORDER BY i.expiryDate ASC")
    List<Inventory> findExpiredInventories();

    /**
     * 分页查询库存（支持仓库和关键字搜索）
     */
    @Query("SELECT i FROM Inventory i " +
           "LEFT JOIN FETCH i.warehouse w " +
           "LEFT JOIN FETCH i.goods g " +
           "LEFT JOIN FETCH g.category c " +
           "WHERE i.deleted = false AND " +
           "(:warehouseId IS NULL OR w.id = :warehouseId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "g.code LIKE %:keyword% OR g.name LIKE %:keyword% OR i.batchNumber LIKE %:keyword%)")
    Page<Inventory> findByWarehouseAndKeyword(@Param("warehouseId") Long warehouseId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询有库存的记录
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.quantity > 0 AND " +
           "(:warehouseId IS NULL OR i.warehouse.id = :warehouseId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "i.goods.code LIKE %:keyword% OR i.goods.name LIKE %:keyword% OR i.batchNumber LIKE %:keyword%)")
    Page<Inventory> findWithStockByWarehouseAndKeyword(@Param("warehouseId") Long warehouseId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计库存记录数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.deleted = false")
    long countNotDeleted();

    /**
     * 统计有库存的记录数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.deleted = false AND i.quantity > 0")
    long countWithStock();

    /**
     * 根据仓库统计库存记录数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false")
    long countByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 根据仓库统计有库存的记录数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.quantity > 0")
    long countWithStockByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 统计低库存商品数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.deleted = false AND i.quantity <= i.goods.minStock AND i.goods.minStock > 0")
    long countLowStock();

    /**
     * 根据仓库统计低库存商品数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.quantity <= i.goods.minStock AND i.goods.minStock > 0")
    long countLowStockByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 统计即将过期的库存数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.deleted = false AND i.expiryDate IS NOT NULL AND i.expiryDate <= :date AND i.quantity > 0")
    long countNearExpiry(@Param("date") LocalDate date);

    /**
     * 统计已过期的库存数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.deleted = false AND i.expiryDate IS NOT NULL AND i.expiryDate < CURRENT_DATE AND i.quantity > 0")
    long countExpired();

    /**
     * 计算库存总价值
     */
    @Query("SELECT COALESCE(SUM(i.quantity * i.averageCost), 0) FROM Inventory i WHERE i.deleted = false AND i.quantity > 0")
    BigDecimal calculateTotalValue();

    /**
     * 根据仓库计算库存总价值
     */
    @Query("SELECT COALESCE(SUM(i.quantity * i.averageCost), 0) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.quantity > 0")
    BigDecimal calculateTotalValueByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 根据货物分类统计库存数量
     */
    @Query("SELECT i.goods.category.name, COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.deleted = false AND i.quantity > 0 GROUP BY i.goods.category.id, i.goods.category.name ORDER BY i.goods.category.name")
    List<Object[]> countByGoodsCategory();

    /**
     * 根据仓库和货物分类统计库存数量
     */
    @Query("SELECT i.goods.category.name, COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.quantity > 0 GROUP BY i.goods.category.id, i.goods.category.name ORDER BY i.goods.category.name")
    List<Object[]> countByWarehouseAndGoodsCategory(@Param("warehouseId") Long warehouseId);

    /**
     * 查找指定数量范围的库存
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.quantity BETWEEN :minQuantity AND :maxQuantity ORDER BY i.quantity DESC")
    List<Inventory> findByQuantityRange(@Param("minQuantity") BigDecimal minQuantity, @Param("maxQuantity") BigDecimal maxQuantity);

    /**
     * 根据批次号查找库存
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.batchNumber LIKE %:batchNumber% ORDER BY i.batchNumber")
    List<Inventory> findByBatchNumberContaining(@Param("batchNumber") String batchNumber);

    /**
     * 根据库位查找库存
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.location LIKE %:location% ORDER BY i.location")
    List<Inventory> findByLocationContaining(@Param("location") String location);

    // ===== 为Service层添加兼容方法 =====

    /**
     * 根据仓库和货物查找库存 (兼容方法)
     */
    default Optional<Inventory> findByWarehouseIdAndGoodsId(Long warehouseId, Long goodsId) {
        return findByWarehouseIdAndGoodsIdAndDeletedFalse(warehouseId, goodsId);
    }

    /**
     * 根据仓库ID查找所有库存 (兼容方法)
     */
    default List<Inventory> findByWarehouseIdOrderByGoodsCode(Long warehouseId) {
        return findByWarehouseIdAndDeletedFalseOrderByGoodsCode(warehouseId);
    }

    /**
     * 根据货物ID查找所有库存 (兼容方法)
     */
    default List<Inventory> findByGoodsIdOrderByWarehouseName(Long goodsId) {
        return findByGoodsIdAndDeletedFalseOrderByWarehouseCode(goodsId);
    }

    /**
     * 查找所有库存 (兼容方法)
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false ORDER BY i.warehouse.name, i.goods.name")
    List<Inventory> findAllOrderByWarehouseAndGoods();

    /**
     * 查找有库存的记录 (兼容方法)
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.quantity > 0 ORDER BY i.warehouse.name, i.goods.name")
    List<Inventory> findWithStock();

    /**
     * 查找低库存商品 (兼容方法)
     */
    default List<Inventory> findLowStock() {
        return findLowStockInventories();
    }

    /**
     * 查找零库存商品 (兼容方法)
     */
    default List<Inventory> findZeroStock() {
        return findZeroStockInventories();
    }

    /**
     * 查找即将过期的库存 (兼容方法)
     */
    default List<Inventory> findNearExpiry(LocalDate date) {
        return findNearExpiryInventories(date);
    }

    /**
     * 查找已过期的库存 (兼容方法)
     */
    default List<Inventory> findExpired(LocalDate date) {
        return findExpiredInventories();
    }

    /**
     * 查找有锁定库存的记录 (兼容方法)
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.lockedQuantity > 0 ORDER BY i.warehouse.name, i.goods.name")
    List<Inventory> findWithLockedStock();

    /**
     * 根据关键字搜索库存 (兼容方法)
     */
    default Page<Inventory> findByKeyword(String keyword, Pageable pageable) {
        return findByWarehouseAndKeyword(null, keyword, pageable);
    }

    /**
     * 根据货物和关键字搜索库存 (兼容方法)
     */
    default Page<Inventory> findByGoodsAndKeyword(Long goodsId, String keyword, Pageable pageable) {
        return findByWarehouseAndKeyword(null, keyword, pageable);
    }

    /**
     * 根据生产日期范围查找库存 (兼容方法)
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.productionDate BETWEEN :startDate AND :endDate ORDER BY i.productionDate")
    List<Inventory> findByProductionDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据过期日期范围查找库存 (兼容方法)
     */
    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.expiryDate BETWEEN :startDate AND :endDate ORDER BY i.expiryDate")
    List<Inventory> findByExpiryDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // ===== 统计方法 =====

    /**
     * 统计总数量
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.deleted = false")
    Long sumTotalQuantity();

    /**
     * 统计可用数量
     */
    @Query("SELECT COALESCE(SUM(i.availableQuantity), 0) FROM Inventory i WHERE i.deleted = false")
    Long sumTotalAvailableQuantity();

    /**
     * 统计锁定数量
     */
    @Query("SELECT COALESCE(SUM(i.lockedQuantity), 0) FROM Inventory i WHERE i.deleted = false")
    Long sumTotalLockedQuantity();

    /**
     * 统计总价值
     */
    @Query("SELECT COALESCE(SUM(i.quantity * i.averageCost), 0) FROM Inventory i WHERE i.deleted = false AND i.quantity > 0")
    BigDecimal sumTotalValue();

    /**
     * 统计不同仓库数量
     */
    @Query("SELECT COUNT(DISTINCT i.warehouse.id) FROM Inventory i WHERE i.deleted = false")
    Long countDistinctWarehouses();

    /**
     * 统计不同货物数量
     */
    @Query("SELECT COUNT(DISTINCT i.goods.id) FROM Inventory i WHERE i.deleted = false")
    Long countDistinctGoods();

    /**
     * 根据货物统计有库存的仓库数量
     */
    @Query("SELECT COUNT(DISTINCT i.warehouse.id) FROM Inventory i WHERE i.goods.id = :goodsId AND i.deleted = false AND i.quantity > 0")
    Long countWithStockByGoods(@Param("goodsId") Long goodsId);

    /**
     * 根据货物统计总数量
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.goods.id = :goodsId AND i.deleted = false")
    Long sumQuantityByGoods(@Param("goodsId") Long goodsId);

    /**
     * 根据货物统计可用数量
     */
    @Query("SELECT COALESCE(SUM(i.availableQuantity), 0) FROM Inventory i WHERE i.goods.id = :goodsId AND i.deleted = false")
    Long sumAvailableQuantityByGoods(@Param("goodsId") Long goodsId);

    /**
     * 根据货物统计锁定数量
     */
    @Query("SELECT COALESCE(SUM(i.lockedQuantity), 0) FROM Inventory i WHERE i.goods.id = :goodsId AND i.deleted = false")
    Long sumLockedQuantityByGoods(@Param("goodsId") Long goodsId);

    /**
     * 根据货物统计总价值
     */
    @Query("SELECT COALESCE(SUM(i.quantity * i.averageCost), 0) FROM Inventory i WHERE i.goods.id = :goodsId AND i.deleted = false AND i.quantity > 0")
    BigDecimal sumValueByGoods(@Param("goodsId") Long goodsId);

    /**
     * 根据货物统计仓库数量
     */
    @Query("SELECT COUNT(DISTINCT i.warehouse.id) FROM Inventory i WHERE i.goods.id = :goodsId AND i.deleted = false")
    Long countWarehousesByGoods(@Param("goodsId") Long goodsId);

    /**
     * 根据筛选条件查询库存
     */
    @Query("SELECT i FROM Inventory i " +
           "LEFT JOIN FETCH i.warehouse w " +
           "LEFT JOIN FETCH i.goods g " +
           "LEFT JOIN FETCH g.category c " +
           "WHERE i.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "g.code LIKE %:keyword% OR g.name LIKE %:keyword%) AND " +
           "(:warehouseId IS NULL OR w.id = :warehouseId) AND " +
           "(:categoryId IS NULL OR c.id = :categoryId) AND " +
           "(:stockStatus IS NULL OR :stockStatus = '' OR " +
           "(:stockStatus = 'zero' AND i.quantity = 0) OR " +
           "(:stockStatus = 'low' AND i.quantity > 0 AND g.minStock IS NOT NULL AND i.quantity < g.minStock) OR " +
           "(:stockStatus = 'high' AND g.maxStock IS NOT NULL AND i.quantity > (g.maxStock * 1.15)) OR " +
           "(:stockStatus = 'normal' AND i.quantity > 0 AND " +
           " (g.minStock IS NULL OR i.quantity >= g.minStock) AND " +
           " (g.maxStock IS NULL OR i.quantity <= (g.maxStock * 1.15))))")
    Page<Inventory> findByFilters(@Param("keyword") String keyword,
                                 @Param("warehouseId") Long warehouseId,
                                 @Param("categoryId") Long categoryId,
                                 @Param("stockStatus") String stockStatus,
                                 Pageable pageable);

    /**
     * 查询所有有库存记录的货物（去重）
     */
    @Query("SELECT DISTINCT i FROM Inventory i JOIN FETCH i.goods WHERE i.goods IS NOT NULL AND i.deleted = false ORDER BY i.goods.code")
    List<Inventory> findDistinctGoods();

    // 按仓库统计的方法

    /**
     * 根据仓库ID统计库存记录数
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false")
    long countByWarehouseId(@Param("warehouseId") Long warehouseId);

    /**
     * 根据仓库ID统计总库存数量
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false")
    Long sumTotalQuantityByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 根据仓库ID统计可用库存数量
     */
    @Query("SELECT COALESCE(SUM(i.availableQuantity), 0) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false")
    Long sumTotalAvailableQuantityByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 根据仓库ID统计锁定库存数量
     */
    @Query("SELECT COALESCE(SUM(i.lockedQuantity), 0) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false")
    Long sumTotalLockedQuantityByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 根据仓库ID统计库存总价值
     */
    @Query("SELECT COALESCE(SUM(i.quantity * i.averageCost), 0) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false")
    BigDecimal sumTotalValueByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 根据仓库ID查找低库存商品
     */
    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.quantity > 0 AND i.quantity < i.goods.minStock AND i.goods.minStock > 0")
    List<Inventory> findLowStockByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 根据仓库ID查找零库存商品
     */
    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.quantity = 0 AND i.deleted = false")
    List<Inventory> findZeroStockByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 根据仓库ID查找即将过期的商品
     */
    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.expiryDate IS NOT NULL AND i.expiryDate <= :date AND i.deleted = false")
    List<Inventory> findNearExpiryByWarehouse(@Param("warehouseId") Long warehouseId, @Param("date") LocalDate date);

    /**
     * 根据仓库ID查找已过期的商品
     */
    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.expiryDate IS NOT NULL AND i.expiryDate < :date AND i.deleted = false")
    List<Inventory> findExpiredByWarehouse(@Param("warehouseId") Long warehouseId, @Param("date") LocalDate date);

    /**
     * 根据仓库ID统计不同货物数量
     */
    @Query("SELECT COUNT(DISTINCT i.goods.id) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false")
    long countDistinctGoodsByWarehouse(@Param("warehouseId") Long warehouseId);

    // ===== 仪表盘专用查询方法 =====

    /**
     * 获取指定仓库的低库存商品信息（用于仪表盘）
     */
    @Query("SELECT i.id, i.goods.name, i.warehouse.name, i.quantity, i.goods.minStock " +
           "FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false " +
           "AND i.quantity > 0 AND i.goods.minStock > 0 AND i.quantity <= i.goods.minStock")
    List<Object[]> findLowStockInfoByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 获取所有低库存商品信息（用于仪表盘）
     */
    @Query("SELECT i.id, i.goods.name, i.warehouse.name, i.quantity, i.goods.minStock " +
           "FROM Inventory i WHERE i.deleted = false " +
           "AND i.quantity > 0 AND i.goods.minStock > 0 AND i.quantity <= i.goods.minStock")
    List<Object[]> findLowStockInfo();

    /**
     * 获取指定仓库的零库存商品信息（用于仪表盘）
     */
    @Query("SELECT i.id, i.goods.name, i.warehouse.name " +
           "FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.quantity = 0 AND i.deleted = false")
    List<Object[]> findZeroStockInfoByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 获取所有零库存商品信息（用于仪表盘）
     */
    @Query("SELECT i.id, i.goods.name, i.warehouse.name " +
           "FROM Inventory i WHERE i.quantity = 0 AND i.deleted = false")
    List<Object[]> findZeroStockInfo();

    /**
     * 获取所有仓库的库存统计（用于仪表盘）
     */
    @Query("SELECT w.name, COALESCE(SUM(i.quantity), 0) " +
           "FROM Warehouse w LEFT JOIN Inventory i ON w.id = i.warehouse.id AND i.deleted = false " +
           "GROUP BY w.id, w.name ORDER BY SUM(i.quantity) DESC")
    List<Object[]> getAllWarehouseInventoryStats();

    /**
     * 获取指定仓库的库存统计（用于仪表盘）
     */
    @Query("SELECT w.name, COALESCE(SUM(i.quantity), 0) " +
           "FROM Warehouse w LEFT JOIN Inventory i ON w.id = i.warehouse.id AND i.deleted = false " +
           "WHERE w.id = :warehouseId " +
           "GROUP BY w.id, w.name")
    List<Object[]> getWarehouseInventoryStats(@Param("warehouseId") Long warehouseId);

    // ==================== 报表相关查询方法 ====================

    /**
     * 获取仓库分布数据
     */
    @Query("SELECT w.name, COUNT(i.id) " +
           "FROM Warehouse w LEFT JOIN Inventory i ON w.id = i.warehouse.id AND i.deleted = false " +
           "GROUP BY w.id, w.name ORDER BY COUNT(i.id) DESC")
    List<Object[]> findWarehouseDistribution();

    /**
     * 获取库存周转率数据（指定仓库）
     */
    @Query("SELECT g.name, " +
           "CASE WHEN i.quantity > 0 THEN (i.quantity * 1.0 / 30) ELSE 0 END, " +
           "i.quantity " +
           "FROM Inventory i JOIN i.goods g " +
           "WHERE i.warehouse.id = :warehouseId AND i.deleted = false " +
           "ORDER BY i.quantity DESC")
    List<Object[]> findInventoryStatsWithTurnover(@Param("warehouseId") Long warehouseId);

    /**
     * 获取所有库存周转率数据
     */
    @Query("SELECT g.name, " +
           "CASE WHEN SUM(i.quantity) > 0 THEN (SUM(i.quantity) * 1.0 / 30) ELSE 0 END, " +
           "SUM(i.quantity) " +
           "FROM Inventory i JOIN i.goods g " +
           "WHERE i.deleted = false " +
           "GROUP BY g.id, g.name " +
           "ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findAllInventoryStatsWithTurnover();

    /**
     * 获取活跃商品数据（指定仓库）
     */
    @Query("SELECT g.name FROM Inventory i JOIN i.goods g " +
           "WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.quantity > 0 " +
           "ORDER BY i.quantity DESC")
    List<Object[]> findActiveGoodsByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 获取所有活跃商品数据
     */
    @Query("SELECT g.name FROM Inventory i JOIN i.goods g " +
           "WHERE i.deleted = false AND i.quantity > 0 " +
           "GROUP BY g.id, g.name " +
           "ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findActiveGoods();

    /**
     * 获取仓库利用率数据（指定仓库）
     */
    @Query("SELECT w.name, SUM(i.quantity), 1000 " +
           "FROM Warehouse w LEFT JOIN Inventory i ON w.id = i.warehouse.id AND i.deleted = false " +
           "WHERE w.id = :warehouseId " +
           "GROUP BY w.id, w.name")
    List<Object[]> findWarehouseUtilization(@Param("warehouseId") Long warehouseId);

    /**
     * 获取所有仓库利用率数据
     */
    @Query("SELECT w.name, COALESCE(SUM(i.quantity), 0), 1000 " +
           "FROM Warehouse w LEFT JOIN Inventory i ON w.id = i.warehouse.id AND i.deleted = false " +
           "GROUP BY w.id, w.name " +
           "ORDER BY w.name")
    List<Object[]> findAllWarehouseUtilization();

    /**
     * 获取入库商品排行（指定仓库和时间范围）
     */
    @Query("SELECT g.code, g.name, c.name, SUM(iod.quantity), SUM(iod.amount) " +
           "FROM InboundOrderDetail iod " +
           "JOIN iod.inboundOrder io " +
           "JOIN iod.goods g " +
           "LEFT JOIN g.category c " +
           "WHERE io.warehouse.id = :warehouseId AND io.deleted = false " +
           "AND CAST(io.createdTime AS DATE) >= :startDate AND CAST(io.createdTime AS DATE) <= :endDate " +
           "GROUP BY g.id, g.code, g.name, c.name " +
           "ORDER BY SUM(iod.quantity) DESC")
    List<Object[]> findTopInboundGoodsByWarehouse(@Param("warehouseId") Long warehouseId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * 获取入库商品排行（所有仓库）
     */
    @Query("SELECT g.code, g.name, c.name, SUM(iod.quantity), SUM(iod.amount) " +
           "FROM InboundOrderDetail iod " +
           "JOIN iod.inboundOrder io " +
           "JOIN iod.goods g " +
           "LEFT JOIN g.category c " +
           "WHERE io.deleted = false " +
           "AND CAST(io.createdTime AS DATE) >= :startDate AND CAST(io.createdTime AS DATE) <= :endDate " +
           "GROUP BY g.id, g.code, g.name, c.name " +
           "ORDER BY SUM(iod.quantity) DESC")
    List<Object[]> findTopInboundGoods(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * 获取出库商品排行（指定仓库和时间范围）
     */
    @Query("SELECT g.code, g.name, c.name, SUM(ood.quantity), SUM(ood.amount) " +
           "FROM OutboundOrderDetail ood " +
           "JOIN ood.outboundOrder oo " +
           "JOIN ood.goods g " +
           "LEFT JOIN g.category c " +
           "WHERE oo.warehouse.id = :warehouseId AND oo.deleted = false " +
           "AND CAST(oo.createdTime AS DATE) >= :startDate AND CAST(oo.createdTime AS DATE) <= :endDate " +
           "GROUP BY g.id, g.code, g.name, c.name " +
           "ORDER BY SUM(ood.quantity) DESC")
    List<Object[]> findTopOutboundGoodsByWarehouse(@Param("warehouseId") Long warehouseId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 获取出库商品排行（所有仓库）
     */
    @Query("SELECT g.code, g.name, c.name, SUM(ood.quantity), SUM(ood.amount) " +
           "FROM OutboundOrderDetail ood " +
           "JOIN ood.outboundOrder oo " +
           "JOIN ood.goods g " +
           "LEFT JOIN g.category c " +
           "WHERE oo.deleted = false " +
           "AND CAST(oo.createdTime AS DATE) >= :startDate AND CAST(oo.createdTime AS DATE) <= :endDate " +
           "GROUP BY g.id, g.code, g.name, c.name " +
           "ORDER BY SUM(ood.quantity) DESC")
    List<Object[]> findTopOutboundGoods(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * 获取周转率排行（指定仓库）
     */
    @Query("SELECT g.code, g.name, c.name, i.quantity, " +
           "CASE WHEN i.quantity > 0 THEN (i.quantity * 1.0 / 30) ELSE 0 END, " +
           "CASE WHEN i.quantity > 0 THEN 100.0 ELSE 0 END " +
           "FROM Inventory i " +
           "JOIN i.goods g " +
           "LEFT JOIN g.category c " +
           "WHERE i.warehouse.id = :warehouseId AND i.deleted = false " +
           "ORDER BY i.quantity DESC")
    List<Object[]> findTopTurnoverGoodsByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 获取周转率排行（所有仓库）
     */
    @Query("SELECT g.code, g.name, c.name, SUM(i.quantity), " +
           "CASE WHEN SUM(i.quantity) > 0 THEN (SUM(i.quantity) * 1.0 / 30) ELSE 0 END, " +
           "CASE WHEN SUM(i.quantity) > 0 THEN 100.0 ELSE 0 END " +
           "FROM Inventory i " +
           "JOIN i.goods g " +
           "LEFT JOIN g.category c " +
           "WHERE i.deleted = false " +
           "GROUP BY g.id, g.code, g.name, c.name " +
           "ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findTopTurnoverGoods();

    // ==================== 库存分析相关查询方法 ====================

    /**
     * 获取指定仓库的库存总量
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false")
    long getTotalQuantityByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 获取所有仓库的库存总量
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.deleted = false")
    long getTotalQuantity();

    /**
     * 获取指定仓库的库存总价值
     */
    @Query("SELECT COALESCE(SUM(i.quantity * i.averageCost), 0) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false")
    BigDecimal getTotalValueByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 获取所有仓库的库存总价值
     */
    @Query("SELECT COALESCE(SUM(i.quantity * i.averageCost), 0) FROM Inventory i WHERE i.deleted = false")
    BigDecimal getTotalValue();



    /**
     * 统计指定仓库的零库存商品数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.quantity = 0")
    long countZeroStockByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 统计所有仓库的零库存商品数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.deleted = false AND i.quantity = 0")
    long countZeroStock();

    /**
     * 获取所有仓库的库存统计信息
     */
    @Query("SELECT w.name, COUNT(DISTINCT i.goods.id), COALESCE(SUM(i.quantity), 0), " +
           "COALESCE(SUM(i.quantity * i.averageCost), 0), w.id, " +
           "COUNT(CASE WHEN i.quantity <= 10 THEN 1 END) " +
           "FROM Inventory i JOIN i.warehouse w " +
           "WHERE i.deleted = false " +
           "GROUP BY w.id, w.name")
    List<Object[]> findWarehouseInventoryStats();

    /**
     * 获取指定仓库的库存统计信息
     */
    @Query("SELECT w.name, COUNT(DISTINCT i.goods.id), COALESCE(SUM(i.quantity), 0), " +
           "COALESCE(SUM(i.quantity * i.averageCost), 0), w.id, " +
           "COUNT(CASE WHEN i.quantity <= 10 THEN 1 END) " +
           "FROM Inventory i JOIN i.warehouse w " +
           "WHERE i.deleted = false AND w.id = :warehouseId " +
           "GROUP BY w.id, w.name")
    Object[] findWarehouseInventoryStatsByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 获取指定仓库的出库数量总计
     */
    @Query("SELECT COALESCE(SUM(ood.quantity), 0) FROM OutboundOrderDetail ood " +
           "JOIN ood.outboundOrder oo " +
           "WHERE oo.warehouse.id = :warehouseId AND oo.deleted = false")
    Long sumOutboundQuantityByWarehouse(@Param("warehouseId") Long warehouseId);



    /**
     * 统计滞销商品数量（在指定时间范围内没有出库记录）
     */
    @Query("SELECT COUNT(DISTINCT i.goods.id) FROM Inventory i " +
           "WHERE i.deleted = false " +
           "AND i.goods.id NOT IN (" +
           "    SELECT DISTINCT ood.goods.id FROM OutboundOrderDetail ood " +
           "    JOIN ood.outboundOrder oo " +
           "    WHERE oo.deleted = false " +
           "    AND CAST(oo.createdTime AS DATE) >= :startDate " +
           "    AND CAST(oo.createdTime AS DATE) <= :endDate" +
           ")")
    long countSlowMovingItems(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计指定仓库的滞销商品数量
     */
    @Query("SELECT COUNT(DISTINCT i.goods.id) FROM Inventory i " +
           "WHERE i.deleted = false AND i.warehouse.id = :warehouseId " +
           "AND i.goods.id NOT IN (" +
           "    SELECT DISTINCT ood.goods.id FROM OutboundOrderDetail ood " +
           "    JOIN ood.outboundOrder oo " +
           "    WHERE oo.deleted = false AND oo.warehouse.id = :warehouseId " +
           "    AND CAST(oo.createdTime AS DATE) >= :startDate " +
           "    AND CAST(oo.createdTime AS DATE) <= :endDate" +
           ")")
    long countSlowMovingItemsByWarehouse(@Param("warehouseId") Long warehouseId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * 统计高价值低周转商品数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i " +
           "WHERE i.deleted = false " +
           "AND (i.quantity * i.averageCost) > 1000 " +
           "AND i.quantity > 50")
    long countHighValueLowTurnover();

    /**
     * 统计指定仓库的高价值低周转商品数量
     */
    @Query("SELECT COUNT(i) FROM Inventory i " +
           "WHERE i.deleted = false AND i.warehouse.id = :warehouseId " +
           "AND (i.quantity * i.averageCost) > 1000 " +
           "AND i.quantity > 50")
    long countHighValueLowTurnoverByWarehouse(@Param("warehouseId") Long warehouseId);

}
