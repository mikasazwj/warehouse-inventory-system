package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存历史记录DTO
 * 
 * @author Warehouse Team
 */
public class InventoryHistoryDTO {

    private Long id;

    @NotNull(message = "库存ID不能为空")
    private Long inventoryId;

    @NotBlank(message = "操作类型不能为空")
    private String operationType;

    @NotNull(message = "变动数量不能为空")
    private BigDecimal quantity;

    private BigDecimal beforeQuantity;

    private BigDecimal afterQuantity;

    private String reason;

    private String operatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operatedTime;

    private String remark;

    // 关联单据号
    private String relatedOrderNumber;

    // 业务类型
    private String businessType;

    // 关联信息
    private String warehouseName;
    private String goodsName;
    private String goodsCode;

    // Constructors
    public InventoryHistoryDTO() {
    }

    public InventoryHistoryDTO(Long inventoryId, String operationType, BigDecimal quantity,
                              BigDecimal beforeQuantity, BigDecimal afterQuantity, String reason,
                              String operatedBy, LocalDateTime operatedTime) {
        this.inventoryId = inventoryId;
        this.operationType = operationType;
        this.quantity = quantity;
        this.beforeQuantity = beforeQuantity;
        this.afterQuantity = afterQuantity;
        this.reason = reason;
        this.operatedBy = operatedBy;
        this.operatedTime = operatedTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getBeforeQuantity() {
        return beforeQuantity;
    }

    public void setBeforeQuantity(BigDecimal beforeQuantity) {
        this.beforeQuantity = beforeQuantity;
    }

    public BigDecimal getAfterQuantity() {
        return afterQuantity;
    }

    public void setAfterQuantity(BigDecimal afterQuantity) {
        this.afterQuantity = afterQuantity;
    }



    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getOperatedBy() {
        return operatedBy;
    }

    public void setOperatedBy(String operatedBy) {
        this.operatedBy = operatedBy;
    }

    public LocalDateTime getOperatedTime() {
        return operatedTime;
    }

    public void setOperatedTime(LocalDateTime operatedTime) {
        this.operatedTime = operatedTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getRelatedOrderNumber() {
        return relatedOrderNumber;
    }

    public void setRelatedOrderNumber(String relatedOrderNumber) {
        this.relatedOrderNumber = relatedOrderNumber;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    @Override
    public String toString() {
        return "InventoryHistoryDTO{" +
                "id=" + id +
                ", inventoryId=" + inventoryId +
                ", operationType='" + operationType + '\'' +
                ", quantity=" + quantity +
                ", beforeQuantity=" + beforeQuantity +
                ", afterQuantity=" + afterQuantity +
                ", reason='" + reason + '\'' +
                ", operatedBy='" + operatedBy + '\'' +
                ", operatedTime=" + operatedTime +
                '}';
    }
}
