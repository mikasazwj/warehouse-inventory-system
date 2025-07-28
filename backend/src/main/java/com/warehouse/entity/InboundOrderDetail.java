package com.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 入库单明细实体类
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "inbound_order_details", indexes = {
    @Index(name = "idx_inbound_detail_order", columnList = "inbound_order_id"),
    @Index(name = "idx_inbound_detail_goods", columnList = "goods_id")
})
public class InboundOrderDetail extends BaseEntity {

    @NotNull(message = "入库单不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_order_id", nullable = false)
    private InboundOrder inboundOrder;

    @NotNull(message = "货物不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = false)
    private Goods goods;

    @NotNull(message = "计划数量不能为空")
    @DecimalMin(value = "0.001", message = "计划数量必须大于0")
    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", message = "实际数量不能为负数")
    @Column(name = "actual_quantity", precision = 15, scale = 3)
    private BigDecimal actualQuantity;

    @DecimalMin(value = "0.0", message = "单价不能为负数")
    @Column(name = "unit_price", precision = 15, scale = 4)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "金额不能为负数")
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "quality_status", length = 20)
    private String qualityStatus = "合格";

    @Column(name = "remark", length = 500)
    private String remark;

    // Constructors
    public InboundOrderDetail() {
    }

    public InboundOrderDetail(InboundOrder inboundOrder, Goods goods, BigDecimal quantity) {
        this.inboundOrder = inboundOrder;
        this.goods = goods;
        this.quantity = quantity;
        this.actualQuantity = quantity;
    }

    public InboundOrderDetail(InboundOrder inboundOrder, Goods goods, BigDecimal quantity, BigDecimal unitPrice) {
        this.inboundOrder = inboundOrder;
        this.goods = goods;
        this.quantity = quantity;
        this.actualQuantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = quantity.multiply(unitPrice);
    }

    // Getters and Setters
    public InboundOrder getInboundOrder() {
        return inboundOrder;
    }

    public void setInboundOrder(InboundOrder inboundOrder) {
        this.inboundOrder = inboundOrder;
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
        recalculateAmount();
    }

    public BigDecimal getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(BigDecimal actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        recalculateAmount();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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



    public String getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(String qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    // Business methods
    
    /**
     * 重新计算金额
     */
    public void recalculateAmount() {
        if (quantity != null && unitPrice != null) {
            this.amount = quantity.multiply(unitPrice);
        }
    }



    /**
     * 判断是否有保质期
     */
    public boolean hasExpiryDate() {
        return expiryDate != null;
    }

    /**
     * 判断数量是否一致
     */
    public boolean isQuantityMatched() {
        if (actualQuantity == null) {
            return false;
        }
        return quantity.compareTo(actualQuantity) == 0;
    }

    /**
     * 获取数量差异
     */
    public BigDecimal getQuantityDifference() {
        if (actualQuantity == null) {
            return BigDecimal.ZERO;
        }
        return actualQuantity.subtract(quantity);
    }

    /**
     * 判断是否合格
     */
    public boolean isQualified() {
        return "合格".equals(qualityStatus);
    }

    @Override
    public String toString() {
        return "InboundOrderDetail{" +
                "id=" + getId() +
                ", goods=" + (goods != null ? goods.getName() : null) +
                ", quantity=" + quantity +
                ", actualQuantity=" + actualQuantity +
                ", unitPrice=" + unitPrice +
                ", amount=" + amount +

                '}';
    }
}
