package com.warehouse.service;

import com.warehouse.dto.PageResponse;
import com.warehouse.dto.StocktakeOrderDTO;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.StocktakeType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 盘点单服务接口
 * 
 * @author Warehouse Team
 */
public interface StocktakeOrderService {

    /**
     * 创建盘点单
     */
    StocktakeOrderDTO createStocktakeOrder(StocktakeOrderDTO.CreateRequest request);

    /**
     * 更新盘点单
     */
    StocktakeOrderDTO updateStocktakeOrder(Long id, StocktakeOrderDTO.UpdateRequest request);

    /**
     * 删除盘点单（软删除）
     */
    void deleteStocktakeOrder(Long id);

    /**
     * 根据ID查找盘点单
     */
    Optional<StocktakeOrderDTO> findById(Long id);

    /**
     * 根据单号查找盘点单
     */
    Optional<StocktakeOrderDTO> findByOrderNumber(String orderNumber);

    /**
     * 查找所有盘点单
     */
    List<StocktakeOrderDTO> findAll();

    /**
     * 根据仓库查找盘点单
     */
    List<StocktakeOrderDTO> findByWarehouse(Long warehouseId);

    /**
     * 根据状态查找盘点单
     */
    List<StocktakeOrderDTO> findByStatus(ApprovalStatus status);

    /**
     * 根据盘点类型查找盘点单
     */
    List<StocktakeOrderDTO> findByStocktakeType(StocktakeType stocktakeType);

    /**
     * 分页查询盘点单
     */
    PageResponse<StocktakeOrderDTO> findByPage(String keyword, Pageable pageable);

    /**
     * 根据日期范围查找盘点单
     */
    List<StocktakeOrderDTO> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 审批盘点单
     */
    StocktakeOrderDTO approveStocktakeOrder(Long id, StocktakeOrderDTO.ApprovalRequest request);

    /**
     * 开始盘点
     */
    StocktakeOrderDTO startStocktake(Long id);

    /**
     * 完成盘点
     */
    StocktakeOrderDTO completeStocktake(Long id);

    /**
     * 取消盘点单
     */
    void cancelStocktakeOrder(Long id, String reason);

    /**
     * 检查单号是否存在
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * 生成盘点单号
     */
    String generateOrderNumber();

    /**
     * 查找今日盘点单
     */
    List<StocktakeOrderDTO> findTodayOrders();

    /**
     * 查找逾期未执行的盘点单
     */
    List<StocktakeOrderDTO> findOverdueOrders();

    /**
     * 获取盘点统计信息
     */
    StocktakeStatistics getStocktakeStatistics();

    /**
     * 根据仓库和日期范围统计盘点次数
     */
    long countStocktakeByWarehouseAndDateRange(Long warehouseId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取盘点单明细列表
     */
    List<StocktakeOrderDTO.StocktakeOrderDetailDTO> getStocktakeOrderDetails(Long stocktakeOrderId);

    /**
     * 更新盘点明细
     */
    StocktakeOrderDTO.StocktakeOrderDetailDTO updateStocktakeOrderDetail(Long stocktakeOrderId, Long detailId, StocktakeOrderDTO.UpdateDetailRequest request);

    /**
     * 批量更新盘点明细
     */
    List<StocktakeOrderDTO.StocktakeOrderDetailDTO> batchUpdateStocktakeOrderDetails(Long stocktakeOrderId, List<StocktakeOrderDTO.UpdateDetailRequest> requests);

    /**
     * 生成盘点报告
     */
    StocktakeOrderDTO.StocktakeReport generateStocktakeReport(Long stocktakeOrderId);

    /**
     * 获取盘点差异明细
     */
    List<StocktakeOrderDTO.StocktakeOrderDetailDTO> getStocktakeDifferences(Long stocktakeOrderId);

    /**
     * 盘点统计信息
     */
    class StocktakeStatistics {
        private long totalOrders;
        private long pendingOrders;
        private long approvedOrders;
        private long completedOrders;
        private long cancelledOrders;
        private long overdueOrders;

        // Getters and Setters
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        public long getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
        public long getApprovedOrders() { return approvedOrders; }
        public void setApprovedOrders(long approvedOrders) { this.approvedOrders = approvedOrders; }
        public long getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }
        public long getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }
        public long getOverdueOrders() { return overdueOrders; }
        public void setOverdueOrders(long overdueOrders) { this.overdueOrders = overdueOrders; }
    }
}
