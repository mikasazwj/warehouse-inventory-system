package com.warehouse.repository;

import com.warehouse.entity.GoodsCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 货物分类数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface GoodsCategoryRepository extends JpaRepository<GoodsCategory, Long> {

    /**
     * 根据编码查找分类
     */
    Optional<GoodsCategory> findByCodeAndDeletedFalse(String code);

    /**
     * 根据名称查找分类
     */
    Optional<GoodsCategory> findByNameAndDeletedFalse(String name);

    /**
     * 查找所有启用的分类
     */
    List<GoodsCategory> findByEnabledTrueAndDeletedFalseOrderBySortOrderAscNameAsc();

    /**
     * 查找所有未删除的分类
     */
    List<GoodsCategory> findByDeletedFalseOrderBySortOrderAscNameAsc();

    /**
     * 查找根分类
     */
    List<GoodsCategory> findByParentIsNullAndDeletedFalseOrderBySortOrderAscNameAsc();

    /**
     * 查找启用的根分类
     */
    List<GoodsCategory> findByParentIsNullAndEnabledTrueAndDeletedFalseOrderBySortOrderAscNameAsc();

    /**
     * 根据父分类查找子分类
     */
    List<GoodsCategory> findByParentIdAndDeletedFalseOrderBySortOrderAscNameAsc(Long parentId);

    /**
     * 根据父分类查找启用的子分类
     */
    List<GoodsCategory> findByParentIdAndEnabledTrueAndDeletedFalseOrderBySortOrderAscNameAsc(Long parentId);

    /**
     * 根据层级查找分类
     */
    List<GoodsCategory> findByLevelAndDeletedFalseOrderBySortOrderAscNameAsc(Integer level);

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
    @Query("SELECT COUNT(c) > 0 FROM GoodsCategory c WHERE c.code = :code AND c.id != :id AND c.deleted = false")
    boolean existsByCodeAndIdNotAndDeletedFalse(@Param("code") String code, @Param("id") Long id);

    /**
     * 检查名称是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(c) > 0 FROM GoodsCategory c WHERE c.name = :name AND c.id != :id AND c.deleted = false")
    boolean existsByNameAndIdNotAndDeletedFalse(@Param("name") String name, @Param("id") Long id);

    /**
     * 检查是否存在子分类
     */
    @Query("SELECT COUNT(c) > 0 FROM GoodsCategory c WHERE c.parent.id = :parentId AND c.deleted = false")
    boolean existsByParentIdAndDeletedFalse(@Param("parentId") Long parentId);

    /**
     * 分页查询分类（支持关键字搜索）
     */
    @Query("SELECT c FROM GoodsCategory c WHERE c.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "c.code LIKE %:keyword% OR c.name LIKE %:keyword% OR c.description LIKE %:keyword%)")
    Page<GoodsCategory> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询分类（支持父分类和关键字搜索）
     */
    @Query("SELECT c FROM GoodsCategory c WHERE c.deleted = false AND " +
           "(:parentId IS NULL OR c.parent.id = :parentId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "c.code LIKE %:keyword% OR c.name LIKE %:keyword% OR c.description LIKE %:keyword%)")
    Page<GoodsCategory> findByParentAndKeyword(@Param("parentId") Long parentId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询分类（支持启用状态和关键字搜索）
     */
    @Query("SELECT c FROM GoodsCategory c WHERE c.deleted = false AND " +
           "(:enabled IS NULL OR c.enabled = :enabled) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "c.code LIKE %:keyword% OR c.name LIKE %:keyword% OR c.description LIKE %:keyword%)")
    Page<GoodsCategory> findByEnabledAndKeyword(@Param("enabled") Boolean enabled, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计分类数量
     */
    @Query("SELECT COUNT(c) FROM GoodsCategory c WHERE c.deleted = false")
    long countNotDeleted();

    /**
     * 统计启用的分类数量
     */
    @Query("SELECT COUNT(c) FROM GoodsCategory c WHERE c.enabled = true AND c.deleted = false")
    long countEnabledAndNotDeleted();

    /**
     * 根据层级统计分类数量
     */
    @Query("SELECT COUNT(c) FROM GoodsCategory c WHERE c.level = :level AND c.deleted = false")
    long countByLevelAndNotDeleted(@Param("level") Integer level);

    /**
     * 统计根分类数量
     */
    @Query("SELECT COUNT(c) FROM GoodsCategory c WHERE c.parent IS NULL AND c.deleted = false")
    long countRootCategories();

    /**
     * 根据父分类统计子分类数量
     */
    @Query("SELECT COUNT(c) FROM GoodsCategory c WHERE c.parent.id = :parentId AND c.deleted = false")
    long countByParentAndNotDeleted(@Param("parentId") Long parentId);

    /**
     * 查找指定分类的所有祖先分类
     */
    @Query("SELECT c FROM GoodsCategory c WHERE c.deleted = false AND :categoryPath LIKE CONCAT(c.path, c.id, '/%') ORDER BY c.level")
    List<GoodsCategory> findAncestors(@Param("categoryPath") String categoryPath);

    /**
     * 查找指定分类的所有后代分类
     */
    @Query("SELECT c FROM GoodsCategory c WHERE c.deleted = false AND c.path LIKE :pathPattern ORDER BY c.level, c.sortOrder, c.name")
    List<GoodsCategory> findDescendants(@Param("pathPattern") String pathPattern);

    /**
     * 查找叶子分类（没有子分类的分类）
     */
    @Query("SELECT c FROM GoodsCategory c WHERE c.deleted = false AND c.id NOT IN " +
           "(SELECT DISTINCT p.id FROM GoodsCategory p WHERE p.deleted = false AND EXISTS " +
           "(SELECT 1 FROM GoodsCategory child WHERE child.parent.id = p.id AND child.deleted = false))")
    List<GoodsCategory> findLeafCategories();

    /**
     * 查找有货物的分类
     */
    @Query("SELECT DISTINCT c FROM GoodsCategory c JOIN Goods g ON c.id = g.category.id WHERE c.deleted = false AND g.deleted = false")
    List<GoodsCategory> findCategoriesWithGoods();

    /**
     * 根据路径模式查找分类
     */
    @Query("SELECT c FROM GoodsCategory c WHERE c.deleted = false AND c.path LIKE :pathPattern ORDER BY c.level, c.sortOrder, c.name")
    List<GoodsCategory> findByPathPattern(@Param("pathPattern") String pathPattern);

    /**
     * 查找最大排序号
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM GoodsCategory c WHERE c.parent.id = :parentId AND c.deleted = false")
    Integer findMaxSortOrderByParent(@Param("parentId") Long parentId);

    /**
     * 统计分类数量
     */
    long countByDeletedFalse();

    /**
     * 统计启用的分类数量
     */
    long countByEnabledTrueAndDeletedFalse();

    /**
     * 统计根分类数量
     */
    long countByParentIsNullAndDeletedFalse();

    /**
     * 查找根分类的最大排序号
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM GoodsCategory c WHERE c.parent IS NULL AND c.deleted = false")
    Integer findMaxSortOrderForRoot();

    /**
     * 查找最近创建的分类
     */
    @Query("SELECT c FROM GoodsCategory c WHERE c.deleted = false ORDER BY c.createdTime DESC")
    List<GoodsCategory> findRecentCategories(Pageable pageable);

    /**
     * 查找最近更新的分类
     */
    @Query("SELECT c FROM GoodsCategory c WHERE c.deleted = false ORDER BY c.updatedTime DESC")
    List<GoodsCategory> findRecentUpdatedCategories(Pageable pageable);
}
