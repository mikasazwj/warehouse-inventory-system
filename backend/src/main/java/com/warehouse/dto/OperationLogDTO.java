package com.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 操作日志DTO
 * 
 * @author Warehouse Team
 */
public class OperationLogDTO {

    private Long id;
    private Long operatorId;
    private String operatorName;
    private String operationType;
    private String operationDesc;
    private String businessType;
    private Long businessId;
    private String businessNumber;
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime operationTime;
    private String ipAddress;
    private String userAgent;
    private String requestUri;
    private String requestMethod;
    private String requestParams;
    private String responseResult;
    private String errorMessage;
    private Long executionTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public String getOperationDesc() { return operationDesc; }
    public void setOperationDesc(String operationDesc) { this.operationDesc = operationDesc; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getBusinessNumber() { return businessNumber; }
    public void setBusinessNumber(String businessNumber) { this.businessNumber = businessNumber; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public LocalDateTime getOperationTime() { return operationTime; }
    public void setOperationTime(LocalDateTime operationTime) { this.operationTime = operationTime; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getRequestUri() { return requestUri; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }

    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }

    public String getRequestParams() { return requestParams; }
    public void setRequestParams(String requestParams) { this.requestParams = requestParams; }

    public String getResponseResult() { return responseResult; }
    public void setResponseResult(String responseResult) { this.responseResult = responseResult; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Long getExecutionTime() { return executionTime; }
    public void setExecutionTime(Long executionTime) { this.executionTime = executionTime; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    /**
     * 创建操作日志请求
     */
    public static class CreateRequest {
        @NotNull(message = "操作人ID不能为空")
        private Long operatorId;

        @NotBlank(message = "操作类型不能为空")
        @Size(max = 50, message = "操作类型长度不能超过50个字符")
        private String operationType;

        @NotBlank(message = "操作描述不能为空")
        @Size(max = 500, message = "操作描述长度不能超过500个字符")
        private String operationDesc;

        private String businessType;
        private Long businessId;
        private String businessNumber;
        private Long warehouseId;
        private String ipAddress;
        private String userAgent;
        private String requestUri;
        private String requestMethod;
        private String requestParams;

        // Getters and Setters
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }

        public String getOperationDesc() { return operationDesc; }
        public void setOperationDesc(String operationDesc) { this.operationDesc = operationDesc; }

        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }

        public Long getBusinessId() { return businessId; }
        public void setBusinessId(Long businessId) { this.businessId = businessId; }

        public String getBusinessNumber() { return businessNumber; }
        public void setBusinessNumber(String businessNumber) { this.businessNumber = businessNumber; }

        public Long getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

        public String getRequestUri() { return requestUri; }
        public void setRequestUri(String requestUri) { this.requestUri = requestUri; }

        public String getRequestMethod() { return requestMethod; }
        public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }

        public String getRequestParams() { return requestParams; }
        public void setRequestParams(String requestParams) { this.requestParams = requestParams; }
    }
}
