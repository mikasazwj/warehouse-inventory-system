package com.warehouse.repository;

import com.warehouse.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 仓库数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    /**
     * 根据编码查找仓库
     */
    Optional<Warehouse> findByCodeAndDeletedFalse(String code);

    /**
     * 根据名称查找仓库
     */
    Optional<Warehouse> findByNameAndDeletedFalse(String name);

    /**
     * 查找所有启用的仓库
     */
    List<Warehouse> findByEnabledTrueAndDeletedFalseOrderByCode();

    /**
     * 查找所有未删除的仓库
     */
    List<Warehouse> findByDeletedFalseOrderByCode();

    /**
     * 检查编码是否存在
     */
    boolean existsByCodeAndDeletedFalse(String code);

    /**
     * 检查名称是否存在
     */
    boolean existsByNameAndDeletedFalse(String name);

    /**
     * 检查编码是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(w) > 0 FROM Warehouse w WHERE w.code = :code AND w.id != :id AND w.deleted = false")
    boolean existsByCodeAndIdNotAndDeletedFalse(@Param("code") String code, @Param("id") Long id);

    /**
     * 检查名称是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(w) > 0 FROM Warehouse w WHERE w.name = :name AND w.id != :id AND w.deleted = false")
    boolean existsByNameAndIdNotAndDeletedFalse(@Param("name") String name, @Param("id") Long id);

    /**
     * 根据用户ID查找可访问的仓库
     */
    @Query("SELECT w FROM Warehouse w JOIN w.managers u WHERE u.id = :userId AND w.deleted = false AND w.enabled = true")
    List<Warehouse> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查找管理的仓库
     */
    @Query("SELECT w FROM Warehouse w JOIN w.managers u WHERE u.id = :userId AND w.deleted = false")
    List<Warehouse> findManagedByUserId(@Param("userId") Long userId);

    /**
     * 分页查询仓库（支持关键字搜索）
     */
    @Query("SELECT w FROM Warehouse w WHERE w.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "w.code LIKE %:keyword% OR w.name LIKE %:keyword% OR w.address LIKE %:keyword%)")
    Page<Warehouse> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询仓库（支持启用状态和关键字搜索）
     */
    @Query("SELECT w FROM Warehouse w WHERE w.deleted = false AND " +
           "(:enabled IS NULL OR w.enabled = :enabled) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "w.code LIKE %:keyword% OR w.name LIKE %:keyword% OR w.address LIKE %:keyword%)")
    Page<Warehouse> findByEnabledAndKeyword(@Param("enabled") Boolean enabled, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据用户ID分页查询可访问的仓库
     */
    @Query("SELECT w FROM Warehouse w JOIN w.managers u WHERE u.id = :userId AND w.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "w.code LIKE %:keyword% OR w.name LIKE %:keyword% OR w.address LIKE %:keyword%)")
    Page<Warehouse> findByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计仓库数量
     */
    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.deleted = false")
    long countNotDeleted();

    /**
     * 统计启用的仓库数量
     */
    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.enabled = true AND w.deleted = false")
    long countEnabledAndNotDeleted();

    /**
     * 统计用户可访问的仓库数量
     */
    @Query("SELECT COUNT(w) FROM Warehouse w JOIN w.managers u WHERE u.id = :userId AND w.deleted = false")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 查找有库存的仓库
     */
    @Query("SELECT DISTINCT w FROM Warehouse w JOIN Inventory i ON w.id = i.warehouse.id WHERE w.deleted = false AND i.quantity > 0")
    List<Warehouse> findWarehousesWithInventory();

    /**
     * 查找指定货物有库存的仓库
     */
    @Query("SELECT DISTINCT w FROM Warehouse w JOIN Inventory i ON w.id = i.warehouse.id WHERE w.deleted = false AND i.goods.id = :goodsId AND i.quantity > 0")
    List<Warehouse> findWarehousesWithGoodsInventory(@Param("goodsId") Long goodsId);

    /**
     * 根据地区查找仓库
     */
    @Query("SELECT w FROM Warehouse w WHERE w.deleted = false AND w.address LIKE %:region%")
    List<Warehouse> findByRegion(@Param("region") String region);

    /**
     * 查找容量大于指定值的仓库
     */
    @Query("SELECT w FROM Warehouse w WHERE w.deleted = false AND w.capacity >= :minCapacity")
    List<Warehouse> findByMinCapacity(@Param("minCapacity") Double minCapacity);

    /**
     * 查找面积大于指定值的仓库
     */
    @Query("SELECT w FROM Warehouse w WHERE w.deleted = false AND w.area >= :minArea")
    List<Warehouse> findByMinArea(@Param("minArea") Double minArea);
}
