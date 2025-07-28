package com.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 出库单明细实体类
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "outbound_order_details", indexes = {
    @Index(name = "idx_outbound_detail_order", columnList = "outbound_order_id"),
    @Index(name = "idx_outbound_detail_goods", columnList = "goods_id")
})
public class OutboundOrderDetail extends BaseEntity {

    @NotNull(message = "出库单不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_order_id", nullable = false)
    private OutboundOrder outboundOrder;

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



    @Column(name = "remark", length = 500)
    private String remark;

    // Constructors
    public OutboundOrderDetail() {
    }

    public OutboundOrderDetail(OutboundOrder outboundOrder, Goods goods, BigDecimal quantity) {
        this.outboundOrder = outboundOrder;
        this.goods = goods;
        this.quantity = quantity;
        this.actualQuantity = quantity;
    }

    public OutboundOrderDetail(OutboundOrder outboundOrder, Goods goods, BigDecimal quantity, BigDecimal unitPrice) {
        this.outboundOrder = outboundOrder;
        this.goods = goods;
        this.quantity = quantity;
        this.actualQuantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = quantity.multiply(unitPrice);
    }

    // Getters and Setters
    public OutboundOrder getOutboundOrder() {
        return outboundOrder;
    }

    public void setOutboundOrder(OutboundOrder outboundOrder) {
        this.outboundOrder = outboundOrder;
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

    @Override
    public String toString() {
        return "OutboundOrderDetail{" +
                "id=" + getId() +
                ", goods=" + (goods != null ? goods.getName() : null) +
                ", quantity=" + quantity +
                ", actualQuantity=" + actualQuantity +
                ", unitPrice=" + unitPrice +
                ", amount=" + amount +

                '}';
    }
}
