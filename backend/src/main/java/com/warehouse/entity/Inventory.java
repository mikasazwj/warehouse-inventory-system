package com.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 库存实体类
 * 记录每个仓库中每种货物的库存情况
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "inventories", 
    indexes = {
        @Index(name = "idx_inventory_warehouse_goods", columnList = "warehouse_id,goods_id", unique = true),
        @Index(name = "idx_inventory_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_inventory_goods", columnList = "goods_id"),
        @Index(name = "idx_inventory_quantity", columnList = "quantity"),
        @Index(name = "idx_inventory_batch", columnList = "batch_number")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_warehouse_goods_batch", 
                         columnNames = {"warehouse_id", "goods_id", "batch_number"})
    }
)
public class Inventory extends BaseEntity {

    @NotNull(message = "仓库不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @NotNull(message = "货物不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = false)
    private Goods goods;

    @NotNull(message = "库存数量不能为空")
    @DecimalMin(value = "0.0", message = "库存数量不能为负数")
    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "可用数量不能为负数")
    @Column(name = "available_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal availableQuantity = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "锁定数量不能为负数")
    @Column(name = "locked_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal lockedQuantity = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "平均成本不能为负数")
    @Column(name = "average_cost", precision = 15, scale = 4)
    private BigDecimal averageCost = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "最新成本不能为负数")
    @Column(name = "latest_cost", precision = 15, scale = 4)
    private BigDecimal latestCost = BigDecimal.ZERO;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "last_inbound_date")
    private LocalDate lastInboundDate;

    @Column(name = "last_outbound_date")
    private LocalDate lastOutboundDate;

    @Column(name = "remark", length = 500)
    private String remark;

    // Constructors
    public Inventory() {
    }

    public Inventory(Warehouse warehouse, Goods goods) {
        this.warehouse = warehouse;
        this.goods = goods;
        this.quantity = BigDecimal.ZERO;
        this.availableQuantity = BigDecimal.ZERO;
        this.lockedQuantity = BigDecimal.ZERO;
    }

    public Inventory(Warehouse warehouse, Goods goods, BigDecimal quantity) {
        this.warehouse = warehouse;
        this.goods = goods;
        this.quantity = quantity;
        this.availableQuantity = quantity;
        this.lockedQuantity = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
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

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public BigDecimal getLatestCost() {
        return latestCost;
    }

    public void setLatestCost(BigDecimal latestCost) {
        this.latestCost = latestCost;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getLastInboundDate() {
        return lastInboundDate;
    }

    public void setLastInboundDate(LocalDate lastInboundDate) {
        this.lastInboundDate = lastInboundDate;
    }

    public LocalDate getLastOutboundDate() {
        return lastOutboundDate;
    }

    public void setLastOutboundDate(LocalDate lastOutboundDate) {
        this.lastOutboundDate = lastOutboundDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    // Business methods
    
    /**
     * 入库操作
     */
    public void inbound(BigDecimal inboundQuantity, BigDecimal cost) {
        if (inboundQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("入库数量必须大于0");
        }
        
        // 更新平均成本
        if (this.quantity.compareTo(BigDecimal.ZERO) == 0) {
            this.averageCost = cost;
        } else {
            BigDecimal totalCost = this.quantity.multiply(this.averageCost)
                                 .add(inboundQuantity.multiply(cost));
            BigDecimal totalQuantity = this.quantity.add(inboundQuantity);
            this.averageCost = totalCost.divide(totalQuantity, 4, java.math.RoundingMode.HALF_UP);
        }
        
        // 更新数量
        this.quantity = this.quantity.add(inboundQuantity);
        this.availableQuantity = this.availableQuantity.add(inboundQuantity);
        this.latestCost = cost;
        this.lastInboundDate = LocalDate.now();
    }

    /**
     * 出库操作
     */
    public void outbound(BigDecimal outboundQuantity) {
        if (outboundQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("出库数量必须大于0");
        }
        if (outboundQuantity.compareTo(this.availableQuantity) > 0) {
            throw new IllegalArgumentException("出库数量不能大于可用库存");
        }
        
        this.quantity = this.quantity.subtract(outboundQuantity);
        this.availableQuantity = this.availableQuantity.subtract(outboundQuantity);
        this.lastOutboundDate = LocalDate.now();
    }

    /**
     * 锁定库存
     */
    public void lockStock(BigDecimal lockQuantity) {
        if (lockQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("锁定数量必须大于0");
        }
        if (lockQuantity.compareTo(this.availableQuantity) > 0) {
            throw new IllegalArgumentException("锁定数量不能大于可用库存");
        }
        
        this.availableQuantity = this.availableQuantity.subtract(lockQuantity);
        this.lockedQuantity = this.lockedQuantity.add(lockQuantity);
    }

    /**
     * 解锁库存
     */
    public void unlockStock(BigDecimal unlockQuantity) {
        if (unlockQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("解锁数量必须大于0");
        }
        if (unlockQuantity.compareTo(this.lockedQuantity) > 0) {
            throw new IllegalArgumentException("解锁数量不能大于锁定库存");
        }
        
        this.lockedQuantity = this.lockedQuantity.subtract(unlockQuantity);
        this.availableQuantity = this.availableQuantity.add(unlockQuantity);
    }

    /**
     * 判断是否低库存
     */
    public boolean isLowStock() {
        if (goods == null || goods.getMinStock() == null) {
            return false;
        }
        return quantity.compareTo(goods.getMinStock()) <= 0;
    }

    /**
     * 判断是否即将过期（30天内）
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
     * 获取库存总价值
     */
    public BigDecimal getTotalValue() {
        return quantity.multiply(averageCost);
    }

    // ===== 兼容方法 =====

    /**
     * 获取成本价格 (兼容方法)
     */
    public BigDecimal getCostPrice() {
        return this.averageCost;
    }

    /**
     * 设置成本价格 (兼容方法)
     */
    public void setCostPrice(BigDecimal costPrice) {
        this.averageCost = costPrice;
    }

    /**
     * 获取总数量 (兼容方法)
     */
    public Integer getTotalQuantity() {
        return this.quantity != null ? this.quantity.intValue() : 0;
    }

    /**
     * 设置总数量 (兼容方法)
     */
    public void setTotalQuantity(Integer totalQuantity) {
        this.quantity = totalQuantity != null ? BigDecimal.valueOf(totalQuantity) : BigDecimal.ZERO;
    }

    /**
     * 获取可用数量 (兼容方法)
     */
    public Integer getAvailableQuantityInt() {
        return this.availableQuantity != null ? this.availableQuantity.intValue() : 0;
    }

    /**
     * 设置可用数量 (兼容方法)
     */
    public void setAvailableQuantityInt(Integer availableQuantity) {
        this.availableQuantity = availableQuantity != null ? BigDecimal.valueOf(availableQuantity) : BigDecimal.ZERO;
    }

    /**
     * 获取锁定数量 (兼容方法)
     */
    public Integer getLockedQuantityInt() {
        return this.lockedQuantity != null ? this.lockedQuantity.intValue() : 0;
    }

    /**
     * 设置锁定数量 (兼容方法)
     */
    public void setLockedQuantityInt(Integer lockedQuantity) {
        this.lockedQuantity = lockedQuantity != null ? BigDecimal.valueOf(lockedQuantity) : BigDecimal.ZERO;
    }

    /**
     * 设置总价值 (兼容方法)
     * 注意：这个方法不会实际存储值，因为总价值是计算得出的
     */
    public void setTotalValue(BigDecimal totalValue) {
        // 总价值是计算属性，不需要实际存储
        // 这个方法只是为了兼容性而存在
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id=" + getId() +
                ", warehouse=" + (warehouse != null ? warehouse.getName() : null) +
                ", goods=" + (goods != null ? goods.getName() : null) +
                ", quantity=" + quantity +
                ", availableQuantity=" + availableQuantity +
                ", lockedQuantity=" + lockedQuantity +
                '}';
    }
}
