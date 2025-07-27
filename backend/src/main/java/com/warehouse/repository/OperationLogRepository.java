package com.warehouse.repository;

import com.warehouse.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    /**
     * 根据操作人查找日志
     */
    List<OperationLog> findByOperatorIdAndDeletedFalseOrderByOperationTimeDesc(Long operatorId);

    /**
     * 根据操作类型查找日志
     */
    List<OperationLog> findByOperationTypeAndDeletedFalseOrderByOperationTimeDesc(String operationType);

    /**
     * 根据业务类型和业务ID查找日志
     */
    List<OperationLog> findByBusinessTypeAndBusinessIdAndDeletedFalseOrderByOperationTimeDesc(
            String businessType, Long businessId);

    /**
     * 根据仓库查找日志
     */
    List<OperationLog> findByWarehouseIdAndDeletedFalseOrderByOperationTimeDesc(Long warehouseId);

    /**
     * 根据时间范围查找日志
     */
    @Query("SELECT ol FROM OperationLog ol WHERE ol.deleted = false AND " +
           "ol.operationTime >= :startTime AND ol.operationTime <= :endTime " +
           "ORDER BY ol.operationTime DESC")
    List<OperationLog> findByOperationTimeBetween(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 根据操作人和时间范围查找日志
     */
    @Query("SELECT ol FROM OperationLog ol WHERE ol.deleted = false AND " +
           "ol.operator.id = :operatorId AND " +
           "ol.operationTime >= :startTime AND ol.operationTime <= :endTime " +
           "ORDER BY ol.operationTime DESC")
    List<OperationLog> findByOperatorAndTimeBetween(@Param("operatorId") Long operatorId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 分页查询操作日志
     */
    @Query("SELECT ol FROM OperationLog ol WHERE ol.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "ol.operationType LIKE %:keyword% OR ol.operationDesc LIKE %:keyword%) " +
           "ORDER BY ol.operationTime DESC")
    Page<OperationLog> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据库存相关操作查找日志
     */
    @Query("SELECT ol FROM OperationLog ol WHERE ol.deleted = false AND " +
           "ol.businessType = 'INVENTORY' AND ol.businessId = :inventoryId " +
           "ORDER BY ol.operationTime DESC")
    List<OperationLog> findInventoryOperationLogs(@Param("inventoryId") Long inventoryId);

    /**
     * 根据库存相关操作查找日志（包括入库、出库、调整）
     */
    @Query("SELECT ol FROM OperationLog ol WHERE ol.deleted = false AND " +
           "(ol.businessType = 'INVENTORY' AND ol.businessId = :inventoryId) OR " +
           "(ol.businessType IN ('INBOUND', 'OUTBOUND', 'ADJUST') AND " +
           "ol.requestParams LIKE CONCAT('%inventoryId\":', :inventoryId, '%')) " +
           "ORDER BY ol.operationTime DESC")
    List<OperationLog> findAllInventoryRelatedLogs(@Param("inventoryId") Long inventoryId);

    /**
     * 统计操作日志数量
     */
    @Query("SELECT COUNT(ol) FROM OperationLog ol WHERE ol.deleted = false")
    long countNotDeleted();

    /**
     * 根据操作人统计日志数量
     */
    @Query("SELECT COUNT(ol) FROM OperationLog ol WHERE ol.deleted = false AND ol.operator.id = :operatorId")
    long countByOperator(@Param("operatorId") Long operatorId);

    /**
     * 根据操作类型统计日志数量
     */
    @Query("SELECT COUNT(ol) FROM OperationLog ol WHERE ol.deleted = false AND ol.operationType = :operationType")
    long countByOperationType(@Param("operationType") String operationType);

    /**
     * 查找最近的操作日志
     */
    @Query("SELECT ol FROM OperationLog ol WHERE ol.deleted = false " +
           "ORDER BY ol.operationTime DESC")
    Page<OperationLog> findRecentLogs(Pageable pageable);
}
