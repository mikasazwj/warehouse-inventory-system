package com.warehouse.repository;

import com.warehouse.entity.StocktakeOrder;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.StocktakeType;
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
 * 盘点单数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface StocktakeOrderRepository extends JpaRepository<StocktakeOrder, Long> {

    /**
     * 根据单号查找盘点单
     */
    Optional<StocktakeOrder> findByOrderNumberAndDeletedFalse(String orderNumber);

    /**
     * 检查单号是否存在
     */
    boolean existsByOrderNumberAndDeletedFalse(String orderNumber);

    /**
     * 查找所有未删除的盘点单
     */
    List<StocktakeOrder> findByDeletedFalseOrderByCreatedTimeDesc();

    /**
     * 分页查找所有未删除的盘点单
     */
    Page<StocktakeOrder> findByDeletedFalse(Pageable pageable);

    /**
     * 根据单号模糊查询并分页
     */
    Page<StocktakeOrder> findByOrderNumberContainingAndDeletedFalse(String orderNumber, Pageable pageable);

    /**
     * 根据仓库查找盘点单
     */
    List<StocktakeOrder> findByWarehouseIdAndDeletedFalseOrderByCreatedTimeDesc(Long warehouseId);

    /**
     * 根据状态查找盘点单
     */
    List<StocktakeOrder> findByStatusAndDeletedFalseOrderByCreatedTimeDesc(ApprovalStatus status);

    /**
     * 根据盘点类型查找盘点单
     */
    List<StocktakeOrder> findByStocktakeTypeAndDeletedFalseOrderByCreatedTimeDesc(StocktakeType stocktakeType);

    /**
     * 根据日期范围查找盘点单
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND s.createdTime >= :startDate AND s.createdTime <= :endDate ORDER BY s.createdTime DESC")
    List<StocktakeOrder> findByDateRangeAndDeletedFalse(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查找待审批的盘点单
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND s.status IN ('PENDING', 'IN_PROGRESS') ORDER BY s.createdTime ASC")
    List<StocktakeOrder> findPendingOrders();

    /**
     * 根据仓库查找待审批的盘点单
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.warehouse.id = :warehouseId AND s.deleted = false AND s.status IN ('PENDING', 'IN_PROGRESS') ORDER BY s.createdTime ASC")
    List<StocktakeOrder> findPendingOrdersByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 查找已审批但未执行的盘点单
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND s.status = 'APPROVED' AND s.completedTime IS NULL ORDER BY s.approvedTime ASC")
    List<StocktakeOrder> findApprovedButNotExecuted();

    /**
     * 分页查询盘点单（支持关键字搜索）
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "s.orderNumber LIKE %:keyword%)")
    Page<StocktakeOrder> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询盘点单（支持仓库和关键字搜索）
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND " +
           "(:warehouseId IS NULL OR s.warehouse.id = :warehouseId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "s.orderNumber LIKE %:keyword%)")
    Page<StocktakeOrder> findByWarehouseAndKeyword(@Param("warehouseId") Long warehouseId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询盘点单（支持状态和关键字搜索）
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "s.orderNumber LIKE %:keyword%)")
    Page<StocktakeOrder> findByStatusAndKeyword(@Param("status") ApprovalStatus status, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页查询盘点单（支持多条件筛选）
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND " +
           "(:orderNumber IS NULL OR :orderNumber = '' OR s.orderNumber LIKE %:orderNumber%) AND " +
           "(:stocktakeType IS NULL OR s.stocktakeType = :stocktakeType) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:warehouseId IS NULL OR s.warehouse.id = :warehouseId) AND " +
           "(:startDate IS NULL OR s.createdTime >= :startDate) AND " +
           "(:endDate IS NULL OR s.createdTime <= :endDate) " +
           "ORDER BY s.createdTime DESC")
    Page<StocktakeOrder> findByFilters(@Param("orderNumber") String orderNumber,
                                     @Param("stocktakeType") StocktakeType stocktakeType,
                                     @Param("status") ApprovalStatus status,
                                     @Param("warehouseId") Long warehouseId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     Pageable pageable);

    /**
     * 根据日期范围查找盘点单
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND s.plannedDate BETWEEN :startDate AND :endDate ORDER BY s.plannedDate DESC")
    List<StocktakeOrder> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据计划日期范围查找盘点单
     */
    List<StocktakeOrder> findByPlannedDateBetweenAndDeletedFalse(LocalDate startDate, LocalDate endDate);

    /**
     * 统计盘点单数量
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.deleted = false")
    long countNotDeleted();

    /**
     * 根据状态统计盘点单数量
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.status = :status AND s.deleted = false")
    long countByStatusAndNotDeleted(@Param("status") ApprovalStatus status);

    /**
     * 根据仓库统计盘点单数量
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.warehouse.id = :warehouseId AND s.deleted = false")
    long countByWarehouseAndNotDeleted(@Param("warehouseId") Long warehouseId);

    /**
     * 统计待审批盘点单数量
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.deleted = false AND s.status IN ('PENDING', 'IN_PROGRESS')")
    long countPendingOrders();

    /**
     * 统计已执行的盘点单数量
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.deleted = false AND s.completedTime IS NOT NULL")
    long countExecutedOrders();

    /**
     * 查找今日盘点单
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND DATE(s.createdTime) = CURRENT_DATE ORDER BY s.createdTime DESC")
    List<StocktakeOrder> findTodayOrders();

    /**
     * 查找逾期未执行的盘点单
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND s.status = 'APPROVED' AND s.plannedDate < CURRENT_DATE AND s.completedTime IS NULL ORDER BY s.plannedDate ASC")
    List<StocktakeOrder> findOverdueOrders();

    /**
     * 根据仓库和日期范围统计盘点次数
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.warehouse.id = :warehouseId AND s.deleted = false AND s.status = 'APPROVED' AND s.actualDate BETWEEN :startDate AND :endDate")
    long countStocktakeByWarehouseAndDateRange(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 生成盘点单号
     */
    @Query("SELECT CONCAT('ST', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(CAST(COALESCE(MAX(CAST(SUBSTRING(s.orderNumber, 11) AS INTEGER)), 0) + 1 AS STRING), 3, '0')) " +
           "FROM StocktakeOrder s WHERE s.orderNumber LIKE CONCAT('ST', DATE_FORMAT(NOW(), '%Y%m%d'), '%')")
    String generateOrderNumber();

    /**
     * 按日期范围统计盘点单数量
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.deleted = false AND DATE(s.createdTime) >= :startDate AND DATE(s.createdTime) <= :endDate")
    long countByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按仓库和日期范围统计盘点单数量
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.warehouse.id = :warehouseId AND s.deleted = false AND DATE(s.createdTime) >= :startDate AND DATE(s.createdTime) <= :endDate")
    long countByWarehouseAndDateRange(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按仓库和日期统计盘点单数量
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.warehouse.id = :warehouseId AND s.deleted = false AND DATE(s.createdTime) = :date")
    long countByWarehouseAndDate(@Param("warehouseId") Long warehouseId, @Param("date") LocalDate date);

    /**
     * 按日期统计盘点单数量
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.deleted = false AND DATE(s.createdTime) = :date")
    long countByDate(@Param("date") LocalDate date);
}
