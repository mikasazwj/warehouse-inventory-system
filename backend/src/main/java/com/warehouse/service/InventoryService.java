package com.warehouse.service;

import com.warehouse.dto.InventoryDTO;
import com.warehouse.dto.InventoryHistoryDTO;
import com.warehouse.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 库存服务接口
 * 
 * @author Warehouse Team
 */
public interface InventoryService {

    /**
     * 根据ID查找库存
     */
    Optional<InventoryDTO> findById(Long id);

    /**
     * 根据仓库和货物查找库存
     */
    Optional<InventoryDTO> findByWarehouseAndGoods(Long warehouseId, Long goodsId);

    /**
     * 根据仓库查找库存
     */
    List<InventoryDTO> findByWarehouse(Long warehouseId);

    /**
     * 根据货物查找库存
     */
    List<InventoryDTO> findByGoods(Long goodsId);

    /**
     * 查找所有库存
     */
    List<InventoryDTO> findAll();

    /**
     * 查找有库存的记录
     */
    List<InventoryDTO> findWithStock();

    /**
     * 查找低库存记录
     */
    List<InventoryDTO> findLowStock();

    /**
     * 查找即将过期的库存
     */
    List<InventoryDTO> findNearExpiry(int days);

    /**
     * 查找已过期的库存
     */
    List<InventoryDTO> findExpired();

    /**
     * 查找有锁定库存的记录
     */
    List<InventoryDTO> findWithLockedStock();

    /**
     * 分页查询库存
     */
    PageResponse<InventoryDTO> findByPage(String keyword, Pageable pageable);

    /**
     * 带筛选条件的分页查询库存
     */
    PageResponse<InventoryDTO> findByPageWithFilters(String keyword, Long warehouseId,
                                                   Long categoryId, String stockStatus, Pageable pageable);

    /**
     * 分页查询库存（按仓库）
     */
    PageResponse<InventoryDTO> findByWarehouseAndPage(Long warehouseId, String keyword, Pageable pageable);

    /**
     * 分页查询库存（按货物）
     */
    PageResponse<InventoryDTO> findByGoodsAndPage(Long goodsId, String keyword, Pageable pageable);

    /**
     * 库存调整
     */
    InventoryDTO adjustInventory(InventoryDTO.AdjustRequest request);

    /**
     * 库存入库
     */
    InventoryDTO inboundInventory(Long warehouseId, Long goodsId, BigDecimal quantity,
                                 BigDecimal costPrice, LocalDate productionDate, LocalDate expiryDate);

    /**
     * 库存出库
     */
    InventoryDTO outboundInventory(Long warehouseId, Long goodsId, BigDecimal quantity);

    /**
     * 库存锁定
     */
    void lockInventory(InventoryDTO.LockRequest request);

    /**
     * 库存解锁
     */
    void unlockInventory(InventoryDTO.UnlockRequest request);

    /**
     * 检查库存是否充足
     */
    boolean checkStockAvailable(Long warehouseId, Long goodsId, BigDecimal requiredQuantity);

    /**
     * 获取可用库存数量
     */
    BigDecimal getAvailableQuantity(Long warehouseId, Long goodsId);

    /**
     * 获取总库存数量
     */
    BigDecimal getTotalQuantity(Long warehouseId, Long goodsId);

    /**
     * 获取锁定库存数量
     */
    BigDecimal getLockedQuantity(Long warehouseId, Long goodsId);



    /**
     * 根据生产日期范围查找库存
     */
    List<InventoryDTO> findByProductionDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 根据过期日期范围查找库存
     */
    List<InventoryDTO> findByExpiryDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 获取库存统计信息
     */
    InventoryStatistics getInventoryStatistics();

    /**
     * 获取仓库库存统计
     */
    InventoryStatistics getWarehouseInventoryStatistics(Long warehouseId);

    /**
     * 获取货物库存统计
     */
    InventoryStatistics getGoodsInventoryStatistics(Long goodsId);

    /**
     * 库存预警检查
     */
    List<InventoryDTO> checkInventoryAlerts();

    /**
     * 获取库存历史记录
     */
    List<InventoryHistoryDTO> getInventoryHistory(Long inventoryId);

    /**
     * 导出库存数据到Excel
     */
    byte[] exportInventoryToExcel(String keyword, Long warehouseId, Long categoryId, String stockStatus);

    /**
     * 获取有库存记录的货物列表
     */
    List<InventoryDTO.GoodsInfo> getGoodsWithStock();

    /**
     * 获取指定仓库有库存记录的货物列表
     */
    List<InventoryDTO.GoodsInfo> getGoodsWithStock(Long warehouseId);

    /**
     * 获取货物的库存信息和加权平均价
     */
    InventoryDTO.StockInfo getGoodsStockInfo(Long goodsId, Long warehouseId);

    /**
     * 获取指定仓库的库存货物列表（用于盘点选择）
     */
    List<InventoryDTO.GoodsInventoryInfo> getWarehouseGoodsList(Long warehouseId, Long categoryId, boolean onlyWithStock);

    /**
     * 库存统计信息
     */
    class InventoryStatistics {
        private long totalItems;
        private BigDecimal totalQuantity;
        private BigDecimal totalAvailableQuantity;
        private BigDecimal totalLockedQuantity;
        private BigDecimal totalValue;
        private long lowStockItems;
        private long zeroStockItems;
        private long nearExpiryItems;
        private long expiredItems;
        private long warehousesCount;
        private long goodsCount;

        // Constructors
        public InventoryStatistics() {}

        public InventoryStatistics(long totalItems, BigDecimal totalQuantity, BigDecimal totalAvailableQuantity,
                                 BigDecimal totalLockedQuantity, BigDecimal totalValue, long lowStockItems,
                                 long zeroStockItems, long nearExpiryItems, long expiredItems, long warehousesCount, long goodsCount) {
            this.totalItems = totalItems;
            this.totalQuantity = totalQuantity;
            this.totalAvailableQuantity = totalAvailableQuantity;
            this.totalLockedQuantity = totalLockedQuantity;
            this.totalValue = totalValue;
            this.lowStockItems = lowStockItems;
            this.zeroStockItems = zeroStockItems;
            this.nearExpiryItems = nearExpiryItems;
            this.expiredItems = expiredItems;
            this.warehousesCount = warehousesCount;
            this.goodsCount = goodsCount;
        }

        // Getters and Setters
        public long getTotalItems() { return totalItems; }
        public void setTotalItems(long totalItems) { this.totalItems = totalItems; }
        public BigDecimal getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }
        public BigDecimal getTotalAvailableQuantity() { return totalAvailableQuantity; }
        public void setTotalAvailableQuantity(BigDecimal totalAvailableQuantity) { this.totalAvailableQuantity = totalAvailableQuantity; }
        public BigDecimal getTotalLockedQuantity() { return totalLockedQuantity; }
        public void setTotalLockedQuantity(BigDecimal totalLockedQuantity) { this.totalLockedQuantity = totalLockedQuantity; }
        public BigDecimal getTotalValue() { return totalValue; }
        public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
        public long getLowStockItems() { return lowStockItems; }
        public void setLowStockItems(long lowStockItems) { this.lowStockItems = lowStockItems; }
        public long getZeroStockItems() { return zeroStockItems; }
        public void setZeroStockItems(long zeroStockItems) { this.zeroStockItems = zeroStockItems; }
        public long getNearExpiryItems() { return nearExpiryItems; }
        public void setNearExpiryItems(long nearExpiryItems) { this.nearExpiryItems = nearExpiryItems; }
        public long getExpiredItems() { return expiredItems; }
        public void setExpiredItems(long expiredItems) { this.expiredItems = expiredItems; }
        public long getWarehousesCount() { return warehousesCount; }
        public void setWarehousesCount(long warehousesCount) { this.warehousesCount = warehousesCount; }
        public long getGoodsCount() { return goodsCount; }
        public void setGoodsCount(long goodsCount) { this.goodsCount = goodsCount; }
    }
}
