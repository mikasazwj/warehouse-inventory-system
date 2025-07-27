package com.warehouse.repository;

import com.warehouse.entity.StocktakeOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
     * 根据盘点单ID删除明细
     */
    @Modifying
    @Query("DELETE FROM StocktakeOrderDetail d WHERE d.stocktakeOrder.id = :stocktakeOrderId")
    void deleteByStocktakeOrderId(@Param("stocktakeOrderId") Long stocktakeOrderId);

    /**
     * 统计盘点单明细数量
     */
    @Query("SELECT COUNT(d) FROM StocktakeOrderDetail d WHERE d.stocktakeOrder.id = :stocktakeOrderId")
    long countByStocktakeOrderId(@Param("stocktakeOrderId") Long stocktakeOrderId);

    /**
     * 根据盘点单ID计算账面总数量
     */
    @Query("SELECT COALESCE(SUM(d.bookQuantity), 0) FROM StocktakeOrderDetail d WHERE d.stocktakeOrder.id = :stocktakeOrderId")
    java.math.BigDecimal sumBookQuantityByStocktakeOrderId(@Param("stocktakeOrderId") Long stocktakeOrderId);

    /**
     * 根据盘点单ID计算实盘总数量
     */
    @Query("SELECT COALESCE(SUM(d.actualQuantity), 0) FROM StocktakeOrderDetail d WHERE d.stocktakeOrder.id = :stocktakeOrderId")
    java.math.BigDecimal sumActualQuantityByStocktakeOrderId(@Param("stocktakeOrderId") Long stocktakeOrderId);

    /**
     * 根据盘点单ID计算差异总数量
     */
    @Query("SELECT COALESCE(SUM(d.differenceQuantity), 0) FROM StocktakeOrderDetail d WHERE d.stocktakeOrder.id = :stocktakeOrderId")
    java.math.BigDecimal sumDifferenceQuantityByStocktakeOrderId(@Param("stocktakeOrderId") Long stocktakeOrderId);

    /**
     * 查找有差异的明细
     */
    @Query("SELECT d FROM StocktakeOrderDetail d WHERE d.stocktakeOrder.id = :stocktakeOrderId AND d.differenceQuantity != 0")
    List<StocktakeOrderDetail> findDifferenceDetailsByStocktakeOrderId(@Param("stocktakeOrderId") Long stocktakeOrderId);
}
