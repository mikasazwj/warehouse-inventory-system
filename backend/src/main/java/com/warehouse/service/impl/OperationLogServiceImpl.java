package com.warehouse.service.impl;

import com.warehouse.dto.OperationLogDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.entity.OperationLog;
import com.warehouse.entity.User;
import com.warehouse.entity.Warehouse;
import com.warehouse.repository.OperationLogRepository;
import com.warehouse.repository.UserRepository;
import com.warehouse.repository.WarehouseRepository;
import com.warehouse.service.OperationLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现类
 * 
 * @author Warehouse Team
 */
@Service
@Transactional
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public OperationLogDTO createOperationLog(OperationLogDTO.CreateRequest request) {
        User operator = userRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new RuntimeException("操作人不存在"));

        OperationLog log = new OperationLog();
        log.setOperator(operator);
        log.setOperationType(request.getOperationType());
        log.setOperationDesc(request.getOperationDesc());
        log.setBusinessType(request.getBusinessType());
        log.setBusinessId(request.getBusinessId());
        log.setBusinessNumber(request.getBusinessNumber());
        log.setIpAddress(request.getIpAddress());
        log.setUserAgent(request.getUserAgent());
        log.setRequestUri(request.getRequestUri());
        log.setRequestMethod(request.getRequestMethod());
        log.setRequestParams(request.getRequestParams());
        log.setOperationTime(LocalDateTime.now());

        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId()).orElse(null);
            log.setWarehouse(warehouse);
        }

        OperationLog savedLog = operationLogRepository.save(log);
        return convertToDTO(savedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OperationLogDTO> findById(Long id) {
        return operationLogRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationLogDTO> findAll() {
        return operationLogRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationLogDTO> findByOperator(Long operatorId) {
        return operationLogRepository.findByOperatorIdAndDeletedFalseOrderByOperationTimeDesc(operatorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationLogDTO> findByOperationType(String operationType) {
        return operationLogRepository.findByOperationTypeAndDeletedFalseOrderByOperationTimeDesc(operationType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationLogDTO> findByBusiness(String businessType, Long businessId) {
        return operationLogRepository.findByBusinessTypeAndBusinessIdAndDeletedFalseOrderByOperationTimeDesc(businessType, businessId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationLogDTO> findByWarehouse(Long warehouseId) {
        return operationLogRepository.findByWarehouseIdAndDeletedFalseOrderByOperationTimeDesc(warehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationLogDTO> findByTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogRepository.findByOperationTimeBetween(startTime, endTime)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationLogDTO> findByOperatorAndTimeBetween(Long operatorId, LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogRepository.findByOperatorAndTimeBetween(operatorId, startTime, endTime)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OperationLogDTO> findByPage(String keyword, Pageable pageable) {
        Page<OperationLog> page = operationLogRepository.findByKeyword(keyword, pageable);
        List<OperationLogDTO> content = page.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public void recordLoginLog(User user, String ipAddress, String userAgent) {
        try {
            OperationLog log = OperationLog.createLoginLog(user, ipAddress, userAgent);
            operationLogRepository.save(log);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
            System.err.println("记录登录日志失败: " + e.getMessage());
        }
    }

    @Override
    public void recordLogoutLog(User user, String ipAddress) {
        try {
            OperationLog log = OperationLog.createLogoutLog(user, ipAddress);
            operationLogRepository.save(log);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
            System.err.println("记录登出日志失败: " + e.getMessage());
        }
    }

    @Override
    public void recordBusinessLog(User user, String operationType, String operationDesc, 
                                 String businessType, Long businessId, String businessNumber, 
                                 Warehouse warehouse, String ipAddress, String userAgent,
                                 String requestUri, String requestMethod, String requestParams) {
        try {
            OperationLog log = OperationLog.createBusinessLog(user, operationType, operationDesc, 
                                                            businessType, businessId, businessNumber, warehouse);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            log.setRequestUri(requestUri);
            log.setRequestMethod(requestMethod);
            log.setRequestParams(requestParams);
            operationLogRepository.save(log);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
            System.err.println("记录业务日志失败: " + e.getMessage());
        }
    }

    @Override
    public void recordSimpleLog(User user, String operationType, String operationDesc) {
        try {
            OperationLog log = new OperationLog(user, operationType, operationDesc);
            operationLogRepository.save(log);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
            System.err.println("记录操作日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationLogDTO> getUserOperationLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return findByOperatorAndTimeBetween(userId, startTime, endTime);
        } else {
            return findByOperator(userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationLogDTO> getRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<OperationLog> page = operationLogRepository.findRecentLogs(pageable);
        return page.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return operationLogRepository.countNotDeleted();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByOperator(Long operatorId) {
        return operationLogRepository.countByOperator(operatorId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByOperationType(String operationType) {
        return operationLogRepository.countByOperationType(operationType);
    }

    /**
     * 转换为DTO
     */
    private OperationLogDTO convertToDTO(OperationLog log) {
        OperationLogDTO dto = new OperationLogDTO();
        BeanUtils.copyProperties(log, dto);

        if (log.getOperator() != null) {
            dto.setOperatorId(log.getOperator().getId());
            dto.setOperatorName(log.getOperator().getRealName() != null ? 
                               log.getOperator().getRealName() : log.getOperator().getUsername());
        }

        if (log.getWarehouse() != null) {
            dto.setWarehouseId(log.getWarehouse().getId());
            dto.setWarehouseName(log.getWarehouse().getName());
        }

        return dto;
    }
}
