package com.warehouse.repository;

import com.warehouse.entity.InboundOrder;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 入库单数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {

    /**
     * 根据单号查找入库单
     */
    Optional<InboundOrder> findByOrderNumberAndDeletedFalse(String orderNumber);

    /**
     * 检查单号是否存在
     */
    boolean existsByOrderNumberAndDeletedFalse(String orderNumber);

    /**
     * 根据仓库查找入库单
     */
    List<InboundOrder> findByWarehouseIdAndDeletedFalseOrderByCreatedTimeDesc(Long warehouseId);



    /**
     * 根据状态查找入库单
     */
    List<InboundOrder> findByStatusAndDeletedFalseOrderByCreatedTimeDesc(ApprovalStatus status);

    /**
     * 根据业务类型查找入库单
     */
    List<InboundOrder> findByBusinessTypeAndDeletedFalseOrderByCreatedTimeDesc(BusinessType businessType);

    /**
     * 根据申请人查找入库单
     */
    List<InboundOrder> findByApplicantIdAndDeletedFalseOrderByCreatedTimeDesc(Long applicantId);

    /**
     * 根据审批人查找入库单
     */
    List<InboundOrder> findByApproverIdAndDeletedFalseOrderByCreatedTimeDesc(Long approverId);

    /**
     * 查找待审批的入库单
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.deleted = false AND i.status IN ('PENDING', 'IN_PROGRESS') ORDER BY i.createdTime ASC")
    List<InboundOrder> findPendingOrders();

    /**
     * 根据仓库查找待审批的入库单
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.status IN ('PENDING', 'IN_PROGRESS') ORDER BY i.createdTime ASC")
    List<InboundOrder> findPendingOrdersByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 查找已审批但未执行的入库单
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.deleted = false AND i.status = 'APPROVED' AND i.operationTime IS NULL ORDER BY i.approvalTime ASC")
    List<InboundOrder> findApprovedButNotExecuted();

    /**
     * 分页查询入库单（支持关键字搜索）
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "i.orderNumber LIKE %:keyword% OR i.referenceNumber LIKE %:keyword% OR i.remark LIKE %:keyword%)")
    Page<InboundOrder> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询入库单（支持多条件筛选）
     */
    @Query("SELECT i FROM InboundOrder i WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "i.orderNumber LIKE CONCAT('%', :keyword, '%') OR i.referenceNumber LIKE CONCAT('%', :keyword, '%') OR i.remark LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:warehouseId IS NULL OR i.warehouse.id = :warehouseId) AND " +
           "(:businessType IS NULL OR i.businessType = :businessType) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:startDate IS NULL OR :startDate = '' OR i.plannedDate >= CAST(:startDate AS date)) AND " +
           "(:endDate IS NULL OR :endDate = '' OR i.plannedDate <= CAST(:endDate AS date))")
    Page<InboundOrder> findByFilters(@Param("keyword") String keyword,
                                   @Param("warehouseId") Long warehouseId,
                                   @Param("businessType") BusinessType businessType,
                                   @Param("status") ApprovalStatus status,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate,
                                   Pageable pageable);

    /**
     * 分页查询入库单（支持仓库和关键字搜索）
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.deleted = false AND " +
           "(:warehouseId IS NULL OR i.warehouse.id = :warehouseId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "i.orderNumber LIKE %:keyword% OR i.referenceNumber LIKE %:keyword% OR i.remark LIKE %:keyword%)")
    Page<InboundOrder> findByWarehouseAndKeyword(@Param("warehouseId") Long warehouseId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询入库单（支持状态和关键字搜索）
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.deleted = false AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "i.orderNumber LIKE %:keyword% OR i.referenceNumber LIKE %:keyword% OR i.remark LIKE %:keyword%)")
    Page<InboundOrder> findByStatusAndKeyword(@Param("status") ApprovalStatus status, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询入库单（支持业务类型和关键字搜索）
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.deleted = false AND " +
           "(:businessType IS NULL OR i.businessType = :businessType) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "i.orderNumber LIKE %:keyword% OR i.referenceNumber LIKE %:keyword% OR i.remark LIKE %:keyword%)")
    Page<InboundOrder> findByBusinessTypeAndKeyword(@Param("businessType") BusinessType businessType, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据日期范围查找入库单
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.deleted = false AND i.plannedDate BETWEEN :startDate AND :endDate ORDER BY i.plannedDate DESC")
    List<InboundOrder> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据仓库和日期范围查找入库单
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.plannedDate BETWEEN :startDate AND :endDate ORDER BY i.plannedDate DESC")
    List<InboundOrder> findByWarehouseAndDateRange(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计入库单数量
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE i.deleted = false")
    long countNotDeleted();

    /**
     * 根据状态统计入库单数量
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE i.status = :status AND i.deleted = false")
    long countByStatusAndNotDeleted(@Param("status") ApprovalStatus status);

    /**
     * 根据仓库统计入库单数量
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE i.warehouse.id = :warehouseId AND i.deleted = false")
    long countByWarehouseAndNotDeleted(@Param("warehouseId") Long warehouseId);

    /**
     * 根据业务类型统计入库单数量
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE i.businessType = :businessType AND i.deleted = false")
    long countByBusinessTypeAndNotDeleted(@Param("businessType") BusinessType businessType);

    /**
     * 统计待审批入库单数量
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE i.deleted = false AND i.status IN ('PENDING', 'IN_PROGRESS')")
    long countPendingOrders();

    /**
     * 根据仓库统计待审批入库单数量
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.status IN ('PENDING', 'IN_PROGRESS')")
    long countPendingOrdersByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 查找今日入库单 - H2/MySQL兼容版本
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.deleted = false AND CAST(i.createdTime AS DATE) = CURRENT_DATE ORDER BY i.createdTime DESC")
    List<InboundOrder> findTodayOrders();

    /**
     * 查找本周入库单 - H2/MySQL兼容版本（使用日期范围替代WEEK函数）
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.deleted = false AND i.createdTime >= :weekStart AND i.createdTime < :weekEnd ORDER BY i.createdTime DESC")
    List<InboundOrder> findThisWeekOrders(@Param("weekStart") LocalDateTime weekStart, @Param("weekEnd") LocalDateTime weekEnd);

    /**
     * 查找本月入库单 - H2/MySQL兼容版本（使用日期范围替代MONTH函数）
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.deleted = false AND i.createdTime >= :monthStart AND i.createdTime < :monthEnd ORDER BY i.createdTime DESC")
    List<InboundOrder> findThisMonthOrders(@Param("monthStart") LocalDateTime monthStart, @Param("monthEnd") LocalDateTime monthEnd);

    /**
     * 查找逾期未执行的入库单 - H2/MySQL兼容版本
     */
    @Query("SELECT i FROM InboundOrder i WHERE i.deleted = false AND i.status = 'APPROVED' AND i.plannedDate < CURRENT_DATE AND i.operationTime IS NULL ORDER BY i.plannedDate ASC")
    List<InboundOrder> findOverdueOrders();



    /**
     * 根据仓库和日期范围统计入库金额
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM InboundOrder i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND i.status = 'APPROVED' AND i.actualDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumAmountByWarehouseAndDateRange(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 生成入库单号 - 暂时禁用，使用Java代码生成
     */
    // @Query("SELECT CONCAT('IN', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(CAST(COALESCE(MAX(CAST(SUBSTRING(i.orderNumber, 11) AS INTEGER)), 0) + 1 AS STRING), 3, '0')) " +
    //        "FROM InboundOrder i WHERE i.orderNumber LIKE CONCAT('IN', DATE_FORMAT(NOW(), '%Y%m%d'), '%')")
    // String generateOrderNumber();

    /**
     * 根据订单号前缀查找订单，按订单号降序排列
     */
    List<InboundOrder> findByOrderNumberStartingWithOrderByOrderNumberDesc(String prefix);



    // ===== 仪表盘专用查询方法 =====

    /**
     * 根据状态统计入库单数量
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE i.status = :status")
    long countByStatus(@Param("status") ApprovalStatus status);

    /**
     * 根据日期和仓库统计入库单数量 - H2兼容版本
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE CAST(i.createdTime AS DATE) = :date AND i.warehouse.id = :warehouseId")
    long countByDateAndWarehouse(@Param("date") LocalDate date, @Param("warehouseId") Long warehouseId);

    /**
     * 根据日期统计入库单数量 - H2兼容版本
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE CAST(i.createdTime AS DATE) = :date")
    long countByDate(@Param("date") LocalDate date);

    // 简化的查询方法，直接返回实体对象
    List<InboundOrder> findByStatus(ApprovalStatus status);
    List<InboundOrder> findByWarehouseIdAndStatus(Long warehouseId, ApprovalStatus status);
    List<InboundOrder> findTop5ByOrderByCreatedTimeDesc();
    List<InboundOrder> findTop5ByWarehouseIdOrderByCreatedTimeDesc(Long warehouseId);

    /**
     * 查找所有未删除的入库单
     */
    List<InboundOrder> findByDeletedFalseOrderByCreatedTimeDesc();

    /**
     * 统计已执行的入库单数量
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE i.deleted = false AND i.operationTime IS NOT NULL")
    long countExecutedOrders();

    /**
     * 计算总金额
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM InboundOrder i WHERE i.deleted = false")
    java.math.BigDecimal sumTotalAmount();

    /**
     * 计算已执行的总金额
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM InboundOrder i WHERE i.deleted = false AND i.operationTime IS NOT NULL")
    java.math.BigDecimal sumExecutedAmount();

    /**
     * 按日期范围统计入库单数量 - H2/MySQL兼容版本
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE i.deleted = false AND CAST(i.createdTime AS DATE) >= :startDate AND CAST(i.createdTime AS DATE) <= :endDate")
    long countByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按仓库和日期范围统计入库单数量 - H2/MySQL兼容版本
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND CAST(i.createdTime AS DATE) >= :startDate AND CAST(i.createdTime AS DATE) <= :endDate")
    long countByWarehouseAndDateRange(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按仓库和日期统计入库单数量 - H2/MySQL兼容版本
     */
    @Query("SELECT COUNT(i) FROM InboundOrder i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND CAST(i.createdTime AS DATE) = :date")
    long countByWarehouseAndDate(@Param("warehouseId") Long warehouseId, @Param("date") LocalDate date);

    /**
     * 按日期范围统计入库金额 - H2/MySQL兼容版本
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM InboundOrder i WHERE i.deleted = false AND CAST(i.createdTime AS DATE) >= :startDate AND CAST(i.createdTime AS DATE) <= :endDate")
    java.math.BigDecimal sumAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按仓库和日期统计入库金额 - H2/MySQL兼容版本
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM InboundOrder i WHERE i.warehouse.id = :warehouseId AND i.deleted = false AND CAST(i.createdTime AS DATE) = :date")
    java.math.BigDecimal sumAmountByWarehouseAndDate(@Param("warehouseId") Long warehouseId, @Param("date") LocalDate date);

    /**
     * 按日期统计入库金额 - H2/MySQL兼容版本
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM InboundOrder i WHERE i.deleted = false AND CAST(i.createdTime AS DATE) = :date")
    java.math.BigDecimal sumAmountByDate(@Param("date") LocalDate date);

}
