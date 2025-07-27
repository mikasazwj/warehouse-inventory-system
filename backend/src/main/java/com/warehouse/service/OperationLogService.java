package com.warehouse.service;

import com.warehouse.dto.OperationLogDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.entity.OperationLog;
import com.warehouse.entity.User;
import com.warehouse.entity.Warehouse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 操作日志服务接口
 * 
 * @author Warehouse Team
 */
public interface OperationLogService {

    /**
     * 创建操作日志
     */
    OperationLogDTO createOperationLog(OperationLogDTO.CreateRequest request);

    /**
     * 根据ID查找操作日志
     */
    Optional<OperationLogDTO> findById(Long id);

    /**
     * 查找所有操作日志
     */
    List<OperationLogDTO> findAll();

    /**
     * 根据操作人查找日志
     */
    List<OperationLogDTO> findByOperator(Long operatorId);

    /**
     * 根据操作类型查找日志
     */
    List<OperationLogDTO> findByOperationType(String operationType);

    /**
     * 根据业务类型和业务ID查找日志
     */
    List<OperationLogDTO> findByBusiness(String businessType, Long businessId);

    /**
     * 根据仓库查找日志
     */
    List<OperationLogDTO> findByWarehouse(Long warehouseId);

    /**
     * 根据时间范围查找日志
     */
    List<OperationLogDTO> findByTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据操作人和时间范围查找日志
     */
    List<OperationLogDTO> findByOperatorAndTimeBetween(Long operatorId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询操作日志
     */
    PageResponse<OperationLogDTO> findByPage(String keyword, Pageable pageable);

    /**
     * 记录登录日志
     */
    void recordLoginLog(User user, String ipAddress, String userAgent);

    /**
     * 记录登出日志
     */
    void recordLogoutLog(User user, String ipAddress);

    /**
     * 记录业务操作日志
     */
    void recordBusinessLog(User user, String operationType, String operationDesc, 
                          String businessType, Long businessId, String businessNumber, 
                          Warehouse warehouse, String ipAddress, String userAgent,
                          String requestUri, String requestMethod, String requestParams);

    /**
     * 记录简单操作日志
     */
    void recordSimpleLog(User user, String operationType, String operationDesc);

    /**
     * 获取用户的操作日志
     */
    List<OperationLogDTO> getUserOperationLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取最近的操作日志
     */
    List<OperationLogDTO> getRecentLogs(int limit);

    /**
     * 统计操作日志数量
     */
    long countAll();

    /**
     * 根据操作人统计日志数量
     */
    long countByOperator(Long operatorId);

    /**
     * 根据操作类型统计日志数量
     */
    long countByOperationType(String operationType);
}
