package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一API响应格式
 * 
 * @author Warehouse Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    private String timestamp;

    /**
     * 请求路径
     */
    private String path;

    // 构造函数
    public ApiResponse() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public ApiResponse(Integer code, String message) {
        this();
        this.code = code;
        this.success = (code != null && code == 200);
        this.message = message;
    }

    public ApiResponse(Integer code, String message, T data) {
        this();
        this.code = code;
        this.success = (code != null && code == 200);
        this.message = message;
        this.data = data;
    }

    // 静态工厂方法

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功");
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message);
    }

    /**
     * 失败响应（带错误码）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message);
    }

    /**
     * 参数错误响应
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message);
    }

    /**
     * 未授权响应
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message != null ? message : "未授权访问");
    }

    /**
     * 禁止访问响应
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message != null ? message : "禁止访问");
    }

    /**
     * 资源不存在响应
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message != null ? message : "资源不存在");
    }

    /**
     * 业务异常响应
     */
    public static <T> ApiResponse<T> businessError(String message) {
        return new ApiResponse<>(600, message);
    }

    // Getters and Setters
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
        this.success = (code != null && code == 200);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // 业务方法

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return code == null || code != 200;
    }

    /**
     * 设置请求路径
     */
    public ApiResponse<T> withPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", path='" + path + '\'' +
                '}';
    }
}
