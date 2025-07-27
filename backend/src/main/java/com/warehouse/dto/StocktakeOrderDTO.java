package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.StocktakeType;
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
 * 盘点单DTO
 * 
 * @author Warehouse Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StocktakeOrderDTO {

    private Long id;

    @NotBlank(message = "盘点单号不能为空")
    @Size(max = 50, message = "盘点单号长度不能超过50个字符")
    private String orderNumber;

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private String warehouseName;

    @NotNull(message = "盘点类型不能为空")
    private StocktakeType stocktakeType;

    @NotNull(message = "计划盘点日期不能为空")
    private LocalDate plannedDate;

    private LocalDate actualDate;

    private ApprovalStatus status;

    private String remark;

    private String stocktakeUserNames;

    private LocalDateTime operationTime;

    private String operatedBy;

    private LocalDateTime approvalTime;

    private String approvedBy;

    private String approvalRemark;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private String createdBy;

    private String updatedBy;

    // 统计字段
    private Integer totalItems;
    private Integer completedItems;
    private Integer differenceItems;
    private Integer gainItems;
    private Integer lossItems;
    private Integer normalItems;

    /**
     * 盘点单明细列表
     */
    @Valid
    @NotEmpty(message = "盘点明细不能为空")
    private List<StocktakeOrderDetailDTO> details;

    // 构造函数
    public StocktakeOrderDTO() {
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

    public StocktakeType getStocktakeType() {
        return stocktakeType;
    }

    public void setStocktakeType(StocktakeType stocktakeType) {
        this.stocktakeType = stocktakeType;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStocktakeUserNames() {
        return stocktakeUserNames;
    }

    public void setStocktakeUserNames(String stocktakeUserNames) {
        this.stocktakeUserNames = stocktakeUserNames;
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

    public List<StocktakeOrderDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<StocktakeOrderDetailDTO> details) {
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
     * 获取盘点类型显示文本
     */
    public String getStocktakeTypeText() {
        return stocktakeType != null ? stocktakeType.getDescription() : "未知";
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

    // 统计字段的getter和setter
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

    public Integer getCompletedItems() { return completedItems; }
    public void setCompletedItems(Integer completedItems) { this.completedItems = completedItems; }

    public Integer getDifferenceItems() { return differenceItems; }
    public void setDifferenceItems(Integer differenceItems) { this.differenceItems = differenceItems; }

    public Integer getGainItems() { return gainItems; }
    public void setGainItems(Integer gainItems) { this.gainItems = gainItems; }

    public Integer getLossItems() { return lossItems; }
    public void setLossItems(Integer lossItems) { this.lossItems = lossItems; }

    public Integer getNormalItems() { return normalItems; }
    public void setNormalItems(Integer normalItems) { this.normalItems = normalItems; }

    @Override
    public String toString() {
        return "StocktakeOrderDTO{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", warehouseName='" + warehouseName + '\'' +
                ", stocktakeType=" + stocktakeType +
                ", status=" + status +
                '}';
    }

    /**
     * 盘点单明细DTO
     */
    public static class StocktakeOrderDetailDTO {
        private Long id;

        @NotNull(message = "货物不能为空")
        private Long goodsId;

        private String goodsCode;
        private String goodsName;
        private String goodsSpecification;
        private String goodsUnit;

        @DecimalMin(value = "0.0", message = "账面数量不能为负数")
        private BigDecimal bookQuantity;

        @NotNull(message = "实盘数量不能为空")
        @DecimalMin(value = "0.0", message = "实盘数量不能为负数")
        private BigDecimal actualQuantity;

        private BigDecimal differenceQuantity;

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
        public String getGoodsSpecification() { return goodsSpecification; }
        public void setGoodsSpecification(String goodsSpecification) { this.goodsSpecification = goodsSpecification; }
        public String getGoodsUnit() { return goodsUnit; }
        public void setGoodsUnit(String goodsUnit) { this.goodsUnit = goodsUnit; }
        public BigDecimal getBookQuantity() { return bookQuantity; }
        public void setBookQuantity(BigDecimal bookQuantity) { this.bookQuantity = bookQuantity; }
        public BigDecimal getActualQuantity() { return actualQuantity; }
        public void setActualQuantity(BigDecimal actualQuantity) { this.actualQuantity = actualQuantity; }
        public BigDecimal getDifferenceQuantity() { return differenceQuantity; }
        public void setDifferenceQuantity(BigDecimal differenceQuantity) { this.differenceQuantity = differenceQuantity; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }

        /**
         * 计算差异数量
         */
        public void calculateDifference() {
            if (actualQuantity != null && bookQuantity != null) {
                differenceQuantity = actualQuantity.subtract(bookQuantity);
            }
        }

        /**
         * 判断是否有差异
         */
        public boolean hasDifference() {
            return differenceQuantity != null && differenceQuantity.compareTo(BigDecimal.ZERO) != 0;
        }
    }

    /**
     * 盘点单创建请求DTO
     */
    public static class CreateRequest {
        @NotNull(message = "仓库不能为空")
        private Long warehouseId;

        @NotNull(message = "盘点类型不能为空")
        private StocktakeType stocktakeType;

        @NotNull(message = "计划盘点日期不能为空")
        private LocalDate plannedDate;

        private String remark;

        private String stocktakeUserNames;

        @Valid
        @NotEmpty(message = "盘点明细不能为空")
        private List<StocktakeOrderDetailDTO> details;

        // Getters and Setters
        public Long getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
        public StocktakeType getStocktakeType() { return stocktakeType; }
        public void setStocktakeType(StocktakeType stocktakeType) { this.stocktakeType = stocktakeType; }
        public LocalDate getPlannedDate() { return plannedDate; }
        public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public String getStocktakeUserNames() { return stocktakeUserNames; }
        public void setStocktakeUserNames(String stocktakeUserNames) { this.stocktakeUserNames = stocktakeUserNames; }
        public List<StocktakeOrderDetailDTO> getDetails() { return details; }
        public void setDetails(List<StocktakeOrderDetailDTO> details) { this.details = details; }
    }

    /**
     * 盘点单更新请求DTO
     */
    public static class UpdateRequest {
        private LocalDate plannedDate;
        private String remark;
        private String stocktakeUserNames;

        @Valid
        private List<StocktakeOrderDetailDTO> details;

        // Getters and Setters
        public LocalDate getPlannedDate() { return plannedDate; }
        public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public String getStocktakeUserNames() { return stocktakeUserNames; }
        public void setStocktakeUserNames(String stocktakeUserNames) { this.stocktakeUserNames = stocktakeUserNames; }
        public List<StocktakeOrderDetailDTO> getDetails() { return details; }
        public void setDetails(List<StocktakeOrderDetailDTO> details) { this.details = details; }
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

    /**
     * 盘点明细更新请求DTO
     */
    public static class UpdateDetailRequest {
        private Long id;

        @NotNull(message = "实盘数量不能为空")
        @DecimalMin(value = "0.0", message = "实盘数量不能为负数")
        private BigDecimal actualQuantity;

        private String remark;
        private String stocktakeUser;
        private Boolean isCompleted;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public BigDecimal getActualQuantity() { return actualQuantity; }
        public void setActualQuantity(BigDecimal actualQuantity) { this.actualQuantity = actualQuantity; }

        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }

        public String getStocktakeUser() { return stocktakeUser; }
        public void setStocktakeUser(String stocktakeUser) { this.stocktakeUser = stocktakeUser; }

        public Boolean getIsCompleted() { return isCompleted; }
        public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    }

    /**
     * 盘点报告DTO
     */
    public static class StocktakeReport {
        private Long stocktakeOrderId;
        private String orderNumber;
        private String warehouseName;
        private StocktakeType stocktakeType;
        private LocalDate plannedDate;
        private LocalDate actualDate;
        private ApprovalStatus status;

        // 统计信息
        private Integer totalItems;
        private Integer completedItems;
        private Integer differenceItems;
        private Integer gainItems;
        private Integer lossItems;
        private Integer normalItems;

        // 金额统计
        private BigDecimal totalBookAmount;
        private BigDecimal totalActualAmount;
        private BigDecimal totalDifferenceAmount;
        private BigDecimal gainAmount;
        private BigDecimal lossAmount;

        // 明细列表
        private List<StocktakeOrderDetailDTO> gainDetails;
        private List<StocktakeOrderDetailDTO> lossDetails;
        private List<StocktakeOrderDetailDTO> normalDetails;

        private String createdBy;
        private LocalDateTime createdTime;
        private String completedBy;
        private LocalDateTime completedTime;

        // Getters and Setters
        public Long getStocktakeOrderId() { return stocktakeOrderId; }
        public void setStocktakeOrderId(Long stocktakeOrderId) { this.stocktakeOrderId = stocktakeOrderId; }

        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

        public StocktakeType getStocktakeType() { return stocktakeType; }
        public void setStocktakeType(StocktakeType stocktakeType) { this.stocktakeType = stocktakeType; }

        public LocalDate getPlannedDate() { return plannedDate; }
        public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }

        public LocalDate getActualDate() { return actualDate; }
        public void setActualDate(LocalDate actualDate) { this.actualDate = actualDate; }

        public ApprovalStatus getStatus() { return status; }
        public void setStatus(ApprovalStatus status) { this.status = status; }

        public Integer getTotalItems() { return totalItems; }
        public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

        public Integer getCompletedItems() { return completedItems; }
        public void setCompletedItems(Integer completedItems) { this.completedItems = completedItems; }

        public Integer getDifferenceItems() { return differenceItems; }
        public void setDifferenceItems(Integer differenceItems) { this.differenceItems = differenceItems; }

        public Integer getGainItems() { return gainItems; }
        public void setGainItems(Integer gainItems) { this.gainItems = gainItems; }

        public Integer getLossItems() { return lossItems; }
        public void setLossItems(Integer lossItems) { this.lossItems = lossItems; }

        public Integer getNormalItems() { return normalItems; }
        public void setNormalItems(Integer normalItems) { this.normalItems = normalItems; }

        public BigDecimal getTotalBookAmount() { return totalBookAmount; }
        public void setTotalBookAmount(BigDecimal totalBookAmount) { this.totalBookAmount = totalBookAmount; }

        public BigDecimal getTotalActualAmount() { return totalActualAmount; }
        public void setTotalActualAmount(BigDecimal totalActualAmount) { this.totalActualAmount = totalActualAmount; }

        public BigDecimal getTotalDifferenceAmount() { return totalDifferenceAmount; }
        public void setTotalDifferenceAmount(BigDecimal totalDifferenceAmount) { this.totalDifferenceAmount = totalDifferenceAmount; }

        public BigDecimal getGainAmount() { return gainAmount; }
        public void setGainAmount(BigDecimal gainAmount) { this.gainAmount = gainAmount; }

        public BigDecimal getLossAmount() { return lossAmount; }
        public void setLossAmount(BigDecimal lossAmount) { this.lossAmount = lossAmount; }

        public List<StocktakeOrderDetailDTO> getGainDetails() { return gainDetails; }
        public void setGainDetails(List<StocktakeOrderDetailDTO> gainDetails) { this.gainDetails = gainDetails; }

        public List<StocktakeOrderDetailDTO> getLossDetails() { return lossDetails; }
        public void setLossDetails(List<StocktakeOrderDetailDTO> lossDetails) { this.lossDetails = lossDetails; }

        public List<StocktakeOrderDetailDTO> getNormalDetails() { return normalDetails; }
        public void setNormalDetails(List<StocktakeOrderDetailDTO> normalDetails) { this.normalDetails = normalDetails; }

        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

        public LocalDateTime getCreatedTime() { return createdTime; }
        public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

        public String getCompletedBy() { return completedBy; }
        public void setCompletedBy(String completedBy) { this.completedBy = completedBy; }

        public LocalDateTime getCompletedTime() { return completedTime; }
        public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    }
}
