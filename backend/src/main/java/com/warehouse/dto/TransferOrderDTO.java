package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.Priority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 调拨单DTO
 * 
 * @author Warehouse Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferOrderDTO {

    private Long id;

    @NotBlank(message = "调拨单号不能为空")
    @Size(max = 50, message = "调拨单号长度不能超过50个字符")
    private String orderNumber;

    @NotNull(message = "源仓库不能为空")
    private Long sourceWarehouseId;

    private String sourceWarehouseName;

    @NotNull(message = "目标仓库不能为空")
    private Long targetWarehouseId;

    private String targetWarehouseName;

    @NotNull(message = "计划调拨日期不能为空")
    private LocalDate plannedDate;

    private LocalDate actualDate;

    private ApprovalStatus status;

    private String referenceNumber;

    private String remark;

    private String reason;

    private Priority priority;

    private BigDecimal totalQuantity;

    private LocalDateTime operationTime;

    private String operatedBy;

    private LocalDateTime approvalTime;

    private String approvedBy;

    private String approvalRemark;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private String createdBy;

    private String updatedBy;

    /**
     * 调拨单明细列表
     */
    @Valid
    @NotEmpty(message = "调拨明细不能为空")
    private List<TransferOrderDetailDTO> details;

    // 构造函数
    public TransferOrderDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getSourceWarehouseId() {
        return sourceWarehouseId;
    }

    public void setSourceWarehouseId(Long sourceWarehouseId) {
        this.sourceWarehouseId = sourceWarehouseId;
    }

    public String getSourceWarehouseName() {
        return sourceWarehouseName;
    }

    public void setSourceWarehouseName(String sourceWarehouseName) {
        this.sourceWarehouseName = sourceWarehouseName;
    }

    public Long getTargetWarehouseId() {
        return targetWarehouseId;
    }

    public void setTargetWarehouseId(Long targetWarehouseId) {
        this.targetWarehouseId = targetWarehouseId;
    }

    public String getTargetWarehouseName() {
        return targetWarehouseName;
    }

    public void setTargetWarehouseName(String targetWarehouseName) {
        this.targetWarehouseName = targetWarehouseName;
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

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public String getOperatedBy() {
        return operatedBy;
    }

    public void setOperatedBy(String operatedBy) {
        this.operatedBy = operatedBy;
    }

    public LocalDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getApprovalRemark() {
        return approvalRemark;
    }

    public void setApprovalRemark(String approvalRemark) {
        this.approvalRemark = approvalRemark;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<TransferOrderDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<TransferOrderDetailDTO> details) {
        this.details = details;
    }

    // 业务方法

    /**
     * 获取状态显示文本
     */
    public String getStatusText() {
        return status != null ? status.getDescription() : "未知";
    }

    /**
     * 判断是否可以编辑
     */
    public boolean canEdit() {
        return status == ApprovalStatus.PENDING;
    }

    /**
     * 判断是否可以审批
     */
    public boolean canApprove() {
        return status == ApprovalStatus.PENDING || status == ApprovalStatus.IN_PROGRESS;
    }

    /**
     * 判断是否可以执行
     */
    public boolean canExecute() {
        return status == ApprovalStatus.APPROVED && operationTime == null;
    }

    /**
     * 判断是否已执行
     */
    public boolean isExecuted() {
        return operationTime != null;
    }

    @Override
    public String toString() {
        return "TransferOrderDTO{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", sourceWarehouseName='" + sourceWarehouseName + '\'' +
                ", targetWarehouseName='" + targetWarehouseName + '\'' +
                ", status=" + status +
                '}';
    }

    /**
     * 调拨单明细DTO
     */
    public static class TransferOrderDetailDTO {
        private Long id;

        @NotNull(message = "货物不能为空")
        private Long goodsId;

        private String goodsCode;
        private String goodsName;
        private String goodsUnit;
        private String specification;
        private BigDecimal unitPrice;

        @NotNull(message = "调拨数量不能为空")
        @DecimalMin(value = "0.001", message = "调拨数量必须大于0")
        private BigDecimal quantity;

        private String remark;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getGoodsId() { return goodsId; }
        public void setGoodsId(Long goodsId) { this.goodsId = goodsId; }
        public String getGoodsCode() { return goodsCode; }
        public void setGoodsCode(String goodsCode) { this.goodsCode = goodsCode; }
        public String getGoodsName() { return goodsName; }
        public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
        public String getGoodsUnit() { return goodsUnit; }
        public void setGoodsUnit(String goodsUnit) { this.goodsUnit = goodsUnit; }
        public String getSpecification() { return specification; }
        public void setSpecification(String specification) { this.specification = specification; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    /**
     * 调拨单创建请求DTO
     */
    public static class CreateRequest {
        @NotNull(message = "源仓库不能为空")
        private Long sourceWarehouseId;

        @NotNull(message = "目标仓库不能为空")
        private Long targetWarehouseId;

        @NotNull(message = "计划调拨日期不能为空")
        private LocalDate plannedDate;

        private String referenceNumber;
        private String remark;
        private String reason;
        private Priority priority;

        @Valid
        @NotEmpty(message = "调拨明细不能为空")
        private List<TransferOrderDetailDTO> details;

        // Getters and Setters
        public Long getSourceWarehouseId() { return sourceWarehouseId; }
        public void setSourceWarehouseId(Long sourceWarehouseId) { this.sourceWarehouseId = sourceWarehouseId; }
        public Long getTargetWarehouseId() { return targetWarehouseId; }
        public void setTargetWarehouseId(Long targetWarehouseId) { this.targetWarehouseId = targetWarehouseId; }
        public LocalDate getPlannedDate() { return plannedDate; }
        public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public Priority getPriority() { return priority; }
        public void setPriority(Priority priority) { this.priority = priority; }
        public List<TransferOrderDetailDTO> getDetails() { return details; }
        public void setDetails(List<TransferOrderDetailDTO> details) { this.details = details; }
    }

    /**
     * 调拨单更新请求DTO
     */
    public static class UpdateRequest {
        private LocalDate plannedDate;
        private String referenceNumber;
        private String remark;
        private String reason;
        private Priority priority;

        @Valid
        private List<TransferOrderDetailDTO> details;

        // Getters and Setters
        public LocalDate getPlannedDate() { return plannedDate; }
        public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public Priority getPriority() { return priority; }
        public void setPriority(Priority priority) { this.priority = priority; }
        public List<TransferOrderDetailDTO> getDetails() { return details; }
        public void setDetails(List<TransferOrderDetailDTO> details) { this.details = details; }
    }

    /**
     * 审批请求DTO
     */
    public static class ApprovalRequest {
        @NotNull(message = "审批状态不能为空")
        private ApprovalStatus status;

        private String approvalRemark;

        // Getters and Setters
        public ApprovalStatus getStatus() { return status; }
        public void setStatus(ApprovalStatus status) { this.status = status; }
        public String getApprovalRemark() { return approvalRemark; }
        public void setApprovalRemark(String approvalRemark) { this.approvalRemark = approvalRemark; }
    }
}
