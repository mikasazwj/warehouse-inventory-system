package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
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
 * 出库单DTO
 * 
 * @author Warehouse Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutboundOrderDTO {

    private Long id;

    @NotBlank(message = "出库单号不能为空")
    @Size(max = 50, message = "出库单号长度不能超过50个字符")
    private String orderNumber;

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private String warehouseName;

    private Long customerId;

    private String customerName;

    @NotBlank(message = "领取人不能为空")
    @Size(max = 100, message = "领取人姓名长度不能超过100个字符")
    private String recipientName;

    @NotNull(message = "业务类型不能为空")
    private BusinessType businessType;

    @NotNull(message = "计划出库日期不能为空")
    private LocalDate plannedDate;

    private LocalDate actualDate;

    @DecimalMin(value = "0.0", message = "总金额不能为负数")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0", message = "总数量不能为负数")
    private BigDecimal totalQuantity;

    private ApprovalStatus status;

    private String referenceNumber;

    private String remark;

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
     * 出库单明细列表
     */
    @Valid
    @NotEmpty(message = "出库明细不能为空")
    private List<OutboundOrderDetailDTO> details;

    // 构造函数
    public OutboundOrderDTO() {
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

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
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

    public List<OutboundOrderDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<OutboundOrderDetailDTO> details) {
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
     * 获取业务类型显示文本
     */
    public String getBusinessTypeText() {
        return businessType != null ? businessType.getDescription() : "未知";
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
        return "OutboundOrderDTO{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", warehouseName='" + warehouseName + '\'' +
                ", businessType=" + businessType +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                '}';
    }

    /**
     * 出库单明细DTO
     */
    public static class OutboundOrderDetailDTO {
        private Long id;

        @NotNull(message = "货物不能为空")
        private Long goodsId;

        private String goodsCode;
        private String goodsName;
        private String goodsUnit;
        private String goodsSpecification;
        private String goodsModel;

        @NotNull(message = "出库数量不能为空")
        @DecimalMin(value = "0.001", message = "出库数量必须大于0")
        private BigDecimal quantity;

        @DecimalMin(value = "0.0", message = "单价不能为负数")
        private BigDecimal unitPrice;

        @DecimalMin(value = "0.0", message = "金额不能为负数")
        private BigDecimal amount;

        private String batchNumber;
        private String location;
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
        public String getGoodsSpecification() { return goodsSpecification; }
        public void setGoodsSpecification(String goodsSpecification) { this.goodsSpecification = goodsSpecification; }
        public String getGoodsModel() { return goodsModel; }
        public void setGoodsModel(String goodsModel) { this.goodsModel = goodsModel; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    /**
     * 出库单创建请求DTO
     */
    public static class CreateRequest {
        @NotNull(message = "仓库不能为空")
        private Long warehouseId;

        private Long customerId;

        @NotBlank(message = "领取人不能为空")
        @Size(max = 100, message = "领取人姓名长度不能超过100个字符")
        private String recipientName;

        @NotNull(message = "业务类型不能为空")
        private BusinessType businessType;

        @NotNull(message = "计划出库日期不能为空")
        private LocalDate plannedDate;

        private String referenceNumber;
        private String remark;

        @NotBlank(message = "制单人不能为空")
        @Size(max = 50, message = "制单人长度不能超过50个字符")
        private String createdBy;

        @Valid
        @NotEmpty(message = "出库明细不能为空")
        private List<OutboundOrderDetailDTO> details;

        // Getters and Setters
        public Long getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        public BusinessType getBusinessType() { return businessType; }
        public void setBusinessType(BusinessType businessType) { this.businessType = businessType; }
        public LocalDate getPlannedDate() { return plannedDate; }
        public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public List<OutboundOrderDetailDTO> getDetails() { return details; }
        public void setDetails(List<OutboundOrderDetailDTO> details) { this.details = details; }
    }

    /**
     * 出库单更新请求DTO
     */
    public static class UpdateRequest {
        private Long customerId;
        private String recipientName;
        private LocalDate plannedDate;
        private String referenceNumber;
        private String remark;

        @Valid
        private List<OutboundOrderDetailDTO> details;

        // Getters and Setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        public LocalDate getPlannedDate() { return plannedDate; }
        public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public List<OutboundOrderDetailDTO> getDetails() { return details; }
        public void setDetails(List<OutboundOrderDetailDTO> details) { this.details = details; }
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
