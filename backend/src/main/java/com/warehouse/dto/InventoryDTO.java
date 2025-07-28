package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存DTO
 * 
 * @author Warehouse Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryDTO {

    private Long id;

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private String warehouseName;

    @NotNull(message = "货物不能为空")
    private Long goodsId;

    private String goodsCode;

    private String goodsName;

    private String goodsUnit;

    private String specification;

    private String categoryName;

    @DecimalMin(value = "0.0", message = "数量不能为负数")
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", message = "可用数量不能为负数")
    private BigDecimal availableQuantity;

    @DecimalMin(value = "0.0", message = "锁定数量不能为负数")
    private BigDecimal lockedQuantity;

    @DecimalMin(value = "0.0", message = "成本价不能为负数")
    private BigDecimal costPrice;

    @DecimalMin(value = "0.0", message = "库存价值不能为负数")
    private BigDecimal totalValue;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private String createdBy;

    private String updatedBy;

    /**
     * 库存状态信息
     */
    private InventoryStatus inventoryStatus;

    // 构造函数
    public InventoryDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsUnit() {
        return goodsUnit;
    }

    public void setGoodsUnit(String goodsUnit) {
        this.goodsUnit = goodsUnit;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public BigDecimal getLockedQuantity() {
        return lockedQuantity;
    }

    public void setLockedQuantity(BigDecimal lockedQuantity) {
        this.lockedQuantity = lockedQuantity;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }



    public LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
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

    public InventoryStatus getInventoryStatus() {
        return inventoryStatus;
    }

    public void setInventoryStatus(InventoryStatus inventoryStatus) {
        this.inventoryStatus = inventoryStatus;
    }

    // 业务方法

    /**
     * 判断是否有库存
     */
    public boolean hasStock() {
        return quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否有可用库存
     */
    public boolean hasAvailableStock() {
        return availableQuantity != null && availableQuantity.compareTo(BigDecimal.ZERO) > 0;
    }



    /**
     * 判断是否即将过期
     */
    public boolean isNearExpiry() {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDate.now().plusDays(30));
    }

    /**
     * 判断是否已过期
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDate.now());
    }

    /**
     * 获取剩余保质期天数
     */
    public Long getRemainingShelfLifeDays() {
        if (expiryDate == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    @Override
    public String toString() {
        return "InventoryDTO{" +
                "id=" + id +
                ", warehouseName='" + warehouseName + '\'' +
                ", goodsCode='" + goodsCode + '\'' +
                ", goodsName='" + goodsName + '\'' +
                ", quantity=" + quantity +
                ", availableQuantity=" + availableQuantity +
                '}';
    }

    /**
     * 库存状态信息
     */
    public static class InventoryStatus {
        private Boolean isLowStock;
        private Boolean isHighStock;
        private Boolean isZeroStock;
        private Boolean isNearExpiry;
        private Boolean isExpired;
        private Boolean hasLocked;
        private String statusText;

        // Constructors
        public InventoryStatus() {}

        public InventoryStatus(Boolean isLowStock, Boolean isHighStock, Boolean isZeroStock, Boolean isNearExpiry, Boolean isExpired, Boolean hasLocked) {
            this.isLowStock = isLowStock;
            this.isHighStock = isHighStock;
            this.isZeroStock = isZeroStock;
            this.isNearExpiry = isNearExpiry;
            this.isExpired = isExpired;
            this.hasLocked = hasLocked;
            this.statusText = generateStatusText();
        }

        // Getters and Setters
        public Boolean getIsLowStock() { return isLowStock; }
        public void setIsLowStock(Boolean isLowStock) { this.isLowStock = isLowStock; }
        public Boolean getIsHighStock() { return isHighStock; }
        public void setIsHighStock(Boolean isHighStock) { this.isHighStock = isHighStock; }
        public Boolean getIsZeroStock() { return isZeroStock; }
        public void setIsZeroStock(Boolean isZeroStock) { this.isZeroStock = isZeroStock; }
        public Boolean getIsNearExpiry() { return isNearExpiry; }
        public void setIsNearExpiry(Boolean isNearExpiry) { this.isNearExpiry = isNearExpiry; }
        public Boolean getIsExpired() { return isExpired; }
        public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }
        public Boolean getHasLocked() { return hasLocked; }
        public void setHasLocked(Boolean hasLocked) { this.hasLocked = hasLocked; }
        public String getStatusText() { return statusText; }
        public void setStatusText(String statusText) { this.statusText = statusText; }

        private String generateStatusText() {
            if (Boolean.TRUE.equals(isZeroStock)) {
                return "零库存";
            }
            if (Boolean.TRUE.equals(isExpired)) {
                return "已过期";
            }
            if (Boolean.TRUE.equals(isNearExpiry)) {
                return "即将过期";
            }
            if (Boolean.TRUE.equals(isHighStock)) {
                return "库存超量";
            }
            if (Boolean.TRUE.equals(isLowStock)) {
                return "库存不足";
            }
            if (Boolean.TRUE.equals(hasLocked)) {
                return "有锁定";
            }
            return "正常";
        }
    }

    /**
     * 库存调整请求DTO
     */
    public static class AdjustRequest {
        @NotNull(message = "仓库不能为空")
        private Long warehouseId;

        @NotNull(message = "货物不能为空")
        private Long goodsId;

        private BigDecimal adjustQuantity;

        private BigDecimal setQuantity;

        private BigDecimal costPrice;
        private LocalDate productionDate;
        private LocalDate expiryDate;
        private String reason;

        // Getters and Setters
        public Long getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
        public Long getGoodsId() { return goodsId; }
        public void setGoodsId(Long goodsId) { this.goodsId = goodsId; }
        public BigDecimal getAdjustQuantity() { return adjustQuantity; }
        public void setAdjustQuantity(BigDecimal adjustQuantity) { this.adjustQuantity = adjustQuantity; }
        public BigDecimal getSetQuantity() { return setQuantity; }
        public void setSetQuantity(BigDecimal setQuantity) { this.setQuantity = setQuantity; }
        public BigDecimal getCostPrice() { return costPrice; }
        public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
        public LocalDate getProductionDate() { return productionDate; }
        public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * 库存锁定请求DTO
     */
    public static class LockRequest {
        @NotNull(message = "库存ID不能为空")
        private Long inventoryId;

        @NotNull(message = "锁定数量不能为空")
        @DecimalMin(value = "0.001", message = "锁定数量必须大于0")
        private BigDecimal lockQuantity;

        private String reason;

        // Getters and Setters
        public Long getInventoryId() { return inventoryId; }
        public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
        public BigDecimal getLockQuantity() { return lockQuantity; }
        public void setLockQuantity(BigDecimal lockQuantity) { this.lockQuantity = lockQuantity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * 库存解锁请求DTO
     */
    public static class UnlockRequest {
        @NotNull(message = "库存ID不能为空")
        private Long inventoryId;

        @NotNull(message = "解锁数量不能为空")
        @DecimalMin(value = "0.001", message = "解锁数量必须大于0")
        private BigDecimal unlockQuantity;

        private String reason;

        // Getters and Setters
        public Long getInventoryId() { return inventoryId; }
        public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
        public BigDecimal getUnlockQuantity() { return unlockQuantity; }
        public void setUnlockQuantity(BigDecimal unlockQuantity) { this.unlockQuantity = unlockQuantity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * 货物信息DTO（用于下拉选择）
     */
    public static class GoodsInfo {
        private Long id;
        private String code;
        private String name;
        private String specification;
        private String model;
        private String unit;

        public GoodsInfo() {}

        public GoodsInfo(Long id, String code, String name, String unit) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.unit = unit;
        }

        public GoodsInfo(Long id, String code, String name, String specification, String model, String unit) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.specification = specification;
            this.model = model;
            this.unit = unit;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSpecification() { return specification; }
        public void setSpecification(String specification) { this.specification = specification; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    /**
     * 库存信息DTO（用于出库选择）
     */
    public static class StockInfo {
        private BigDecimal availableStock;
        private BigDecimal weightedAveragePrice;
        private String unit;

        public StockInfo() {}

        public StockInfo(BigDecimal availableStock, BigDecimal weightedAveragePrice, String unit) {
            this.availableStock = availableStock;
            this.weightedAveragePrice = weightedAveragePrice;
            this.unit = unit;
        }

        public BigDecimal getAvailableStock() { return availableStock; }
        public void setAvailableStock(BigDecimal availableStock) { this.availableStock = availableStock; }
        public BigDecimal getWeightedAveragePrice() { return weightedAveragePrice; }
        public void setWeightedAveragePrice(BigDecimal weightedAveragePrice) { this.weightedAveragePrice = weightedAveragePrice; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    /**
     * 货物库存信息DTO（用于盘点选择）
     */
    public static class GoodsInventoryInfo {
        private Long id;
        private String code;
        private String name;
        private String specification;
        private String model;
        private String brand;
        private String unit;
        private String categoryName;
        private BigDecimal currentStock;
        private BigDecimal availableStock;

        public GoodsInventoryInfo() {}

        public GoodsInventoryInfo(Long id, String code, String name, String specification, String model,
                                String brand, String unit, String categoryName, BigDecimal currentStock,
                                BigDecimal availableStock) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.specification = specification;
            this.model = model;
            this.brand = brand;
            this.unit = unit;
            this.categoryName = categoryName;
            this.currentStock = currentStock;
            this.availableStock = availableStock;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSpecification() { return specification; }
        public void setSpecification(String specification) { this.specification = specification; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public BigDecimal getCurrentStock() { return currentStock; }
        public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }
        public BigDecimal getAvailableStock() { return availableStock; }
        public void setAvailableStock(BigDecimal availableStock) { this.availableStock = availableStock; }
    }

    /**
     * 导出请求DTO
     */
    public static class ExportRequest {
        private String keyword;
        private Long warehouseId;
        private Long categoryId;
        private String stockStatus;

        public ExportRequest() {}

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        public Long getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getStockStatus() { return stockStatus; }
        public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
    }
}
