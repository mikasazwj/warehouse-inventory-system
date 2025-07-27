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
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND s.plannedDate BETWEEN :startDate AND :endDate ORDER BY s.createdTime DESC")
    List<StocktakeOrder> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计指定仓库和日期范围内的盘点次数
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.deleted = false AND s.warehouse.id = :warehouseId AND s.plannedDate BETWEEN :startDate AND :endDate")
    long countByWarehouseAndDateRange(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计日期范围内的盘点次数
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.deleted = false AND s.plannedDate BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计指定仓库和日期的盘点次数
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.deleted = false AND s.warehouse.id = :warehouseId AND CAST(s.plannedDate AS DATE) = :date")
    long countByWarehouseAndDate(@Param("warehouseId") Long warehouseId, @Param("date") LocalDate date);

    /**
     * 统计指定日期的盘点次数
     */
    @Query("SELECT COUNT(s) FROM StocktakeOrder s WHERE s.deleted = false AND CAST(s.plannedDate AS DATE) = :date")
    long countByDate(@Param("date") LocalDate date);

    /**
     * 查找今日盘点单
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND CAST(s.createdTime AS DATE) = CURRENT_DATE ORDER BY s.createdTime DESC")
    List<StocktakeOrder> findTodayOrders();

    /**
     * 查找逾期未执行的盘点单
     */
    @Query("SELECT s FROM StocktakeOrder s WHERE s.deleted = false AND s.status = 'APPROVED' AND s.plannedDate < CURRENT_DATE AND s.completedTime IS NULL ORDER BY s.plannedDate ASC")
    List<StocktakeOrder> findOverdueOrders();
}
