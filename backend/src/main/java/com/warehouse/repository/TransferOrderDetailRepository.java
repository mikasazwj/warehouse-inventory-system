package com.warehouse.repository;

import com.warehouse.entity.TransferOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 调拨单明细数据访问接口
 * 
 * @author Warehouse Team
 */
@Repository
public interface TransferOrderDetailRepository extends JpaRepository<TransferOrderDetail, Long> {

    /**
     * 根据调拨单ID查找明细
     */
    List<TransferOrderDetail> findByTransferOrderId(Long transferOrderId);

    /**
     * 根据调拨单ID查找明细（按序号排序）
     */
    List<TransferOrderDetail> findByTransferOrderIdOrderByIdAsc(Long transferOrderId);

    /**
     * 根据货物ID查找明细
     */
    List<TransferOrderDetail> findByGoodsId(Long goodsId);



    /**
     * 根据调拨单ID删除明细
     */
    @Modifying
    @Query("DELETE FROM TransferOrderDetail d WHERE d.transferOrder.id = :transferOrderId")
    void deleteByTransferOrderId(@Param("transferOrderId") Long transferOrderId);

    /**
     * 统计调拨单明细数量
     */
    @Query("SELECT COUNT(d) FROM TransferOrderDetail d WHERE d.transferOrder.id = :transferOrderId")
    long countByTransferOrderId(@Param("transferOrderId") Long transferOrderId);

    /**
     * 根据调拨单ID计算总数量
     */
    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM TransferOrderDetail d WHERE d.transferOrder.id = :transferOrderId")
    java.math.BigDecimal sumQuantityByTransferOrderId(@Param("transferOrderId") Long transferOrderId);
}
