package com.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 记录系统中所有重要操作的日志
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "operation_logs", indexes = {
    @Index(name = "idx_log_operator", columnList = "operator_id"),
    @Index(name = "idx_log_operation_type", columnList = "operation_type"),
    @Index(name = "idx_log_business", columnList = "business_type,business_id"),
    @Index(name = "idx_log_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_log_time", columnList = "operation_time"),
    @Index(name = "idx_log_ip", columnList = "ip_address")
})
public class OperationLog extends BaseEntity {

    @NotNull(message = "操作人不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private User operator;

    @NotBlank(message = "操作类型不能为空")
    @Size(max = 50, message = "操作类型长度不能超过50个字符")
    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    @NotBlank(message = "操作描述不能为空")
    @Size(max = 500, message = "操作描述长度不能超过500个字符")
    @Column(name = "operation_desc", nullable = false, length = 500)
    private String operationDesc;

    @Column(name = "business_type", length = 50)
    private String businessType;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "business_number", length = 50)
    private String businessNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @NotNull(message = "操作时间不能为空")
    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "request_uri", length = 200)
    private String requestUri;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_params", length = 2000)
    private String requestParams;

    @Column(name = "response_result", length = 10)
    private String responseResult = "SUCCESS";

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "execution_time")
    private Long executionTime;

    // Constructors
    public OperationLog() {
        this.operationTime = LocalDateTime.now();
    }

    public OperationLog(User operator, String operationType, String operationDesc) {
        this.operator = operator;
        this.operationType = operationType;
        this.operationDesc = operationDesc;
        this.operationTime = LocalDateTime.now();
    }

    // Getters and Setters
    public User getOperator() {
        return operator;
    }

    public void setOperator(User operator) {
        this.operator = operator;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }

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

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getResponseResult() {
        return responseResult;
    }

    public void setResponseResult(String responseResult) {
        this.responseResult = responseResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    // Business methods
    
    /**
     * 判断操作是否成功
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(responseResult);
    }

    /**
     * 判断操作是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(responseResult);
    }

    /**
     * 设置操作成功
     */
    public void setSuccess() {
        this.responseResult = "SUCCESS";
        this.errorMessage = null;
    }

    /**
     * 设置操作失败
     */
    public void setFailed(String errorMessage) {
        this.responseResult = "FAILED";
        this.errorMessage = errorMessage;
    }

    /**
     * 获取操作类型显示名称
     */
    public String getOperationTypeDisplayName() {
        switch (operationType) {
            case "LOGIN":
                return "登录";
            case "LOGOUT":
                return "登出";
            case "CREATE":
                return "创建";
            case "UPDATE":
                return "更新";
            case "DELETE":
                return "删除";
            case "APPROVE":
                return "审批";
            case "REJECT":
                return "拒绝";
            case "INBOUND":
                return "入库";
            case "OUTBOUND":
                return "出库";
            case "TRANSFER":
                return "调拨";
            case "CHECK":
                return "盘点";
            case "EXPORT":
                return "导出";
            case "IMPORT":
                return "导入";
            default:
                return operationType;
        }
    }

    /**
     * 创建登录日志
     */
    public static OperationLog createLoginLog(User user, String ipAddress, String userAgent) {
        OperationLog log = new OperationLog(user, "LOGIN", "用户登录系统");
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        return log;
    }

    /**
     * 创建登出日志
     */
    public static OperationLog createLogoutLog(User user, String ipAddress) {
        OperationLog log = new OperationLog(user, "LOGOUT", "用户登出系统");
        log.setIpAddress(ipAddress);
        return log;
    }

    /**
     * 创建业务操作日志
     */
    public static OperationLog createBusinessLog(User user, String operationType, String operationDesc, 
                                                String businessType, Long businessId, String businessNumber, 
                                                Warehouse warehouse) {
        OperationLog log = new OperationLog(user, operationType, operationDesc);
        log.setBusinessType(businessType);
        log.setBusinessId(businessId);
        log.setBusinessNumber(businessNumber);
        log.setWarehouse(warehouse);
        return log;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
                "id=" + getId() +
                ", operator=" + (operator != null ? operator.getRealName() : null) +
                ", operationType='" + operationType + '\'' +
                ", operationDesc='" + operationDesc + '\'' +
                ", businessType='" + businessType + '\'' +
                ", businessNumber='" + businessNumber + '\'' +
                ", operationTime=" + operationTime +
                ", responseResult='" + responseResult + '\'' +
                '}';
    }
}
