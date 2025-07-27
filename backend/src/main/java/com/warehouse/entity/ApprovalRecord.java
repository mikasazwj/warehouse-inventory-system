package com.warehouse.entity;

import com.warehouse.enums.ApprovalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 审批记录实体类
 * 记录所有业务单据的审批历史
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "approval_records", indexes = {
    @Index(name = "idx_approval_business", columnList = "business_type,business_id"),
    @Index(name = "idx_approval_applicant", columnList = "applicant_id"),
    @Index(name = "idx_approval_approver", columnList = "approver_id"),
    @Index(name = "idx_approval_status", columnList = "approval_status"),
    @Index(name = "idx_approval_time", columnList = "approval_time")
})
public class ApprovalRecord extends BaseEntity {

    @NotBlank(message = "业务类型不能为空")
    @Size(max = 50, message = "业务类型长度不能超过50个字符")
    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;

    @NotNull(message = "业务ID不能为空")
    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @NotBlank(message = "业务单号不能为空")
    @Size(max = 50, message = "业务单号长度不能超过50个字符")
    @Column(name = "business_number", nullable = false, length = 50)
    private String businessNumber;

    @NotNull(message = "申请人不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    @NotNull(message = "审批状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.DRAFT;

    @Column(name = "approval_comment", length = 1000)
    private String approvalComment;

    @Column(name = "apply_reason", length = 1000)
    private String applyReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    // Constructors
    public ApprovalRecord() {
    }

    public ApprovalRecord(String businessType, Long businessId, String businessNumber, User applicant) {
        this.businessType = businessType;
        this.businessId = businessId;
        this.businessNumber = businessNumber;
        this.applicant = applicant;
        this.applyTime = LocalDateTime.now();
        this.approvalStatus = ApprovalStatus.DRAFT;
    }

    // Getters and Setters
    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public void setBusinessNumber(String businessNumber) {
        this.businessNumber = businessNumber;
    }

    public User getApplicant() {
        return applicant;
    }

    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
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

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    // Business methods
    
    /**
     * 判断是否可以审批
     */
    public boolean canApprove() {
        return approvalStatus != null && approvalStatus.canApprove();
    }

    /**
     * 判断是否已完成审批
     */
    public boolean isCompleted() {
        return approvalStatus != null && approvalStatus.isCompleted();
    }

    /**
     * 审批通过
     */
    public void approve(User approver, String comment) {
        if (!canApprove()) {
            throw new IllegalStateException("当前状态不允许审批");
        }
        this.approver = approver;
        this.approvalTime = LocalDateTime.now();
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvalComment = comment;
    }

    /**
     * 审批拒绝
     */
    public void reject(User approver, String comment) {
        if (!canApprove()) {
            throw new IllegalStateException("当前状态不允许审批");
        }
        this.approver = approver;
        this.approvalTime = LocalDateTime.now();
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.approvalComment = comment;
    }

    /**
     * 撤销申请
     */
    public void cancel() {
        if (!canApprove()) {
            throw new IllegalStateException("当前状态不允许撤销");
        }
        this.approvalStatus = ApprovalStatus.CANCELLED;
        this.approvalTime = LocalDateTime.now();
    }

    /**
     * 获取审批耗时（分钟）
     */
    public Long getApprovalDurationMinutes() {
        if (applyTime == null || approvalTime == null) {
            return null;
        }
        return java.time.Duration.between(applyTime, approvalTime).toMinutes();
    }

    /**
     * 获取业务类型显示名称
     */
    public String getBusinessTypeDisplayName() {
        switch (businessType) {
            case "InboundOrder":
                return "入库单";
            case "OutboundOrder":
                return "出库单";
            case "TransferOrder":
                return "调拨单";
            case "InventoryCheckOrder":
                return "盘点单";
            default:
                return businessType;
        }
    }

    @Override
    public String toString() {
        return "ApprovalRecord{" +
                "id=" + getId() +
                ", businessType='" + businessType + '\'' +
                ", businessId=" + businessId +
                ", businessNumber='" + businessNumber + '\'' +
                ", applicant=" + (applicant != null ? applicant.getRealName() : null) +
                ", approver=" + (approver != null ? approver.getRealName() : null) +
                ", approvalStatus=" + approvalStatus +
                ", applyTime=" + applyTime +
                ", approvalTime=" + approvalTime +
                '}';
    }
}
