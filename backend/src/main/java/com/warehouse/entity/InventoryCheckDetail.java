package com.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 盘点单明细实体类
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "inventory_check_details", indexes = {
    @Index(name = "idx_check_detail_order", columnList = "check_order_id"),
    @Index(name = "idx_check_detail_goods", columnList = "goods_id")
})
public class InventoryCheckDetail extends BaseEntity {

    @NotNull(message = "盘点单不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_order_id", nullable = false)
    private InventoryCheckOrder checkOrder;

    @NotNull(message = "货物不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = false)
    private Goods goods;

    @NotNull(message = "账面数量不能为空")
    @DecimalMin(value = "0.0", message = "账面数量不能为负数")
    @Column(name = "book_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal bookQuantity = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "实盘数量不能为负数")
    @Column(name = "actual_quantity", precision = 15, scale = 3)
    private BigDecimal actualQuantity;

    @Column(name = "difference_quantity", precision = 15, scale = 3)
    private BigDecimal differenceQuantity = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "单价不能为负数")
    @Column(name = "unit_cost", precision = 15, scale = 4)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "difference_amount", precision = 15, scale = 2)
    private BigDecimal differenceAmount = BigDecimal.ZERO;



    @Column(name = "checked", nullable = false)
    private Boolean checked = false;

    @Column(name = "check_result", length = 20)
    private String checkResult = "正常";

    @Column(name = "remark", length = 500)
    private String remark;

    // Constructors
    public InventoryCheckDetail() {
    }

    public InventoryCheckDetail(InventoryCheckOrder checkOrder, Goods goods, BigDecimal bookQuantity) {
        this.checkOrder = checkOrder;
        this.goods = goods;
        this.bookQuantity = bookQuantity;
        this.actualQuantity = bookQuantity;
        this.differenceQuantity = BigDecimal.ZERO;
    }

    public InventoryCheckDetail(InventoryCheckOrder checkOrder, Goods goods, BigDecimal bookQuantity, BigDecimal unitCost) {
        this.checkOrder = checkOrder;
        this.goods = goods;
        this.bookQuantity = bookQuantity;
        this.actualQuantity = bookQuantity;
        this.unitCost = unitCost;
        this.differenceQuantity = BigDecimal.ZERO;
        this.differenceAmount = BigDecimal.ZERO;
    }

    // Getters and Setters
    public InventoryCheckOrder getCheckOrder() {
        return checkOrder;
    }

    public void setCheckOrder(InventoryCheckOrder checkOrder) {
        this.checkOrder = checkOrder;
    }

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public BigDecimal getBookQuantity() {
        return bookQuantity;
    }

    public void setBookQuantity(BigDecimal bookQuantity) {
        this.bookQuantity = bookQuantity;
        recalculateDifference();
    }

    public BigDecimal getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(BigDecimal actualQuantity) {
        this.actualQuantity = actualQuantity;
        recalculateDifference();
    }

    public BigDecimal getDifferenceQuantity() {
        return differenceQuantity;
    }

    public void setDifferenceQuantity(BigDecimal differenceQuantity) {
        this.differenceQuantity = differenceQuantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
        recalculateDifference();
    }

    public BigDecimal getDifferenceAmount() {
        return differenceAmount;
    }

    public void setDifferenceAmount(BigDecimal differenceAmount) {
        this.differenceAmount = differenceAmount;
    }



    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public String getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    // Business methods
    
    /**
     * 重新计算差异
     */
    public void recalculateDifference() {
        if (actualQuantity != null && bookQuantity != null) {
            this.differenceQuantity = actualQuantity.subtract(bookQuantity);
            
            if (unitCost != null) {
                this.differenceAmount = differenceQuantity.multiply(unitCost);
            }
            
            // 更新盘点结果
            updateCheckResult();
        }
    }

    /**
     * 更新盘点结果
     */
    private void updateCheckResult() {
        if (differenceQuantity == null) {
            this.checkResult = "未盘点";
            return;
        }
        
        int comparison = differenceQuantity.compareTo(BigDecimal.ZERO);
        if (comparison > 0) {
            this.checkResult = "盘盈";
        } else if (comparison < 0) {
            this.checkResult = "盘亏";
        } else {
            this.checkResult = "正常";
        }
    }

    /**
     * 判断是否已盘点
     */
    public boolean isChecked() {
        return checked != null && checked;
    }



    /**
     * 判断是否盘盈
     */
    public boolean isGain() {
        return differenceQuantity != null && differenceQuantity.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否盘亏
     */
    public boolean isLoss() {
        return differenceQuantity != null && differenceQuantity.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 判断是否正常
     */
    public boolean isNormal() {
        return differenceQuantity != null && differenceQuantity.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 获取差异率
     */
    public BigDecimal getDifferenceRate() {
        if (bookQuantity == null || bookQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (differenceQuantity == null) {
            return BigDecimal.ZERO;
        }
        return differenceQuantity.divide(bookQuantity, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 执行盘点
     */
    public void performCheck(BigDecimal actualQuantity, String remark) {
        this.actualQuantity = actualQuantity;
        this.remark = remark;
        this.checked = true;
        recalculateDifference();
    }

    @Override
    public String toString() {
        return "InventoryCheckDetail{" +
                "id=" + getId() +
                ", goods=" + (goods != null ? goods.getName() : null) +
                ", bookQuantity=" + bookQuantity +
                ", actualQuantity=" + actualQuantity +
                ", differenceQuantity=" + differenceQuantity +
                ", checkResult='" + checkResult + '\'' +
                ", checked=" + checked +
                '}';
    }
}
