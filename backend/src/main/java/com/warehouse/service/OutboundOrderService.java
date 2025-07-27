package com.warehouse.service;

import com.warehouse.dto.OutboundOrderDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 出库单服务接口
 * 
 * @author Warehouse Team
 */
public interface OutboundOrderService {

    /**
     * 创建出库单
     */
    OutboundOrderDTO createOutboundOrder(OutboundOrderDTO.CreateRequest request);

    /**
     * 创建出库单（跳过库存检查，用于系统自动生成）
     */
    OutboundOrderDTO createOutboundOrderWithoutStockCheck(OutboundOrderDTO.CreateRequest request);

    /**
     * 更新出库单
     */
    OutboundOrderDTO updateOutboundOrder(Long id, OutboundOrderDTO.UpdateRequest request);

    /**
     * 删除出库单（软删除）
     */
    void deleteOutboundOrder(Long id);

    /**
     * 根据ID查找出库单
     */
    Optional<OutboundOrderDTO> findById(Long id);

    /**
     * 根据单号查找出库单
     */
    Optional<OutboundOrderDTO> findByOrderNumber(String orderNumber);

    /**
     * 查找所有出库单
     */
    List<OutboundOrderDTO> findAll();

    /**
     * 根据仓库查找出库单
     */
    List<OutboundOrderDTO> findByWarehouse(Long warehouseId);

    /**
     * 根据客户查找出库单
     */
    List<OutboundOrderDTO> findByCustomer(Long customerId);

    /**
     * 根据状态查找出库单
     */
    List<OutboundOrderDTO> findByStatus(ApprovalStatus status);

    /**
     * 根据业务类型查找出库单
     */
    List<OutboundOrderDTO> findByBusinessType(BusinessType businessType);

    /**
     * 查找待审批的出库单
     */
    List<OutboundOrderDTO> findPendingOrders();

    /**
     * 根据仓库查找待审批的出库单
     */
    List<OutboundOrderDTO> findPendingOrdersByWarehouse(Long warehouseId);

    /**
     * 查找已审批但未执行的出库单
     */
    List<OutboundOrderDTO> findApprovedButNotExecuted();

    /**
     * 分页查询出库单
     */
    PageResponse<OutboundOrderDTO> findByPage(String keyword, Pageable pageable);

    /**
     * 分页查询出库单（带筛选条件）
     */
    PageResponse<OutboundOrderDTO> findByPageWithFilters(String keyword, String orderNumber, Long warehouseId,
            BusinessType businessType, ApprovalStatus status, String startDate, String endDate, Pageable pageable);

    /**
     * 分页查询出库单（按仓库）
     */
    PageResponse<OutboundOrderDTO> findByWarehouseAndPage(Long warehouseId, String keyword, Pageable pageable);

    /**
     * 分页查询出库单（按状态）
     */
    PageResponse<OutboundOrderDTO> findByStatusAndPage(ApprovalStatus status, String keyword, Pageable pageable);

    /**
     * 根据日期范围查找出库单
     */
    List<OutboundOrderDTO> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 提交出库单审核
     */
    OutboundOrderDTO submitOutboundOrder(Long id);

    /**
     * 审批出库单
     */
    OutboundOrderDTO approveOutboundOrder(Long id, OutboundOrderDTO.ApprovalRequest request);

    /**
     * 执行出库操作
     */
    OutboundOrderDTO executeOutboundOrder(Long id);

    /**
     * 取消出库单
     */
    void cancelOutboundOrder(Long id, String reason);

    /**
     * 检查单号是否存在
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * 生成出库单号
     */
    String generateOrderNumber();

    /**
     * 查找今日出库单
     */
    List<OutboundOrderDTO> findTodayOrders();

    /**
     * 查找逾期未执行的出库单
     */
    List<OutboundOrderDTO> findOverdueOrders();

    /**
     * 获取出库统计信息
     */
    OutboundStatistics getOutboundStatistics();

    /**
     * 根据客户和日期范围统计出库金额
     */
    java.math.BigDecimal sumAmountByCustomerAndDateRange(Long customerId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据仓库和日期范围统计出库金额
     */
    java.math.BigDecimal sumAmountByWarehouseAndDateRange(Long warehouseId, LocalDate startDate, LocalDate endDate);

    /**
     * 出库统计信息
     */
    class OutboundStatistics {
        private long totalOrders;
        private long pendingOrders;
        private long approvedOrders;
        private long executedOrders;
        private long cancelledOrders;
        private java.math.BigDecimal totalAmount;
        private java.math.BigDecimal executedAmount;
        private long overdueOrders;

        // Constructors
        public OutboundStatistics() {}

        public OutboundStatistics(long totalOrders, long pendingOrders, long approvedOrders, long executedOrders,
                                long cancelledOrders, java.math.BigDecimal totalAmount, java.math.BigDecimal executedAmount, long overdueOrders) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.approvedOrders = approvedOrders;
            this.executedOrders = executedOrders;
            this.cancelledOrders = cancelledOrders;
            this.totalAmount = totalAmount;
            this.executedAmount = executedAmount;
            this.overdueOrders = overdueOrders;
        }

        // Getters and Setters
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        public long getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
        public long getApprovedOrders() { return approvedOrders; }
        public void setApprovedOrders(long approvedOrders) { this.approvedOrders = approvedOrders; }
        public long getExecutedOrders() { return executedOrders; }
        public void setExecutedOrders(long executedOrders) { this.executedOrders = executedOrders; }
        public long getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }
        public java.math.BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public java.math.BigDecimal getExecutedAmount() { return executedAmount; }
        public void setExecutedAmount(java.math.BigDecimal executedAmount) { this.executedAmount = executedAmount; }
        public long getOverdueOrders() { return overdueOrders; }
        public void setOverdueOrders(long overdueOrders) { this.overdueOrders = overdueOrders; }
    }
}
