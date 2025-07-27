package com.warehouse.service;

import com.warehouse.dto.GoodsCategoryDTO;
import com.warehouse.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 商品分类服务接口
 * 
 * @author Warehouse Team
 */
public interface GoodsCategoryService {

    /**
     * 创建分类
     */
    GoodsCategoryDTO createCategory(GoodsCategoryDTO.CreateRequest request);

    /**
     * 更新分类
     */
    GoodsCategoryDTO updateCategory(Long id, GoodsCategoryDTO.UpdateRequest request);

    /**
     * 删除分类（软删除）
     */
    void deleteCategory(Long id);

    /**
     * 根据ID查找分类
     */
    Optional<GoodsCategoryDTO> findById(Long id);

    /**
     * 根据编码查找分类
     */
    Optional<GoodsCategoryDTO> findByCode(String code);

    /**
     * 根据名称查找分类
     */
    Optional<GoodsCategoryDTO> findByName(String name);

    /**
     * 查找所有分类
     */
    List<GoodsCategoryDTO> findAll();

    /**
     * 查找所有启用的分类
     */
    List<GoodsCategoryDTO> findAllEnabled();

    /**
     * 查找根分类
     */
    List<GoodsCategoryDTO> findRootCategories();

    /**
     * 查找启用的根分类
     */
    List<GoodsCategoryDTO> findEnabledRootCategories();

    /**
     * 根据父分类查找子分类
     */
    List<GoodsCategoryDTO> findByParent(Long parentId);

    /**
     * 根据父分类查找启用的子分类
     */
    List<GoodsCategoryDTO> findEnabledByParent(Long parentId);

    /**
     * 获取分类树
     */
    List<GoodsCategoryDTO> getCategoryTree();

    /**
     * 获取启用的分类树
     */
    List<GoodsCategoryDTO> getEnabledCategoryTree();

    /**
     * 分页查询分类
     */
    PageResponse<GoodsCategoryDTO> findByPage(String keyword, Pageable pageable);

    /**
     * 分页查询分类（按父分类）
     */
    PageResponse<GoodsCategoryDTO> findByParentAndPage(Long parentId, String keyword, Pageable pageable);

    /**
     * 启用分类
     */
    void enableCategory(Long id);

    /**
     * 禁用分类
     */
    void disableCategory(Long id);

    /**
     * 检查编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查编码是否存在（排除指定ID）
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 检查名称是否存在（排除指定ID）
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 检查分类是否有子分类
     */
    boolean hasChildren(Long id);

    /**
     * 检查分类是否有商品
     */
    boolean hasGoods(Long id);

    /**
     * 获取分类统计信息
     */
    CategoryStatistics getCategoryStatistics();

    /**
     * 分类统计信息
     */
    class CategoryStatistics {
        private long totalCategories;
        private long enabledCategories;
        private long rootCategories;
        private long categoriesWithGoods;

        // Constructors
        public CategoryStatistics() {}

        public CategoryStatistics(long totalCategories, long enabledCategories, long rootCategories, long categoriesWithGoods) {
            this.totalCategories = totalCategories;
            this.enabledCategories = enabledCategories;
            this.rootCategories = rootCategories;
            this.categoriesWithGoods = categoriesWithGoods;
        }

        // Getters and Setters
        public long getTotalCategories() { return totalCategories; }
        public void setTotalCategories(long totalCategories) { this.totalCategories = totalCategories; }

        public long getEnabledCategories() { return enabledCategories; }
        public void setEnabledCategories(long enabledCategories) { this.enabledCategories = enabledCategories; }

        public long getRootCategories() { return rootCategories; }
        public void setRootCategories(long rootCategories) { this.rootCategories = rootCategories; }

        public long getCategoriesWithGoods() { return categoriesWithGoods; }
        public void setCategoriesWithGoods(long categoriesWithGoods) { this.categoriesWithGoods = categoriesWithGoods; }
    }
}
