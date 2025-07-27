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
 * 入库单DTO
 * 
 * @author Warehouse Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InboundOrderDTO {

    private Long id;

    @NotBlank(message = "入库单号不能为空")
    @Size(max = 50, message = "入库单号长度不能超过50个字符")
    private String orderNumber;

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private String warehouseName;

    @NotNull(message = "业务类型不能为空")
    private BusinessType businessType;

    @NotNull(message = "计划入库日期不能为空")
    private LocalDate plannedDate;

    private LocalDate actualDate;

    @DecimalMin(value = "0.0", message = "总金额不能为负数")
    private BigDecimal totalAmount;

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
     * 入库单明细列表
     */
    @Valid
    @NotEmpty(message = "入库明细不能为空")
    private List<InboundOrderDetailDTO> details;

    // 构造函数
    public InboundOrderDTO() {
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

    public List<InboundOrderDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<InboundOrderDetailDTO> details) {
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
        return "InboundOrderDTO{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", warehouseName='" + warehouseName + '\'' +
                ", businessType=" + businessType +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                '}';
    }

    /**
     * 入库单明细DTO
     */
    public static class InboundOrderDetailDTO {
        private Long id;

        @NotNull(message = "货物不能为空")
        private Long goodsId;

        private String goodsCode;
        private String goodsName;
        private String goodsModel;
        private String goodsSpecification;
        private String goodsUnit;

        @NotNull(message = "入库数量不能为空")
        @DecimalMin(value = "0.001", message = "入库数量必须大于0")
        private BigDecimal quantity;

        @DecimalMin(value = "0.0", message = "单价不能为负数")
        private BigDecimal unitPrice;

        @DecimalMin(value = "0.0", message = "金额不能为负数")
        private BigDecimal amount;

        private String batchNumber;
        private LocalDate productionDate;
        private LocalDate expiryDate;
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
        public String getGoodsModel() { return goodsModel; }
        public void setGoodsModel(String goodsModel) { this.goodsModel = goodsModel; }
        public String getGoodsSpecification() { return goodsSpecification; }
        public void setGoodsSpecification(String goodsSpecification) { this.goodsSpecification = goodsSpecification; }
        public String getGoodsUnit() { return goodsUnit; }
        public void setGoodsUnit(String goodsUnit) { this.goodsUnit = goodsUnit; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
        public LocalDate getProductionDate() { return productionDate; }
        public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    /**
     * 入库单创建请求DTO
     */
    public static class CreateRequest {
        @NotNull(message = "仓库不能为空")
        private Long warehouseId;

        @NotNull(message = "业务类型不能为空")
        private BusinessType businessType;

        @NotNull(message = "计划入库日期不能为空")
        private LocalDate plannedDate;

        private String referenceNumber;
        private String remark;
        private String createdBy;

        @Valid
        @NotEmpty(message = "入库明细不能为空")
        private List<InboundOrderDetailDTO> details;

        // Getters and Setters
        public Long getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
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
        public List<InboundOrderDetailDTO> getDetails() { return details; }
        public void setDetails(List<InboundOrderDetailDTO> details) { this.details = details; }
    }

    /**
     * 入库单更新请求DTO
     */
    public static class UpdateRequest {
        private LocalDate plannedDate;
        private String referenceNumber;
        private String remark;

        @Valid
        private List<InboundOrderDetailDTO> details;

        // Getters and Setters
        public LocalDate getPlannedDate() { return plannedDate; }
        public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public List<InboundOrderDetailDTO> getDetails() { return details; }
        public void setDetails(List<InboundOrderDetailDTO> details) { this.details = details; }
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
