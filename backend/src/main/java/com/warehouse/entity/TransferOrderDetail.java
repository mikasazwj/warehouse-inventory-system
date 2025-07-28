package com.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 调拨单明细实体类
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "transfer_order_details", indexes = {
    @Index(name = "idx_transfer_detail_order", columnList = "transfer_order_id"),
    @Index(name = "idx_transfer_detail_goods", columnList = "goods_id")
})
public class TransferOrderDetail extends BaseEntity {

    @NotNull(message = "调拨单不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_order_id", nullable = false)
    private TransferOrder transferOrder;

    @NotNull(message = "货物不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = false)
    private Goods goods;

    @Column(name = "specification", length = 200)
    private String specification;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "计划数量不能为空")
    @DecimalMin(value = "0.001", message = "计划数量必须大于0")
    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", message = "实际数量不能为负数")
    @Column(name = "actual_quantity", precision = 15, scale = 3)
    private BigDecimal actualQuantity;



    @Column(name = "remark", length = 500)
    private String remark;

    // Constructors
    public TransferOrderDetail() {
    }

    public TransferOrderDetail(TransferOrder transferOrder, Goods goods, BigDecimal quantity) {
        this.transferOrder = transferOrder;
        this.goods = goods;
        this.quantity = quantity;
        this.actualQuantity = quantity;
    }

    public TransferOrderDetail(TransferOrder transferOrder, Goods goods, String specification, BigDecimal unitPrice, BigDecimal quantity) {
        this.transferOrder = transferOrder;
        this.goods = goods;
        this.specification = specification;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.actualQuantity = quantity;
    }

    // Getters and Setters
    public TransferOrder getTransferOrder() {
        return transferOrder;
    }

    public void setTransferOrder(TransferOrder transferOrder) {
        this.transferOrder = transferOrder;
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

    public BigDecimal getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(BigDecimal actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }



    // Business methods

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
        return "TransferOrderDetail{" +
                "id=" + getId() +
                ", goods=" + (goods != null ? goods.getName() : null) +
                ", specification='" + specification + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", actualQuantity=" + actualQuantity +
                '}';
    }
}
