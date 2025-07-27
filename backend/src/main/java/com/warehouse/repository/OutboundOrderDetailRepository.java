package com.warehouse.repository;

import com.warehouse.entity.OutboundOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 出库单明细数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface OutboundOrderDetailRepository extends JpaRepository<OutboundOrderDetail, Long> {

    /**
     * 根据出库单ID查找明细
     */
    List<OutboundOrderDetail> findByOutboundOrderId(Long outboundOrderId);

    /**
     * 根据出库单ID查找明细（按序号排序）
     */
    List<OutboundOrderDetail> findByOutboundOrderIdOrderByIdAsc(Long outboundOrderId);

    /**
     * 根据货物ID查找明细
     */
    List<OutboundOrderDetail> findByGoodsId(Long goodsId);

    /**
     * 根据批次号查找明细
     */
    List<OutboundOrderDetail> findByBatchNumberContaining(String batchNumber);

    /**
     * 根据出库单ID删除明细
     */
    @Modifying
    @Query("DELETE FROM OutboundOrderDetail d WHERE d.outboundOrder.id = :outboundOrderId")
    void deleteByOutboundOrderId(@Param("outboundOrderId") Long outboundOrderId);

    /**
     * 统计出库单明细数量
     */
    @Query("SELECT COUNT(d) FROM OutboundOrderDetail d WHERE d.outboundOrder.id = :outboundOrderId")
    long countByOutboundOrderId(@Param("outboundOrderId") Long outboundOrderId);

    /**
     * 根据出库单ID计算总金额
     */
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM OutboundOrderDetail d WHERE d.outboundOrder.id = :outboundOrderId")
    java.math.BigDecimal sumAmountByOutboundOrderId(@Param("outboundOrderId") Long outboundOrderId);

    /**
     * 根据出库单ID计算总数量
     */
    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM OutboundOrderDetail d WHERE d.outboundOrder.id = :outboundOrderId")
    java.math.BigDecimal sumQuantityByOutboundOrderId(@Param("outboundOrderId") Long outboundOrderId);
}
