package com.warehouse.entity;

import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.StocktakeType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 盘点单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "details")
@Entity
@Table(name = "stocktake_orders")
public class StocktakeOrder extends BaseEntity {

    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "stocktake_type", nullable = false)
    private StocktakeType stocktakeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.DRAFT;

    @Column(name = "planned_date")
    private LocalDate plannedDate;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    @Column(name = "total_items")
    private Integer totalItems = 0;

    @Column(name = "completed_items")
    private Integer completedItems = 0;

    @Column(name = "difference_items")
    private Integer differenceItems = 0;

    @Column(name = "gain_items")
    private Integer gainItems = 0;

    @Column(name = "loss_items")
    private Integer lossItems = 0;

    @Column(name = "normal_items")
    private Integer normalItems = 0;

    @Column(name = "stocktake_users", length = 500)
    private String stocktakeUsers;

    @Column(name = "stocktake_user_names", length = 500)
    private String stocktakeUserNames;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "approval_remark", length = 1000)
    private String approvalRemark;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_time")
    private LocalDateTime approvedTime;

    @Column(name = "started_by", length = 100)
    private String startedBy;

    @Column(name = "started_time")
    private LocalDateTime startedTime;

    @Column(name = "completed_by", length = 100)
    private String completedBy;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;

    @OneToMany(mappedBy = "stocktakeOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StocktakeOrderDetail> details;

    @OneToMany(mappedBy = "stocktakeOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StocktakeOrderRange> ranges;
}
