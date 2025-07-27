package com.warehouse.exception;

import com.warehouse.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * @author Warehouse Team
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.warn("业务异常: {}", e.getMessage());
        ApiResponse<Object> response = ApiResponse.businessError(e.getMessage()).withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        logger.warn("资源不存在: {}", e.getMessage());
        ApiResponse<Object> response = ApiResponse.notFound(e.getMessage()).withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        logger.warn("参数验证失败: {}", message);
        ApiResponse<Object> response = ApiResponse.badRequest("参数验证失败: " + message).withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        logger.warn("参数绑定失败: {}", message);
        ApiResponse<Object> response = ApiResponse.badRequest("参数绑定失败: " + message).withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        logger.warn("约束违反: {}", message);
        ApiResponse<Object> response = ApiResponse.badRequest("约束违反: " + message).withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("参数 '%s' 的值 '%s' 类型不正确", e.getName(), e.getValue());
        logger.warn("参数类型不匹配: {}", message);
        ApiResponse<Object> response = ApiResponse.badRequest(message).withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(org.springframework.security.core.AuthenticationException e, HttpServletRequest request) {
        logger.warn("认证失败: {}", e.getMessage());
        ApiResponse<Object> response = ApiResponse.unauthorized("认证失败: " + e.getMessage()).withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 处理凭证错误异常
     */
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(org.springframework.security.authentication.BadCredentialsException e, HttpServletRequest request) {
        logger.warn("凭证错误: {}", e.getMessage());
        ApiResponse<Object> response = ApiResponse.unauthorized("用户名或密码错误").withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e, HttpServletRequest request) {
        logger.warn("访问被拒绝: {}", e.getMessage());
        ApiResponse<Object> response = ApiResponse.forbidden("访问被拒绝: " + e.getMessage()).withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 处理数据完整性违反异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        String message = "数据完整性违反";
        if (e.getMessage() != null) {
            if (e.getMessage().contains("Duplicate entry")) {
                message = "数据已存在，不能重复添加";
            } else if (e.getMessage().contains("foreign key constraint")) {
                message = "存在关联数据，不能删除";
            } else if (e.getMessage().contains("cannot be null")) {
                message = "必填字段不能为空";
            }
        }
        
        logger.warn("数据完整性违反: {}", e.getMessage());
        ApiResponse<Object> response = ApiResponse.badRequest(message).withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.warn("非法参数: {}", e.getMessage());
        ApiResponse<Object> response = ApiResponse.badRequest("非法参数: " + e.getMessage()).withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        logger.warn("非法状态: {}", e.getMessage());
        ApiResponse<Object> response = ApiResponse.badRequest("操作失败: " + e.getMessage()).withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e, HttpServletRequest request) {
        logger.error("系统异常: ", e);
        ApiResponse<Object> response = ApiResponse.error("系统内部错误，请联系管理员").withPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
