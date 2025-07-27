package com.warehouse.entity;

import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.Priority;
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
 * 调拨单实体类
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "transfer_orders", indexes = {
    @Index(name = "idx_transfer_order_number", columnList = "order_number", unique = true),
    @Index(name = "idx_transfer_from_warehouse", columnList = "from_warehouse_id"),
    @Index(name = "idx_transfer_to_warehouse", columnList = "to_warehouse_id"),
    @Index(name = "idx_transfer_status", columnList = "status"),
    @Index(name = "idx_transfer_date", columnList = "planned_date")
})
public class TransferOrder extends BaseEntity {

    @NotBlank(message = "调拨单号不能为空")
    @Size(max = 50, message = "调拨单号长度不能超过50个字符")
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotNull(message = "调出仓库不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_warehouse_id", nullable = false)
    private Warehouse fromWarehouse;

    @NotNull(message = "调入仓库不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_warehouse_id", nullable = false)
    private Warehouse toWarehouse;

    @NotNull(message = "审批状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.DRAFT;

    @NotNull(message = "计划调拨日期不能为空")
    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "actual_date")
    private LocalDate actualDate;

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

    @Column(name = "transfer_reason", length = 500)
    private String transferReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority = Priority.NORMAL;

    @Column(name = "remark", length = 1000)
    private String remark;

    /**
     * 调拨单明细
     */
    @OneToMany(mappedBy = "transferOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransferOrderDetail> details = new ArrayList<>();

    // Constructors
    public TransferOrder() {
    }

    public TransferOrder(String orderNumber, Warehouse fromWarehouse, Warehouse toWarehouse, LocalDate plannedDate) {
        this.orderNumber = orderNumber;
        this.fromWarehouse = fromWarehouse;
        this.toWarehouse = toWarehouse;
        this.plannedDate = plannedDate;
    }

    // Getters and Setters
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Warehouse getFromWarehouse() {
        return fromWarehouse;
    }

    public void setFromWarehouse(Warehouse fromWarehouse) {
        this.fromWarehouse = fromWarehouse;
    }

    public Warehouse getToWarehouse() {
        return toWarehouse;
    }

    public void setToWarehouse(Warehouse toWarehouse) {
        this.toWarehouse = toWarehouse;
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

    public String getTransferReason() {
        return transferReason;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<TransferOrderDetail> getDetails() {
        return details;
    }

    public void setDetails(List<TransferOrderDetail> details) {
        this.details = details;
    }

    // Business methods
    
    /**
     * 添加明细
     */
    public void addDetail(TransferOrderDetail detail) {
        if (details == null) {
            details = new ArrayList<>();
        }
        details.add(detail);
        detail.setTransferOrder(this);
        recalculateTotal();
    }

    /**
     * 移除明细
     */
    public void removeDetail(TransferOrderDetail detail) {
        if (details != null) {
            details.remove(detail);
            detail.setTransferOrder(null);
            recalculateTotal();
        }
    }

    /**
     * 重新计算总计
     */
    public void recalculateTotal() {
        if (details == null || details.isEmpty()) {
            this.totalQuantity = BigDecimal.ZERO;
            return;
        }

        this.totalQuantity = details.stream()
                .map(TransferOrderDetail::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 判断是否可以审批
     */
    public boolean canApprove() {
        return status != null && status.canApprove();
    }

    /**
     * 判断是否可以执行调拨
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
     * 验证调拨仓库不能相同
     */
    public boolean isValidTransfer() {
        return fromWarehouse != null && toWarehouse != null && 
               !fromWarehouse.getId().equals(toWarehouse.getId());
    }

    /**
     * 审批通过
     */
    public void approve(User approver, String comment) {
        if (!canApprove()) {
            throw new IllegalStateException("当前状态不允许审批");
        }
        if (!isValidTransfer()) {
            throw new IllegalStateException("调出仓库和调入仓库不能相同");
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
     * 执行调拨
     */
    public void execute(User operator) {
        if (!canExecute()) {
            throw new IllegalStateException("当前状态不允许执行调拨");
        }
        this.operator = operator;
        this.operationTime = LocalDateTime.now();
        this.actualDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return "TransferOrder{" +
                "id=" + getId() +
                ", orderNumber='" + orderNumber + '\'' +
                ", fromWarehouse=" + (fromWarehouse != null ? fromWarehouse.getName() : null) +
                ", toWarehouse=" + (toWarehouse != null ? toWarehouse.getName() : null) +
                ", status=" + status +
                ", plannedDate=" + plannedDate +
                ", totalQuantity=" + totalQuantity +
                '}';
    }
}
