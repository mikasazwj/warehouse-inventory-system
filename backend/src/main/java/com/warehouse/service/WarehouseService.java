package com.warehouse.service;

import com.warehouse.dto.PageResponse;
import com.warehouse.dto.WarehouseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 仓库服务接口
 * 
 * @author Warehouse Team
 */
public interface WarehouseService {

    /**
     * 创建仓库
     */
    WarehouseDTO createWarehouse(WarehouseDTO.CreateRequest request);

    /**
     * 更新仓库
     */
    WarehouseDTO updateWarehouse(Long id, WarehouseDTO.UpdateRequest request);

    /**
     * 删除仓库（软删除）
     */
    void deleteWarehouse(Long id);

    /**
     * 根据ID查找仓库
     */
    Optional<WarehouseDTO> findById(Long id);

    /**
     * 根据编码查找仓库
     */
    Optional<WarehouseDTO> findByCode(String code);

    /**
     * 根据名称查找仓库
     */
    Optional<WarehouseDTO> findByName(String name);

    /**
     * 查找所有仓库
     */
    List<WarehouseDTO> findAll();

    /**
     * 查找所有启用的仓库
     */
    List<WarehouseDTO> findAllEnabled();

    /**
     * 根据用户ID查找可访问的仓库
     */
    List<WarehouseDTO> findByUserId(Long userId);

    /**
     * 根据用户ID查找管理的仓库
     */
    List<WarehouseDTO> findManagedByUserId(Long userId);

    /**
     * 分页查询仓库
     */
    PageResponse<WarehouseDTO> findByPage(String keyword, Boolean enabled, Pageable pageable);

    /**
     * 分页查询仓库（按启用状态）
     */
    PageResponse<WarehouseDTO> findByEnabledAndPage(Boolean enabled, String keyword, Pageable pageable);

    /**
     * 根据用户ID分页查询可访问的仓库
     */
    PageResponse<WarehouseDTO> findByUserIdAndPage(Long userId, String keyword, Pageable pageable);

    /**
     * 启用仓库
     */
    void enableWarehouse(Long id);

    /**
     * 禁用仓库
     */
    void disableWarehouse(Long id);

    /**
     * 切换仓库状态
     */
    void toggleStatus(Long id);

    /**
     * 检查仓库是否由指定用户管理
     */
    boolean isWarehouseManagedByUser(Long warehouseId, Long userId);

    /**
     * 分配管理员
     */
    void assignManagers(Long warehouseId, List<Long> userIds);

    /**
     * 移除管理员
     */
    void removeManagers(Long warehouseId, List<Long> userIds);

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
     * 查找有库存的仓库
     */
    List<WarehouseDTO> findWarehousesWithInventory();

    /**
     * 查找指定货物有库存的仓库
     */
    List<WarehouseDTO> findWarehousesWithGoodsInventory(Long goodsId);

    /**
     * 根据地区查找仓库
     */
    List<WarehouseDTO> findByRegion(String region);

    /**
     * 查找容量大于指定值的仓库
     */
    List<WarehouseDTO> findByMinCapacity(Double minCapacity);

    /**
     * 查找面积大于指定值的仓库
     */
    List<WarehouseDTO> findByMinArea(Double minArea);

    /**
     * 获取仓库统计信息
     */
    WarehouseStatistics getWarehouseStatistics();

    /**
     * 导出仓库数据到Excel
     */
    byte[] exportWarehousesToExcel(String keyword, Boolean enabled);

    /**
     * 获取仓库库存统计
     */
    WarehouseDTO.InventoryStatistics getWarehouseInventoryStatistics(Long warehouseId);

    /**
     * 仓库统计信息
     */
    class WarehouseStatistics {
        private long totalWarehouses;
        private long enabledWarehouses;
        private long warehousesWithInventory;
        private double totalArea;
        private double totalCapacity;
        private double averageCapacityUtilization;

        // Constructors
        public WarehouseStatistics() {}

        public WarehouseStatistics(long totalWarehouses, long enabledWarehouses, long warehousesWithInventory,
                                 double totalArea, double totalCapacity, double averageCapacityUtilization) {
            this.totalWarehouses = totalWarehouses;
            this.enabledWarehouses = enabledWarehouses;
            this.warehousesWithInventory = warehousesWithInventory;
            this.totalArea = totalArea;
            this.totalCapacity = totalCapacity;
            this.averageCapacityUtilization = averageCapacityUtilization;
        }

        // Getters and Setters
        public long getTotalWarehouses() { return totalWarehouses; }
        public void setTotalWarehouses(long totalWarehouses) { this.totalWarehouses = totalWarehouses; }
        public long getEnabledWarehouses() { return enabledWarehouses; }
        public void setEnabledWarehouses(long enabledWarehouses) { this.enabledWarehouses = enabledWarehouses; }
        public long getWarehousesWithInventory() { return warehousesWithInventory; }
        public void setWarehousesWithInventory(long warehousesWithInventory) { this.warehousesWithInventory = warehousesWithInventory; }
        public double getTotalArea() { return totalArea; }
        public void setTotalArea(double totalArea) { this.totalArea = totalArea; }
        public double getTotalCapacity() { return totalCapacity; }
        public void setTotalCapacity(double totalCapacity) { this.totalCapacity = totalCapacity; }
        public double getAverageCapacityUtilization() { return averageCapacityUtilization; }
        public void setAverageCapacityUtilization(double averageCapacityUtilization) { this.averageCapacityUtilization = averageCapacityUtilization; }
    }
}
