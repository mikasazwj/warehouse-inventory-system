package com.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 盘点范围实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stocktake_order_ranges")
public class StocktakeOrderRange extends BaseEntity {

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

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "remark", length = 500)
    private String remark;
}
