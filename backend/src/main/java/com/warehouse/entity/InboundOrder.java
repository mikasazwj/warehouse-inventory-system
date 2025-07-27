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
 * 入库单实体类
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "inbound_orders", indexes = {
    @Index(name = "idx_inbound_order_number", columnList = "order_number", unique = true),
    @Index(name = "idx_inbound_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_inbound_status", columnList = "status"),
    @Index(name = "idx_inbound_type", columnList = "business_type"),
    @Index(name = "idx_inbound_date", columnList = "planned_date")
})
public class InboundOrder extends BaseEntity {

    @NotBlank(message = "入库单号不能为空")
    @Size(max = 50, message = "入库单号长度不能超过50个字符")
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotNull(message = "仓库不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @NotNull(message = "业务类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false, length = 20)
    private BusinessType businessType;

    @NotNull(message = "审批状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.DRAFT;

    @NotNull(message = "计划入库日期不能为空")
    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "total_quantity", precision = 15, scale = 3)
    private BigDecimal totalQuantity = BigDecimal.ZERO;

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

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "remark", length = 1000)
    private String remark;

    /**
     * 入库单明细
     */
    @OneToMany(mappedBy = "inboundOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InboundOrderDetail> details = new ArrayList<>();

    // Constructors
    public InboundOrder() {
    }

    public InboundOrder(String orderNumber, Warehouse warehouse, BusinessType businessType, LocalDate plannedDate) {
        this.orderNumber = orderNumber;
        this.warehouse = warehouse;
        this.businessType = businessType;
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

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) {
        this.totalQuantity = totalQuantity;
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

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<InboundOrderDetail> getDetails() {
        return details;
    }

    public void setDetails(List<InboundOrderDetail> details) {
        this.details = details;
    }

    // Business methods
    
    /**
     * 添加明细
     */
    public void addDetail(InboundOrderDetail detail) {
        if (details == null) {
            details = new ArrayList<>();
        }
        details.add(detail);
        detail.setInboundOrder(this);
        recalculateTotal();
    }

    /**
     * 移除明细
     */
    public void removeDetail(InboundOrderDetail detail) {
        if (details != null) {
            details.remove(detail);
            detail.setInboundOrder(null);
            recalculateTotal();
        }
    }

    /**
     * 重新计算总计
     */
    public void recalculateTotal() {
        if (details == null || details.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
            this.totalQuantity = BigDecimal.ZERO;
            return;
        }

        this.totalAmount = details.stream()
                .map(InboundOrderDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalQuantity = details.stream()
                .map(InboundOrderDetail::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 判断是否可以审批
     */
    public boolean canApprove() {
        return status != null && status.canApprove();
    }

    /**
     * 判断是否可以执行入库
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
     * 执行入库
     */
    public void execute(User operator) {
        if (!canExecute()) {
            throw new IllegalStateException("当前状态不允许执行入库");
        }
        this.operator = operator;
        this.operationTime = LocalDateTime.now();
        this.actualDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return "InboundOrder{" +
                "id=" + getId() +
                ", orderNumber='" + orderNumber + '\'' +
                ", warehouse=" + (warehouse != null ? warehouse.getName() : null) +
                ", businessType=" + businessType +
                ", status=" + status +
                ", plannedDate=" + plannedDate +
                ", totalAmount=" + totalAmount +
                ", totalQuantity=" + totalQuantity +
                '}';
    }

    // ===== 兼容方法 =====

    /**
     * 获取审批备注 (兼容方法)
     */
    public String getApprovalRemark() {
        return this.approvalComment;
    }

    /**
     * 设置审批备注 (兼容方法)
     */
    public void setApprovalRemark(String approvalRemark) {
        this.approvalComment = approvalRemark;
    }
}
