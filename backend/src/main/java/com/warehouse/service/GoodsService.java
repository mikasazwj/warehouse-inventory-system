package com.warehouse.service;

import com.warehouse.dto.GoodsDTO;
import com.warehouse.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 货物服务接口
 * 
 * @author Warehouse Team
 */
public interface GoodsService {

    /**
     * 创建货物
     */
    GoodsDTO createGoods(GoodsDTO.CreateRequest request);

    /**
     * 更新货物
     */
    GoodsDTO updateGoods(Long id, GoodsDTO.UpdateRequest request);

    /**
     * 删除货物（软删除）
     */
    void deleteGoods(Long id);

    /**
     * 切换货物启用/禁用状态
     */
    void toggleStatus(Long id);

    /**
     * 获取所有启用的货物列表
     */
    List<GoodsDTO> findAllEnabled();

    /**
     * 根据ID查找货物
     */
    Optional<GoodsDTO> findById(Long id);

    /**
     * 根据编码查找货物
     */
    Optional<GoodsDTO> findByCode(String code);

    /**
     * 根据条形码查找货物
     */
    Optional<GoodsDTO> findByBarcode(String barcode);

    /**
     * 查找所有货物
     */
    List<GoodsDTO> findAll();



    /**
     * 根据分类查找货物
     */
    List<GoodsDTO> findByCategory(Long categoryId);



    /**
     * 分页查询货物
     */
    PageResponse<GoodsDTO> findByPage(String keyword, Long categoryId, Boolean enabled, Pageable pageable);

    /**
     * 分页查询货物（按分类）
     */
    PageResponse<GoodsDTO> findByCategoryAndPage(Long categoryId, String keyword, Pageable pageable);



    /**
     * 分页查询货物（按启用状态）
     */
    PageResponse<GoodsDTO> findByEnabledAndPage(Boolean enabled, String keyword, Pageable pageable);

    /**
     * 启用货物
     */
    void enableGoods(Long id);

    /**
     * 禁用货物
     */
    void disableGoods(Long id);

    /**
     * 检查编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查条形码是否存在
     */
    boolean existsByBarcode(String barcode);

    /**
     * 检查编码是否存在（排除指定ID）
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 检查条形码是否存在（排除指定ID）
     */
    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    /**
     * 查找有库存的货物
     */
    List<GoodsDTO> findGoodsWithInventory();

    /**
     * 查找指定仓库有库存的货物
     */
    List<GoodsDTO> findGoodsWithInventoryInWarehouse(Long warehouseId);

    /**
     * 查找低库存货物
     */
    List<GoodsDTO> findLowStockGoods();

    /**
     * 根据品牌查找货物
     */
    List<GoodsDTO> findByBrand(String brand);

    /**
     * 根据型号查找货物
     */
    List<GoodsDTO> findByModel(String model);

    /**
     * 查找有保质期的货物
     */
    List<GoodsDTO> findGoodsWithShelfLife();

    /**
     * 查找设置了最低库存的货物
     */
    List<GoodsDTO> findGoodsWithMinStock();

    /**
     * 获取货物统计信息
     */
    GoodsStatistics getGoodsStatistics();

    /**
     * 获取货物库存统计
     */
    GoodsDTO.InventoryStatistics getGoodsInventoryStatistics(Long goodsId);

    /**
     * 货物统计信息
     */
    class GoodsStatistics {
        private long totalGoods;
        private long enabledGoods;
        private long goodsWithInventory;
        private long lowStockGoods;
        private long goodsWithShelfLife;
        private long categoriesCount;


        // Constructors
        public GoodsStatistics() {}

        public GoodsStatistics(long totalGoods, long enabledGoods, long goodsWithInventory,
                             long lowStockGoods, long goodsWithShelfLife, long categoriesCount, long suppliersCount) {
            this.totalGoods = totalGoods;
            this.enabledGoods = enabledGoods;
            this.goodsWithInventory = goodsWithInventory;
            this.lowStockGoods = lowStockGoods;
            this.goodsWithShelfLife = goodsWithShelfLife;
            this.categoriesCount = categoriesCount;
        }

        // Getters and Setters
        public long getTotalGoods() { return totalGoods; }
        public void setTotalGoods(long totalGoods) { this.totalGoods = totalGoods; }
        public long getEnabledGoods() { return enabledGoods; }
        public void setEnabledGoods(long enabledGoods) { this.enabledGoods = enabledGoods; }
        public long getGoodsWithInventory() { return goodsWithInventory; }
        public void setGoodsWithInventory(long goodsWithInventory) { this.goodsWithInventory = goodsWithInventory; }
        public long getLowStockGoods() { return lowStockGoods; }
        public void setLowStockGoods(long lowStockGoods) { this.lowStockGoods = lowStockGoods; }
        public long getGoodsWithShelfLife() { return goodsWithShelfLife; }
        public void setGoodsWithShelfLife(long goodsWithShelfLife) { this.goodsWithShelfLife = goodsWithShelfLife; }
        public long getCategoriesCount() { return categoriesCount; }
        public void setCategoriesCount(long categoriesCount) { this.categoriesCount = categoriesCount; }

    }
}
