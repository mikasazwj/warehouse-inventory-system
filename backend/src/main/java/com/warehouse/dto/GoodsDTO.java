package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货物DTO
 * 
 * @author Warehouse Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoodsDTO {

    private Long id;

    @NotBlank(message = "货物编码不能为空")
    @Size(max = 50, message = "货物编码长度不能超过50个字符")
    private String code;

    @NotBlank(message = "货物名称不能为空")
    @Size(max = 200, message = "货物名称长度不能超过200个字符")
    private String name;

    @Size(max = 100, message = "货物简称长度不能超过100个字符")
    private String shortName;

    @Size(max = 100, message = "型号长度不能超过100个字符")
    private String model;

    @Size(max = 500, message = "规格长度不能超过500个字符")
    private String specification;

    @Size(max = 100, message = "品牌长度不能超过100个字符")
    private String brand;

    @Size(max = 100, message = "条形码长度不能超过100个字符")
    private String barcode;

    @NotNull(message = "货物分类不能为空")
    private Long categoryId;

    private String categoryName;



    @NotBlank(message = "计量单位不能为空")
    @Size(max = 20, message = "计量单位长度不能超过20个字符")
    private String unit;

    @DecimalMin(value = "0.0", message = "重量不能为负数")
    private Double weight;

    @DecimalMin(value = "0.0", message = "体积不能为负数")
    private Double volume;



    @DecimalMin(value = "0.0", message = "最低库存不能为负数")
    private BigDecimal minStock;

    @DecimalMin(value = "0.0", message = "最高库存不能为负数")
    private BigDecimal maxStock;

    @DecimalMin(value = "0.0", message = "安全库存不能为负数")
    private BigDecimal safetyStock;

    private Integer shelfLifeDays;

    private String storageConditions;

    private Boolean enabled;

    private String remark;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private String createdBy;

    private String updatedBy;

    /**
     * 库存统计信息
     */
    private InventoryStatistics inventoryStatistics;

    // 构造函数
    public GoodsDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }



    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }



    public BigDecimal getMinStock() {
        return minStock;
    }

    public void setMinStock(BigDecimal minStock) {
        this.minStock = minStock;
    }

    public BigDecimal getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(BigDecimal maxStock) {
        this.maxStock = maxStock;
    }

    public BigDecimal getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(BigDecimal safetyStock) {
        this.safetyStock = safetyStock;
    }

    public Integer getShelfLifeDays() {
        return shelfLifeDays;
    }

    public void setShelfLifeDays(Integer shelfLifeDays) {
        this.shelfLifeDays = shelfLifeDays;
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public void setStorageConditions(String storageConditions) {
        this.storageConditions = storageConditions;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public InventoryStatistics getInventoryStatistics() {
        return inventoryStatistics;
    }

    public void setInventoryStatistics(InventoryStatistics inventoryStatistics) {
        this.inventoryStatistics = inventoryStatistics;
    }

    // 业务方法

    /**
     * 获取状态显示文本
     */
    public String getStatusText() {
        return enabled != null && enabled ? "启用" : "禁用";
    }

    /**
     * 判断是否有保质期
     */
    public boolean hasShelfLife() {
        return shelfLifeDays != null && shelfLifeDays > 0;
    }

    /**
     * 判断是否设置了库存预警
     */
    public boolean hasStockAlert() {
        return minStock != null && minStock.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return "GoodsDTO{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", enabled=" + enabled +
                '}';
    }

    /**
     * 库存统计信息
     */
    public static class InventoryStatistics {
        private BigDecimal totalQuantity;
        private BigDecimal availableQuantity;
        private BigDecimal lockedQuantity;
        private BigDecimal totalValue;
        private Integer warehouseCount;
        private Boolean isLowStock;

        // Constructors
        public InventoryStatistics() {}

        // Getters and Setters
        public BigDecimal getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }
        public BigDecimal getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }
        public BigDecimal getLockedQuantity() { return lockedQuantity; }
        public void setLockedQuantity(BigDecimal lockedQuantity) { this.lockedQuantity = lockedQuantity; }
        public BigDecimal getTotalValue() { return totalValue; }
        public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
        public Integer getWarehouseCount() { return warehouseCount; }
        public void setWarehouseCount(Integer warehouseCount) { this.warehouseCount = warehouseCount; }
        public Boolean getIsLowStock() { return isLowStock; }
        public void setIsLowStock(Boolean isLowStock) { this.isLowStock = isLowStock; }
    }

    /**
     * 货物创建请求DTO
     */
    public static class CreateRequest {
        @NotBlank(message = "货物编码不能为空")
        private String code;
        @NotBlank(message = "货物名称不能为空")
        private String name;
        private String shortName;
        private String model;
        private String specification;
        private String brand;
        private String barcode;
        @NotNull(message = "货物分类不能为空")
        private Long categoryId;

        @NotBlank(message = "计量单位不能为空")
        private String unit;
        private Double weight;
        private Double volume;

        private BigDecimal minStock;
        private BigDecimal maxStock;
        private BigDecimal safetyStock;
        private Integer shelfLifeDays;
        private String storageConditions;
        private String remark;

        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getShortName() { return shortName; }
        public void setShortName(String shortName) { this.shortName = shortName; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getSpecification() { return specification; }
        public void setSpecification(String specification) { this.specification = specification; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
        public Double getVolume() { return volume; }
        public void setVolume(Double volume) { this.volume = volume; }

        public BigDecimal getMinStock() { return minStock; }
        public void setMinStock(BigDecimal minStock) { this.minStock = minStock; }
        public BigDecimal getMaxStock() { return maxStock; }
        public void setMaxStock(BigDecimal maxStock) { this.maxStock = maxStock; }
        public BigDecimal getSafetyStock() { return safetyStock; }
        public void setSafetyStock(BigDecimal safetyStock) { this.safetyStock = safetyStock; }
        public Integer getShelfLifeDays() { return shelfLifeDays; }
        public void setShelfLifeDays(Integer shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; }
        public String getStorageConditions() { return storageConditions; }
        public void setStorageConditions(String storageConditions) { this.storageConditions = storageConditions; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    /**
     * 货物更新请求DTO
     */
    public static class UpdateRequest {
        @NotBlank(message = "货物名称不能为空")
        private String name;
        private String shortName;
        private String model;
        private String specification;
        private String brand;
        private String barcode;
        private Long categoryId;

        private String unit;
        private Double weight;
        private Double volume;

        private BigDecimal minStock;
        private BigDecimal maxStock;
        private BigDecimal safetyStock;
        private Integer shelfLifeDays;
        private String storageConditions;
        private Boolean enabled;
        private String remark;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getShortName() { return shortName; }
        public void setShortName(String shortName) { this.shortName = shortName; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public String getSpecification() { return specification; }
        public void setSpecification(String specification) { this.specification = specification; }

        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }



        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }

        public Double getVolume() { return volume; }
        public void setVolume(Double volume) { this.volume = volume; }



        public BigDecimal getMinStock() { return minStock; }
        public void setMinStock(BigDecimal minStock) { this.minStock = minStock; }

        public BigDecimal getMaxStock() { return maxStock; }
        public void setMaxStock(BigDecimal maxStock) { this.maxStock = maxStock; }

        public BigDecimal getSafetyStock() { return safetyStock; }
        public void setSafetyStock(BigDecimal safetyStock) { this.safetyStock = safetyStock; }

        public Integer getShelfLifeDays() { return shelfLifeDays; }
        public void setShelfLifeDays(Integer shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; }

        public String getStorageConditions() { return storageConditions; }
        public void setStorageConditions(String storageConditions) { this.storageConditions = storageConditions; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}
