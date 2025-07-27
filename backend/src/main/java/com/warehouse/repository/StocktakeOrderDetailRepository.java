package com.warehouse.repository;

import com.warehouse.entity.StocktakeOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 盘点单明细数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface StocktakeOrderDetailRepository extends JpaRepository<StocktakeOrderDetail, Long> {

    /**
     * 根据盘点单ID查找明细
     */
    List<StocktakeOrderDetail> findByStocktakeOrderId(Long stocktakeOrderId);

    /**
     * 根据盘点单ID查找明细（按序号排序）
     */
    List<StocktakeOrderDetail> findByStocktakeOrderIdOrderByIdAsc(Long stocktakeOrderId);

    /**
     * 根据货物ID查找明细
     */
    List<StocktakeOrderDetail> findByGoodsId(Long goodsId);

    /**
     * 根据批次号查找明细
     */
    List<StocktakeOrderDetail> findByBatchNumberContaining(String batchNumber);

    /**
     * 查找有差异的明细
     */
    @Query("SELECT d FROM StocktakeOrderDetail d WHERE d.stocktakeOrder.id = :stocktakeOrderId AND d.differenceQuantity != 0")
    List<StocktakeOrderDetail> findDifferencesByStocktakeOrderId(@Param("stocktakeOrderId") Long stocktakeOrderId);

    /**
     * 查找已完成的明细
     */
    @Query("SELECT d FROM StocktakeOrderDetail d WHERE d.stocktakeOrder.id = :stocktakeOrderId AND d.isCompleted = true")
    List<StocktakeOrderDetail> findCompletedByStocktakeOrderId(@Param("stocktakeOrderId") Long stocktakeOrderId);

    /**
     * 查找未完成的明细
     */
    @Query("SELECT d FROM StocktakeOrderDetail d WHERE d.stocktakeOrder.id = :stocktakeOrderId AND d.isCompleted = false")
    List<StocktakeOrderDetail> findIncompleteByStocktakeOrderId(@Param("stocktakeOrderId") Long stocktakeOrderId);

    /**
     * 统计盘点单的明细数量
     */
    long countByStocktakeOrderId(Long stocktakeOrderId);

    /**
     * 统计已完成的明细数量
     */
    @Query("SELECT COUNT(d) FROM StocktakeOrderDetail d WHERE d.stocktakeOrder.id = :stocktakeOrderId AND d.isCompleted = true")
    long countCompletedByStocktakeOrderId(@Param("stocktakeOrderId") Long stocktakeOrderId);

    /**
     * 统计有差异的明细数量
     */
    @Query("SELECT COUNT(d) FROM StocktakeOrderDetail d WHERE d.stocktakeOrder.id = :stocktakeOrderId AND d.differenceQuantity != 0")
    long countDifferencesByStocktakeOrderId(@Param("stocktakeOrderId") Long stocktakeOrderId);
}
