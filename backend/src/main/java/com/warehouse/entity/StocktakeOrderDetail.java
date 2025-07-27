package com.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 盘点单明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "stocktakeOrder")
@Entity
@Table(name = "stocktake_order_details")
public class StocktakeOrderDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stocktake_order_id", nullable = false)
    private StocktakeOrder stocktakeOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = false)
    private Goods goods;

    @Column(name = "goods_code", length = 50)
    private String goodsCode;

    @Column(name = "goods_name", length = 200)
    private String goodsName;

    @Column(name = "book_quantity", precision = 15, scale = 3)
    private BigDecimal bookQuantity = BigDecimal.ZERO;

    @Column(name = "actual_quantity", precision = 15, scale = 3)
    private BigDecimal actualQuantity;

    @Column(name = "difference_quantity", precision = 15, scale = 3)
    private BigDecimal differenceQuantity = BigDecimal.ZERO;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "book_amount", precision = 15, scale = 2)
    private BigDecimal bookAmount = BigDecimal.ZERO;

    @Column(name = "actual_amount", precision = 15, scale = 2)
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Column(name = "difference_amount", precision = 15, scale = 2)
    private BigDecimal differenceAmount = BigDecimal.ZERO;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "stocktake_user", length = 100)
    private String stocktakeUser;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "is_adjusted")
    private Boolean isAdjusted = false;
}
