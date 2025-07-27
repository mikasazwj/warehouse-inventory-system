package com.warehouse.repository;

import com.warehouse.entity.TransferOrder;
import com.warehouse.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 调拨单数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {

    /**
     * 根据单号查找调拨单
     */
    Optional<TransferOrder> findByOrderNumberAndDeletedFalse(String orderNumber);

    /**
     * 检查单号是否存在
     */
    boolean existsByOrderNumberAndDeletedFalse(String orderNumber);

    /**
     * 查找所有未删除的调拨单
     */
    List<TransferOrder> findByDeletedFalseOrderByCreatedTimeDesc();

    /**
     * 根据源仓库查找调拨单
     */
    List<TransferOrder> findByFromWarehouseIdAndDeletedFalseOrderByCreatedTimeDesc(Long fromWarehouseId);

    /**
     * 根据目标仓库查找调拨单
     */
    List<TransferOrder> findByToWarehouseIdAndDeletedFalseOrderByCreatedTimeDesc(Long toWarehouseId);

    /**
     * 根据状态查找调拨单
     */
    List<TransferOrder> findByStatusAndDeletedFalseOrderByCreatedTimeDesc(ApprovalStatus status);

    /**
     * 查找待审批的调拨单
     */
    @Query("SELECT t FROM TransferOrder t WHERE t.deleted = false AND t.status IN ('PENDING', 'IN_PROGRESS') ORDER BY t.createdTime ASC")
    List<TransferOrder> findPendingOrders();

    /**
     * 根据源仓库查找待审批的调拨单
     */
    @Query("SELECT t FROM TransferOrder t WHERE t.fromWarehouse.id = :sourceWarehouseId AND t.deleted = false AND t.status IN ('PENDING', 'IN_PROGRESS') ORDER BY t.createdTime ASC")
    List<TransferOrder> findPendingOrdersBySourceWarehouse(@Param("sourceWarehouseId") Long sourceWarehouseId);

    /**
     * 查找已审批但未执行的调拨单
     */
    @Query("SELECT t FROM TransferOrder t WHERE t.deleted = false AND t.status = 'APPROVED' AND t.operationTime IS NULL ORDER BY t.approvalTime ASC")
    List<TransferOrder> findApprovedButNotExecuted();

    /**
     * 分页查询调拨单（支持关键字搜索）
     */
    @Query("SELECT t FROM TransferOrder t WHERE t.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "t.orderNumber LIKE %:keyword% OR t.remark LIKE %:keyword%)")
    Page<TransferOrder> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询调拨单（支持源仓库和关键字搜索）
     */
    @Query("SELECT t FROM TransferOrder t WHERE t.deleted = false AND " +
           "(:sourceWarehouseId IS NULL OR t.fromWarehouse.id = :sourceWarehouseId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "t.orderNumber LIKE %:keyword% OR t.remark LIKE %:keyword%)")
    Page<TransferOrder> findBySourceWarehouseAndKeyword(@Param("sourceWarehouseId") Long sourceWarehouseId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询调拨单（支持状态和关键字搜索）
     */
    @Query("SELECT t FROM TransferOrder t WHERE t.deleted = false AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "t.orderNumber LIKE %:keyword% OR t.remark LIKE %:keyword%)")
    Page<TransferOrder> findByStatusAndKeyword(@Param("status") ApprovalStatus status, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 多条件分页查询调拨单
     */
    @Query("SELECT t FROM TransferOrder t WHERE t.deleted = false AND " +
           "(:orderNumber IS NULL OR :orderNumber = '' OR t.orderNumber LIKE %:orderNumber%) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:sourceWarehouseId IS NULL OR t.fromWarehouse.id = :sourceWarehouseId) AND " +
           "(:targetWarehouseId IS NULL OR t.toWarehouse.id = :targetWarehouseId)")
    Page<TransferOrder> findByMultipleConditions(@Param("orderNumber") String orderNumber,
                                                @Param("status") ApprovalStatus status,
                                                @Param("sourceWarehouseId") Long sourceWarehouseId,
                                                @Param("targetWarehouseId") Long targetWarehouseId,
                                                Pageable pageable);

    /**
     * 多条件分页查询调拨单（包含日期范围）
     */
    @Query("SELECT t FROM TransferOrder t WHERE t.deleted = false AND " +
           "(:orderNumber IS NULL OR :orderNumber = '' OR t.orderNumber LIKE %:orderNumber%) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:sourceWarehouseId IS NULL OR t.fromWarehouse.id = :sourceWarehouseId) AND " +
           "(:targetWarehouseId IS NULL OR t.toWarehouse.id = :targetWarehouseId) AND " +
           "(:startDate IS NULL OR CAST(t.createdTime AS DATE) >= :startDate) AND " +
           "(:endDate IS NULL OR CAST(t.createdTime AS DATE) <= :endDate)")
    Page<TransferOrder> findByMultipleConditionsWithDateRange(@Param("orderNumber") String orderNumber,
                                                             @Param("status") ApprovalStatus status,
                                                             @Param("sourceWarehouseId") Long sourceWarehouseId,
                                                             @Param("targetWarehouseId") Long targetWarehouseId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate,
                                                             Pageable pageable);

    /**
     * 根据日期范围查找调拨单
     */
    @Query("SELECT t FROM TransferOrder t WHERE t.deleted = false AND t.plannedDate BETWEEN :startDate AND :endDate ORDER BY t.plannedDate DESC")
    List<TransferOrder> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计调拨单数量
     */
    @Query("SELECT COUNT(t) FROM TransferOrder t WHERE t.deleted = false")
    long countNotDeleted();

    /**
     * 根据状态统计调拨单数量
     */
    @Query("SELECT COUNT(t) FROM TransferOrder t WHERE t.status = :status AND t.deleted = false")
    long countByStatusAndNotDeleted(@Param("status") ApprovalStatus status);

    /**
     * 根据源仓库统计调拨单数量
     */
    @Query("SELECT COUNT(t) FROM TransferOrder t WHERE t.fromWarehouse.id = :sourceWarehouseId AND t.deleted = false")
    long countBySourceWarehouseAndNotDeleted(@Param("sourceWarehouseId") Long sourceWarehouseId);

    /**
     * 统计待审批调拨单数量
     */
    @Query("SELECT COUNT(t) FROM TransferOrder t WHERE t.deleted = false AND t.status IN ('PENDING', 'IN_PROGRESS')")
    long countPendingOrders();

    /**
     * 统计已执行的调拨单数量
     */
    @Query("SELECT COUNT(t) FROM TransferOrder t WHERE t.deleted = false AND t.operationTime IS NOT NULL")
    long countExecutedOrders();

    /**
     * 查找今日调拨单 - H2/MySQL兼容版本
     */
    @Query("SELECT t FROM TransferOrder t WHERE t.deleted = false AND CAST(t.createdTime AS DATE) = CURRENT_DATE ORDER BY t.createdTime DESC")
    List<TransferOrder> findTodayOrders();

    /**
     * 查找逾期未执行的调拨单 - H2/MySQL兼容版本
     */
    @Query("SELECT t FROM TransferOrder t WHERE t.deleted = false AND t.status = 'APPROVED' AND t.plannedDate < CURRENT_DATE AND t.operationTime IS NULL ORDER BY t.plannedDate ASC")
    List<TransferOrder> findOverdueOrders();

    /**
     * 根据仓库和日期范围统计调拨数量（作为源仓库）
     */
    @Query("SELECT COUNT(t) FROM TransferOrder t WHERE t.fromWarehouse.id = :warehouseId AND t.deleted = false AND t.status = 'APPROVED' AND t.actualDate BETWEEN :startDate AND :endDate")
    long countTransferOutByWarehouseAndDateRange(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据仓库和日期范围统计调拨数量（作为目标仓库）
     */
    @Query("SELECT COUNT(t) FROM TransferOrder t WHERE t.toWarehouse.id = :warehouseId AND t.deleted = false AND t.status = 'APPROVED' AND t.actualDate BETWEEN :startDate AND :endDate")
    long countTransferInByWarehouseAndDateRange(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 生成调拨单号 - 暂时禁用，使用Java代码生成
     */
    // @Query("SELECT CONCAT('TR', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(CAST(COALESCE(MAX(CAST(SUBSTRING(t.orderNumber, 11) AS INTEGER)), 0) + 1 AS STRING), 3, '0')) " +
    //        "FROM TransferOrder t WHERE t.orderNumber LIKE CONCAT('TR', DATE_FORMAT(NOW(), '%Y%m%d'), '%')")
    // String generateOrderNumber();

    /**
     * 根据订单号前缀查找订单，按订单号降序排列
     */
    List<TransferOrder> findByOrderNumberStartingWithOrderByOrderNumberDesc(String prefix);

    /**
     * 按日期范围统计调拨单数量 - H2/MySQL兼容版本
     */
    @Query("SELECT COUNT(t) FROM TransferOrder t WHERE t.deleted = false AND CAST(t.createdTime AS DATE) >= :startDate AND CAST(t.createdTime AS DATE) <= :endDate")
    long countByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按仓库和日期范围统计调拨单数量（源仓库或目标仓库） - H2/MySQL兼容版本
     */
    @Query("SELECT COUNT(t) FROM TransferOrder t WHERE (t.fromWarehouse.id = :warehouseId OR t.toWarehouse.id = :warehouseId) AND t.deleted = false AND CAST(t.createdTime AS DATE) >= :startDate AND CAST(t.createdTime AS DATE) <= :endDate")
    long countByWarehouseAndDateRange(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按日期统计调拨单数量 - H2/MySQL兼容版本
     */
    @Query("SELECT COUNT(t) FROM TransferOrder t WHERE t.deleted = false AND CAST(t.createdTime AS DATE) = :date")
    long countByDate(@Param("date") LocalDate date);

    /**
     * 按仓库和日期统计调拨单数量（源仓库或目标仓库） - H2/MySQL兼容版本
     */
    @Query("SELECT COUNT(t) FROM TransferOrder t WHERE (t.fromWarehouse.id = :warehouseId OR t.toWarehouse.id = :warehouseId) AND t.deleted = false AND CAST(t.createdTime AS DATE) = :date")
    long countByWarehouseAndDate(@Param("warehouseId") Long warehouseId, @Param("date") LocalDate date);
}
