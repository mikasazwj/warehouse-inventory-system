package com.warehouse.entity;

import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 盘点单实体类
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "inventory_check_orders", indexes = {
    @Index(name = "idx_check_order_number", columnList = "order_number", unique = true),
    @Index(name = "idx_check_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_check_status", columnList = "status"),
    @Index(name = "idx_check_type", columnList = "check_type"),
    @Index(name = "idx_check_date", columnList = "planned_date")
})
public class InventoryCheckOrder extends BaseEntity {

    @NotBlank(message = "盘点单号不能为空")
    @Size(max = 50, message = "盘点单号长度不能超过50个字符")
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotNull(message = "仓库不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @NotNull(message = "盘点类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "check_type", nullable = false, length = 20)
    private BusinessType checkType;

    @NotNull(message = "审批状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.DRAFT;

    @NotNull(message = "计划盘点日期不能为空")
    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    @Column(name = "total_items")
    private Integer totalItems = 0;

    @Column(name = "checked_items")
    private Integer checkedItems = 0;

    @Column(name = "gain_items")
    private Integer gainItems = 0;

    @Column(name = "loss_items")
    private Integer lossItems = 0;

    @Column(name = "gain_amount", precision = 15, scale = 2)
    private BigDecimal gainAmount = BigDecimal.ZERO;

    @Column(name = "loss_amount", precision = 15, scale = 2)
    private BigDecimal lossAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    @Column(name = "approval_comment", length = 500)
    private String approvalComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(name = "operation_time")
    private LocalDateTime operationTime;

    @Column(name = "check_reason", length = 500)
    private String checkReason;

    @Column(name = "remark", length = 1000)
    private String remark;

    /**
     * 盘点单明细
     */
    @OneToMany(mappedBy = "checkOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryCheckDetail> details = new ArrayList<>();

    // Constructors
    public InventoryCheckOrder() {
    }

    public InventoryCheckOrder(String orderNumber, Warehouse warehouse, BusinessType checkType, LocalDate plannedDate) {
        this.orderNumber = orderNumber;
        this.warehouse = warehouse;
        this.checkType = checkType;
        this.plannedDate = plannedDate;
    }

    // Getters and Setters
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public BusinessType getCheckType() {
        return checkType;
    }

    public void setCheckType(BusinessType checkType) {
        this.checkType = checkType;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public LocalDate getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(LocalDate plannedDate) {
        this.plannedDate = plannedDate;
    }

    public LocalDate getActualDate() {
        return actualDate;
    }

    public void setActualDate(LocalDate actualDate) {
        this.actualDate = actualDate;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getCheckedItems() {
        return checkedItems;
    }

    public void setCheckedItems(Integer checkedItems) {
        this.checkedItems = checkedItems;
    }

    public Integer getGainItems() {
        return gainItems;
    }

    public void setGainItems(Integer gainItems) {
        this.gainItems = gainItems;
    }

    public Integer getLossItems() {
        return lossItems;
    }

    public void setLossItems(Integer lossItems) {
        this.lossItems = lossItems;
    }

    public BigDecimal getGainAmount() {
        return gainAmount;
    }

    public void setGainAmount(BigDecimal gainAmount) {
        this.gainAmount = gainAmount;
    }

    public BigDecimal getLossAmount() {
        return lossAmount;
    }

    public void setLossAmount(BigDecimal lossAmount) {
        this.lossAmount = lossAmount;
    }

    public User getApplicant() {
        return applicant;
    }

    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public LocalDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    public User getOperator() {
        return operator;
    }

    public void setOperator(User operator) {
        this.operator = operator;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public String getCheckReason() {
        return checkReason;
    }

    public void setCheckReason(String checkReason) {
        this.checkReason = checkReason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<InventoryCheckDetail> getDetails() {
        return details;
    }

    public void setDetails(List<InventoryCheckDetail> details) {
        this.details = details;
    }

    // Business methods
    
    /**
     * 添加明细
     */
    public void addDetail(InventoryCheckDetail detail) {
        if (details == null) {
            details = new ArrayList<>();
        }
        details.add(detail);
        detail.setCheckOrder(this);
        recalculateStatistics();
    }

    /**
     * 移除明细
     */
    public void removeDetail(InventoryCheckDetail detail) {
        if (details != null) {
            details.remove(detail);
            detail.setCheckOrder(null);
            recalculateStatistics();
        }
    }

    /**
     * 重新计算统计信息
     */
    public void recalculateStatistics() {
        if (details == null || details.isEmpty()) {
            this.totalItems = 0;
            this.checkedItems = 0;
            this.gainItems = 0;
            this.lossItems = 0;
            this.gainAmount = BigDecimal.ZERO;
            this.lossAmount = BigDecimal.ZERO;
            return;
        }

        this.totalItems = details.size();
        this.checkedItems = (int) details.stream().filter(InventoryCheckDetail::isChecked).count();
        this.gainItems = (int) details.stream().filter(InventoryCheckDetail::isGain).count();
        this.lossItems = (int) details.stream().filter(InventoryCheckDetail::isLoss).count();

        this.gainAmount = details.stream()
                .filter(InventoryCheckDetail::isGain)
                .map(InventoryCheckDetail::getDifferenceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.lossAmount = details.stream()
                .filter(InventoryCheckDetail::isLoss)
                .map(detail -> detail.getDifferenceAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 判断是否可以审批
     */
    public boolean canApprove() {
        return status != null && status.canApprove();
    }

    /**
     * 判断是否可以执行盘点
     */
    public boolean canExecute() {
        return status != null && status.canExecute();
    }

    /**
     * 判断是否可以撤销
     */
    public boolean canCancel() {
        return status != null && status.canCancel();
    }

    /**
     * 获取盘点进度百分比
     */
    public double getProgress() {
        if (totalItems == null || totalItems == 0) {
            return 0.0;
        }
        return (double) checkedItems / totalItems * 100;
    }

    /**
     * 判断是否盘点完成
     */
    public boolean isCheckCompleted() {
        return totalItems != null && checkedItems != null && totalItems.equals(checkedItems);
    }

    /**
     * 审批通过
     */
    public void approve(User approver, String comment) {
        if (!canApprove()) {
            throw new IllegalStateException("当前状态不允许审批");
        }
        this.status = ApprovalStatus.APPROVED;
        this.approver = approver;
        this.approvalTime = LocalDateTime.now();
        this.approvalComment = comment;
    }

    /**
     * 审批拒绝
     */
    public void reject(User approver, String comment) {
        if (!canApprove()) {
            throw new IllegalStateException("当前状态不允许审批");
        }
        this.status = ApprovalStatus.REJECTED;
        this.approver = approver;
        this.approvalTime = LocalDateTime.now();
        this.approvalComment = comment;
    }

    /**
     * 执行盘点
     */
    public void execute(User operator) {
        if (!canExecute()) {
            throw new IllegalStateException("当前状态不允许执行盘点");
        }
        this.operator = operator;
        this.operationTime = LocalDateTime.now();
        this.actualDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return "InventoryCheckOrder{" +
                "id=" + getId() +
                ", orderNumber='" + orderNumber + '\'' +
                ", warehouse=" + (warehouse != null ? warehouse.getName() : null) +
                ", checkType=" + checkType +
                ", status=" + status +
                ", plannedDate=" + plannedDate +
                ", totalItems=" + totalItems +
                ", checkedItems=" + checkedItems +
                '}';
    }
}
