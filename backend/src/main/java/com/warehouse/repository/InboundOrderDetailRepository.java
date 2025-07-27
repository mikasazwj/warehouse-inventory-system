package com.warehouse.repository;

import com.warehouse.entity.InboundOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 入库单明细数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface InboundOrderDetailRepository extends JpaRepository<InboundOrderDetail, Long> {

    /**
     * 根据入库单ID查找明细
     */
    List<InboundOrderDetail> findByInboundOrderId(Long inboundOrderId);

    /**
     * 根据入库单ID查找明细（按序号排序）
     */
    List<InboundOrderDetail> findByInboundOrderIdOrderByIdAsc(Long inboundOrderId);

    /**
     * 根据货物ID查找明细
     */
    List<InboundOrderDetail> findByGoodsId(Long goodsId);

    /**
     * 根据批次号查找明细
     */
    List<InboundOrderDetail> findByBatchNumberContaining(String batchNumber);

    /**
     * 根据入库单ID删除明细
     */
    @Modifying
    @Query("DELETE FROM InboundOrderDetail d WHERE d.inboundOrder.id = :inboundOrderId")
    void deleteByInboundOrderId(@Param("inboundOrderId") Long inboundOrderId);

    /**
     * 统计入库单明细数量
     */
    @Query("SELECT COUNT(d) FROM InboundOrderDetail d WHERE d.inboundOrder.id = :inboundOrderId")
    long countByInboundOrderId(@Param("inboundOrderId") Long inboundOrderId);

    /**
     * 根据入库单ID计算总金额
     */
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM InboundOrderDetail d WHERE d.inboundOrder.id = :inboundOrderId")
    java.math.BigDecimal sumAmountByInboundOrderId(@Param("inboundOrderId") Long inboundOrderId);

    /**
     * 根据入库单ID计算总数量
     */
    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM InboundOrderDetail d WHERE d.inboundOrder.id = :inboundOrderId")
    java.math.BigDecimal sumQuantityByInboundOrderId(@Param("inboundOrderId") Long inboundOrderId);
}
