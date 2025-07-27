package com.warehouse.service;

import com.warehouse.dto.InboundOrderDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 入库单服务接口
 * 
 * @author Warehouse Team
 */
public interface InboundOrderService {

    /**
     * 创建入库单
     */
    InboundOrderDTO createInboundOrder(InboundOrderDTO.CreateRequest request);

    /**
     * 创建入库单（跳过库存检查，用于系统自动生成）
     */
    InboundOrderDTO createInboundOrderWithoutStockCheck(InboundOrderDTO.CreateRequest request);

    /**
     * 更新入库单
     */
    InboundOrderDTO updateInboundOrder(Long id, InboundOrderDTO.UpdateRequest request);

    /**
     * 删除入库单（软删除）
     */
    void deleteInboundOrder(Long id);

    /**
     * 根据ID查找入库单
     */
    Optional<InboundOrderDTO> findById(Long id);

    /**
     * 根据单号查找入库单
     */
    Optional<InboundOrderDTO> findByOrderNumber(String orderNumber);

    /**
     * 查找所有入库单
     */
    List<InboundOrderDTO> findAll();

    /**
     * 根据仓库查找入库单
     */
    List<InboundOrderDTO> findByWarehouse(Long warehouseId);



    /**
     * 根据状态查找入库单
     */
    List<InboundOrderDTO> findByStatus(ApprovalStatus status);

    /**
     * 根据业务类型查找入库单
     */
    List<InboundOrderDTO> findByBusinessType(BusinessType businessType);

    /**
     * 查找待审批的入库单
     */
    List<InboundOrderDTO> findPendingOrders();

    /**
     * 根据仓库查找待审批的入库单
     */
    List<InboundOrderDTO> findPendingOrdersByWarehouse(Long warehouseId);

    /**
     * 查找已审批但未执行的入库单
     */
    List<InboundOrderDTO> findApprovedButNotExecuted();

    /**
     * 分页查询入库单
     */
    PageResponse<InboundOrderDTO> findByPage(String keyword, Pageable pageable);

    /**
     * 分页查询入库单（带筛选条件）
     */
    PageResponse<InboundOrderDTO> findByPageWithFilters(String keyword, Long warehouseId,
            BusinessType businessType, ApprovalStatus status, String startDate, String endDate, Pageable pageable);

    /**
     * 分页查询入库单（按仓库）
     */
    PageResponse<InboundOrderDTO> findByWarehouseAndPage(Long warehouseId, String keyword, Pageable pageable);

    /**
     * 分页查询入库单（按状态）
     */
    PageResponse<InboundOrderDTO> findByStatusAndPage(ApprovalStatus status, String keyword, Pageable pageable);

    /**
     * 根据日期范围查找入库单
     */
    List<InboundOrderDTO> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 审批入库单
     */
    InboundOrderDTO approveInboundOrder(Long id, InboundOrderDTO.ApprovalRequest request);

    /**
     * 执行入库操作
     */
    InboundOrderDTO executeInboundOrder(Long id);

    /**
     * 取消入库单
     */
    void cancelInboundOrder(Long id, String reason);

    /**
     * 检查单号是否存在
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * 生成入库单号
     */
    String generateOrderNumber();



    /**
     * 查找今日入库单
     */
    List<InboundOrderDTO> findTodayOrders();

    /**
     * 查找逾期未执行的入库单
     */
    List<InboundOrderDTO> findOverdueOrders();

    /**
     * 获取入库统计信息
     */
    InboundStatistics getInboundStatistics();



    /**
     * 根据仓库和日期范围统计入库金额
     */
    java.math.BigDecimal sumAmountByWarehouseAndDateRange(Long warehouseId, LocalDate startDate, LocalDate endDate);

    /**
     * 入库统计信息
     */
    class InboundStatistics {
        private long totalOrders;
        private long pendingOrders;
        private long approvedOrders;
        private long executedOrders;
        private long cancelledOrders;
        private java.math.BigDecimal totalAmount;
        private java.math.BigDecimal executedAmount;
        private long overdueOrders;

        // Constructors
        public InboundStatistics() {}

        public InboundStatistics(long totalOrders, long pendingOrders, long approvedOrders, long executedOrders,
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
