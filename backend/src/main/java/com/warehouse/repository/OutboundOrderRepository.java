package com.warehouse.repository;

import com.warehouse.entity.OutboundOrder;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 出库单数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface OutboundOrderRepository extends JpaRepository<OutboundOrder, Long> {

    /**
     * 根据单号查找出库单
     */
    Optional<OutboundOrder> findByOrderNumberAndDeletedFalse(String orderNumber);

    /**
     * 检查单号是否存在
     */
    boolean existsByOrderNumberAndDeletedFalse(String orderNumber);

    /**
     * 根据仓库查找出库单
     */
    List<OutboundOrder> findByWarehouseIdAndDeletedFalseOrderByCreatedTimeDesc(Long warehouseId);

    /**
     * 根据客户查找出库单
     */
    List<OutboundOrder> findByCustomerIdAndDeletedFalseOrderByCreatedTimeDesc(Long customerId);

    /**
     * 根据状态查找出库单
     */
    List<OutboundOrder> findByStatusAndDeletedFalseOrderByCreatedTimeDesc(ApprovalStatus status);

    /**
     * 根据业务类型查找出库单
     */
    List<OutboundOrder> findByBusinessTypeAndDeletedFalseOrderByCreatedTimeDesc(BusinessType businessType);

    /**
     * 查找待审批的出库单
     */
    @Query("SELECT o FROM OutboundOrder o WHERE o.deleted = false AND o.status IN ('PENDING', 'IN_PROGRESS') ORDER BY o.createdTime ASC")
    List<OutboundOrder> findPendingOrders();

    /**
     * 根据仓库查找待审批的出库单
     */
    @Query("SELECT o FROM OutboundOrder o WHERE o.warehouse.id = :warehouseId AND o.deleted = false AND o.status IN ('PENDING', 'IN_PROGRESS') ORDER BY o.createdTime ASC")
    List<OutboundOrder> findPendingOrdersByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 查找已审批但未执行的出库单
     */
    @Query("SELECT o FROM OutboundOrder o WHERE o.deleted = false AND o.status = 'APPROVED' AND o.operationTime IS NULL ORDER BY o.approvalTime ASC")
    List<OutboundOrder> findApprovedButNotExecuted();

    /**
     * 分页查询出库单（支持关键字搜索）
     */
    @Query("SELECT o FROM OutboundOrder o WHERE o.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "o.orderNumber LIKE %:keyword% OR o.referenceNumber LIKE %:keyword% OR o.remark LIKE %:keyword%)")
    Page<OutboundOrder> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询出库单（支持多条件筛选）
     */
    @Query("SELECT o FROM OutboundOrder o WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "o.orderNumber LIKE CONCAT('%', :keyword, '%') OR o.referenceNumber LIKE CONCAT('%', :keyword, '%') OR o.remark LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:orderNumber IS NULL OR :orderNumber = '' OR o.orderNumber LIKE CONCAT('%', :orderNumber, '%')) AND " +
           "(:warehouseId IS NULL OR o.warehouse.id = :warehouseId) AND " +
           "(:businessType IS NULL OR o.businessType = :businessType) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:startDate IS NULL OR :startDate = '' OR o.plannedDate >= CAST(:startDate AS date)) AND " +
           "(:endDate IS NULL OR :endDate = '' OR o.plannedDate <= CAST(:endDate AS date))")
    Page<OutboundOrder> findByFilters(@Param("keyword") String keyword,
                                    @Param("orderNumber") String orderNumber,
                                    @Param("warehouseId") Long warehouseId,
                                    @Param("businessType") BusinessType businessType,
                                    @Param("status") ApprovalStatus status,
                                    @Param("startDate") String startDate,
                                    @Param("endDate") String endDate,
                                    Pageable pageable);

    /**
     * 分页查询出库单（支持仓库和关键字搜索）
     */
    @Query("SELECT o FROM OutboundOrder o WHERE o.deleted = false AND " +
           "(:warehouseId IS NULL OR o.warehouse.id = :warehouseId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "o.orderNumber LIKE %:keyword% OR o.referenceNumber LIKE %:keyword% OR o.remark LIKE %:keyword%)")
    Page<OutboundOrder> findByWarehouseAndKeyword(@Param("warehouseId") Long warehouseId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询出库单（支持状态和关键字搜索）
     */
    @Query("SELECT o FROM OutboundOrder o WHERE o.deleted = false AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "o.orderNumber LIKE %:keyword% OR o.referenceNumber LIKE %:keyword% OR o.remark LIKE %:keyword%)")
    Page<OutboundOrder> findByStatusAndKeyword(@Param("status") ApprovalStatus status, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据日期范围查找出库单
     */
    @Query("SELECT o FROM OutboundOrder o WHERE o.deleted = false AND o.plannedDate BETWEEN :startDate AND :endDate ORDER BY o.plannedDate DESC")
    List<OutboundOrder> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计出库单数量
     */
    @Query("SELECT COUNT(o) FROM OutboundOrder o WHERE o.deleted = false")
    long countNotDeleted();

    /**
     * 根据状态统计出库单数量
     */
    @Query("SELECT COUNT(o) FROM OutboundOrder o WHERE o.status = :status AND o.deleted = false")
    long countByStatusAndNotDeleted(@Param("status") ApprovalStatus status);

    /**
     * 根据仓库统计出库单数量
     */
    @Query("SELECT COUNT(o) FROM OutboundOrder o WHERE o.warehouse.id = :warehouseId AND o.deleted = false")
    long countByWarehouseAndNotDeleted(@Param("warehouseId") Long warehouseId);

    /**
     * 统计待审批出库单数量
     */
    @Query("SELECT COUNT(o) FROM OutboundOrder o WHERE o.deleted = false AND o.status IN ('PENDING', 'IN_PROGRESS')")
    long countPendingOrders();

    /**
     * 查找今日出库单
     */
    @Query("SELECT o FROM OutboundOrder o WHERE o.deleted = false AND DATE(o.createdTime) = CURRENT_DATE ORDER BY o.createdTime DESC")
    List<OutboundOrder> findTodayOrders();

    /**
     * 查找逾期未执行的出库单
     */
    @Query("SELECT o FROM OutboundOrder o WHERE o.deleted = false AND o.status = 'APPROVED' AND o.plannedDate < CURRENT_DATE AND o.operationTime IS NULL ORDER BY o.plannedDate ASC")
    List<OutboundOrder> findOverdueOrders();

    /**
     * 根据客户和日期范围统计出库金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OutboundOrder o WHERE o.customer.id = :customerId AND o.deleted = false AND o.status = 'APPROVED' AND o.actualDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumAmountByCustomerAndDateRange(@Param("customerId") Long customerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据仓库和日期范围统计出库金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OutboundOrder o WHERE o.warehouse.id = :warehouseId AND o.deleted = false AND o.status = 'APPROVED' AND o.actualDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumAmountByWarehouseAndDateRange(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 生成出库单号
     */
    @Query("SELECT CONCAT('OUT', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(CAST(COALESCE(MAX(CAST(SUBSTRING(o.orderNumber, 12) AS INTEGER)), 0) + 1 AS STRING), 3, '0')) " +
           "FROM OutboundOrder o WHERE o.orderNumber LIKE CONCAT('OUT', DATE_FORMAT(NOW(), '%Y%m%d'), '%')")
    String generateOrderNumber();

    /**
     * 查找所有未删除的出库单
     */
    List<OutboundOrder> findByDeletedFalseOrderByCreatedTimeDesc();

    /**
     * 统计已执行的出库单数量
     */
    @Query("SELECT COUNT(o) FROM OutboundOrder o WHERE o.deleted = false AND o.operationTime IS NOT NULL")
    long countExecutedOrders();

    /**
     * 计算总金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OutboundOrder o WHERE o.deleted = false")
    java.math.BigDecimal sumTotalAmount();

    /**
     * 计算已执行的总金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OutboundOrder o WHERE o.deleted = false AND o.operationTime IS NOT NULL")
    java.math.BigDecimal sumExecutedAmount();

    // ===== 仪表盘专用查询方法 =====

    /**
     * 根据状态统计出库单数量
     */
    @Query("SELECT COUNT(o) FROM OutboundOrder o WHERE o.status = :status")
    long countByStatus(@Param("status") ApprovalStatus status);

    /**
     * 根据日期和仓库统计出库单数量
     */
    @Query("SELECT COUNT(o) FROM OutboundOrder o WHERE DATE(o.createdTime) = :date AND o.warehouse.id = :warehouseId")
    long countByDateAndWarehouse(@Param("date") LocalDate date, @Param("warehouseId") Long warehouseId);

    /**
     * 根据日期统计出库单数量
     */
    @Query("SELECT COUNT(o) FROM OutboundOrder o WHERE DATE(o.createdTime) = :date")
    long countByDate(@Param("date") LocalDate date);

    // 简化的查询方法，直接返回实体对象
    List<OutboundOrder> findByStatus(ApprovalStatus status);
    List<OutboundOrder> findByWarehouseIdAndStatus(Long warehouseId, ApprovalStatus status);
    List<OutboundOrder> findTop5ByOrderByCreatedTimeDesc();
    List<OutboundOrder> findTop5ByWarehouseIdOrderByCreatedTimeDesc(Long warehouseId);

    /**
     * 按日期范围统计出库单数量
     */
    @Query("SELECT COUNT(o) FROM OutboundOrder o WHERE o.deleted = false AND DATE(o.createdTime) >= :startDate AND DATE(o.createdTime) <= :endDate")
    long countByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按仓库和日期范围统计出库单数量
     */
    @Query("SELECT COUNT(o) FROM OutboundOrder o WHERE o.warehouse.id = :warehouseId AND o.deleted = false AND DATE(o.createdTime) >= :startDate AND DATE(o.createdTime) <= :endDate")
    long countByWarehouseAndDateRange(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按仓库和日期统计出库单数量
     */
    @Query("SELECT COUNT(o) FROM OutboundOrder o WHERE o.warehouse.id = :warehouseId AND o.deleted = false AND DATE(o.createdTime) = :date")
    long countByWarehouseAndDate(@Param("warehouseId") Long warehouseId, @Param("date") LocalDate date);

    /**
     * 按日期范围统计出库金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OutboundOrder o WHERE o.deleted = false AND DATE(o.createdTime) >= :startDate AND DATE(o.createdTime) <= :endDate")
    java.math.BigDecimal sumAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按仓库和日期统计出库金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OutboundOrder o WHERE o.warehouse.id = :warehouseId AND o.deleted = false AND DATE(o.createdTime) = :date")
    java.math.BigDecimal sumAmountByWarehouseAndDate(@Param("warehouseId") Long warehouseId, @Param("date") LocalDate date);

    /**
     * 按日期统计出库金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OutboundOrder o WHERE o.deleted = false AND DATE(o.createdTime) = :date")
    java.math.BigDecimal sumAmountByDate(@Param("date") LocalDate date);

}
