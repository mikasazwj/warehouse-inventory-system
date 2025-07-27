package com.warehouse.repository;

import com.warehouse.entity.Goods;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 货物数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface GoodsRepository extends JpaRepository<Goods, Long> {

    /**
     * 根据编码查找货物
     */
    Optional<Goods> findByCodeAndDeletedFalse(String code);

    /**
     * 根据条形码查找货物
     */
    Optional<Goods> findByBarcodeAndDeletedFalse(String barcode);

    /**
     * 查找所有启用的货物
     */
    List<Goods> findByEnabledTrueAndDeletedFalseOrderByCode();

    /**
     * 查找所有未删除的货物
     */
    List<Goods> findByDeletedFalseOrderByCode();

    /**
     * 根据分类查找货物
     */
    List<Goods> findByCategoryIdAndDeletedFalseOrderByCode(Long categoryId);



    /**
     * 检查编码是否存在
     */
    boolean existsByCodeAndDeletedFalse(String code);

    /**
     * 检查条形码是否存在
     */
    boolean existsByBarcodeAndDeletedFalse(String barcode);

    /**
     * 检查编码是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(g) > 0 FROM Goods g WHERE g.code = :code AND g.id != :id AND g.deleted = false")
    boolean existsByCodeAndIdNotAndDeletedFalse(@Param("code") String code, @Param("id") Long id);

    /**
     * 检查条形码是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(g) > 0 FROM Goods g WHERE g.barcode = :barcode AND g.id != :id AND g.deleted = false")
    boolean existsByBarcodeAndIdNotAndDeletedFalse(@Param("barcode") String barcode, @Param("id") Long id);

    /**
     * 检查分类下是否有商品
     */
    boolean existsByCategoryIdAndDeletedFalse(Long categoryId);

    /**
     * 分页查询货物（支持关键字搜索）
     */
    @Query("SELECT g FROM Goods g WHERE g.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "g.code LIKE %:keyword% OR g.name LIKE %:keyword% OR g.shortName LIKE %:keyword% OR " +
           "g.model LIKE %:keyword% OR g.brand LIKE %:keyword% OR g.barcode LIKE %:keyword%)")
    Page<Goods> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询货物（支持分类和关键字搜索）
     */
    @Query("SELECT g FROM Goods g WHERE g.deleted = false AND " +
           "(:categoryId IS NULL OR g.category.id = :categoryId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "g.code LIKE %:keyword% OR g.name LIKE %:keyword% OR g.shortName LIKE %:keyword% OR " +
           "g.model LIKE %:keyword% OR g.brand LIKE %:keyword% OR g.barcode LIKE %:keyword%)")
    Page<Goods> findByCategoryAndKeyword(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);



    /**
     * 分页查询货物（支持启用状态和关键字搜索）
     */
    @Query("SELECT g FROM Goods g WHERE g.deleted = false AND " +
           "(:enabled IS NULL OR g.enabled = :enabled) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "g.code LIKE %:keyword% OR g.name LIKE %:keyword% OR g.shortName LIKE %:keyword% OR " +
           "g.model LIKE %:keyword% OR g.brand LIKE %:keyword% OR g.barcode LIKE %:keyword%)")
    Page<Goods> findByEnabledAndKeyword(@Param("enabled") Boolean enabled, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询货物（支持关键字、分类和启用状态搜索）
     */
    @Query("SELECT g FROM Goods g WHERE g.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "g.code LIKE %:keyword% OR g.name LIKE %:keyword% OR g.shortName LIKE %:keyword% OR " +
           "g.model LIKE %:keyword% OR g.brand LIKE %:keyword% OR g.barcode LIKE %:keyword%) AND " +
           "(:categoryId IS NULL OR g.category.id = :categoryId) AND " +
           "(:enabled IS NULL OR g.enabled = :enabled)")
    Page<Goods> findByKeywordAndCategoryAndEnabled(@Param("keyword") String keyword,
                                                   @Param("categoryId") Long categoryId,
                                                   @Param("enabled") Boolean enabled,
                                                   Pageable pageable);

    /**
     * 统计货物数量
     */
    @Query("SELECT COUNT(g) FROM Goods g WHERE g.deleted = false")
    long countNotDeleted();

    /**
     * 统计启用的货物数量
     */
    @Query("SELECT COUNT(g) FROM Goods g WHERE g.enabled = true AND g.deleted = false")
    long countEnabledAndNotDeleted();

    /**
     * 根据分类统计货物数量
     */
    @Query("SELECT COUNT(g) FROM Goods g WHERE g.category.id = :categoryId AND g.deleted = false")
    long countByCategoryAndNotDeleted(@Param("categoryId") Long categoryId);



    /**
     * 查找有库存的货物
     */
    @Query("SELECT DISTINCT g FROM Goods g JOIN Inventory i ON g.id = i.goods.id WHERE g.deleted = false AND i.quantity > 0")
    List<Goods> findGoodsWithInventory();

    /**
     * 查找指定仓库有库存的货物 - 优化版本，使用JOIN FETCH
     */
    @Query("SELECT DISTINCT g FROM Goods g " +
           "LEFT JOIN FETCH g.category " +
           "JOIN Inventory i ON g.id = i.goods.id " +
           "WHERE g.deleted = false AND i.warehouse.id = :warehouseId AND i.quantity > 0 " +
           "ORDER BY g.code")
    List<Goods> findGoodsWithInventoryInWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 查找低库存货物 - 优化版本，使用JOIN FETCH
     */
    @Query("SELECT DISTINCT g FROM Goods g " +
           "LEFT JOIN FETCH g.category " +
           "JOIN Inventory i ON g.id = i.goods.id " +
           "WHERE g.deleted = false AND i.quantity <= g.minStock AND g.minStock > 0 " +
           "ORDER BY g.code")
    List<Goods> findLowStockGoods();

    /**
     * 根据品牌查找货物
     */
    @Query("SELECT g FROM Goods g WHERE g.deleted = false AND g.brand LIKE %:brand% ORDER BY g.code")
    List<Goods> findByBrandContaining(@Param("brand") String brand);

    /**
     * 根据型号查找货物
     */
    @Query("SELECT g FROM Goods g WHERE g.deleted = false AND g.model LIKE %:model% ORDER BY g.code")
    List<Goods> findByModelContaining(@Param("model") String model);

    /**
     * 查找有保质期的货物
     */
    @Query("SELECT g FROM Goods g WHERE g.deleted = false AND g.shelfLifeDays IS NOT NULL AND g.shelfLifeDays > 0 ORDER BY g.code")
    List<Goods> findGoodsWithShelfLife();

    /**
     * 查找设置了最低库存的货物
     */
    @Query("SELECT g FROM Goods g WHERE g.deleted = false AND g.minStock IS NOT NULL AND g.minStock > 0 ORDER BY g.code")
    List<Goods> findGoodsWithMinStock();

    /**
     * 根据分类路径查找货物（包含子分类）
     */
    @Query("SELECT g FROM Goods g WHERE g.deleted = false AND g.category.path LIKE :categoryPath% ORDER BY g.code")
    List<Goods> findByCategoryPath(@Param("categoryPath") String categoryPath);

    /**
     * 查找最近创建的货物
     */
    @Query("SELECT g FROM Goods g WHERE g.deleted = false ORDER BY g.createdTime DESC")
    List<Goods> findRecentGoods(Pageable pageable);

    /**
     * 查找最近更新的货物
     */
    @Query("SELECT g FROM Goods g WHERE g.deleted = false ORDER BY g.updatedTime DESC")
    List<Goods> findRecentUpdatedGoods(Pageable pageable);
}
