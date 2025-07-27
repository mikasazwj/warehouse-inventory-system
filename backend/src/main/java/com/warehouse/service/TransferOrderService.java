package com.warehouse.service;

import com.warehouse.dto.TransferOrderDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.enums.ApprovalStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 调拨单服务接口
 * 
 * @author Warehouse Team
 */
public interface TransferOrderService {

    /**
     * 创建调拨单
     */
    TransferOrderDTO createTransferOrder(TransferOrderDTO.CreateRequest request);

    /**
     * 更新调拨单
     */
    TransferOrderDTO updateTransferOrder(Long id, TransferOrderDTO.UpdateRequest request);

    /**
     * 删除调拨单（软删除）
     */
    void deleteTransferOrder(Long id);

    /**
     * 根据ID查找调拨单
     */
    Optional<TransferOrderDTO> findById(Long id);

    /**
     * 根据单号查找调拨单
     */
    Optional<TransferOrderDTO> findByOrderNumber(String orderNumber);

    /**
     * 查找所有调拨单
     */
    List<TransferOrderDTO> findAll();

    /**
     * 根据源仓库查找调拨单
     */
    List<TransferOrderDTO> findBySourceWarehouse(Long sourceWarehouseId);

    /**
     * 根据目标仓库查找调拨单
     */
    List<TransferOrderDTO> findByTargetWarehouse(Long targetWarehouseId);

    /**
     * 根据状态查找调拨单
     */
    List<TransferOrderDTO> findByStatus(ApprovalStatus status);

    /**
     * 查找待审批的调拨单
     */
    List<TransferOrderDTO> findPendingOrders();

    /**
     * 根据源仓库查找待审批的调拨单
     */
    List<TransferOrderDTO> findPendingOrdersBySourceWarehouse(Long sourceWarehouseId);

    /**
     * 查找已审批但未执行的调拨单
     */
    List<TransferOrderDTO> findApprovedButNotExecuted();

    /**
     * 分页查询调拨单
     */
    PageResponse<TransferOrderDTO> findByPage(String keyword, Pageable pageable);

    /**
     * 多条件分页查询调拨单
     */
    PageResponse<TransferOrderDTO> findByPage(String orderNumber, ApprovalStatus status,
                                            Long sourceWarehouseId, Long targetWarehouseId, Pageable pageable);

    /**
     * 多条件分页查询调拨单（包含日期范围）
     */
    PageResponse<TransferOrderDTO> findByPage(String orderNumber, ApprovalStatus status,
                                            Long sourceWarehouseId, Long targetWarehouseId,
                                            LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 分页查询调拨单（按源仓库）
     */
    PageResponse<TransferOrderDTO> findBySourceWarehouseAndPage(Long sourceWarehouseId, String keyword, Pageable pageable);

    /**
     * 分页查询调拨单（按状态）
     */
    PageResponse<TransferOrderDTO> findByStatusAndPage(ApprovalStatus status, String keyword, Pageable pageable);

    /**
     * 根据日期范围查找调拨单
     */
    List<TransferOrderDTO> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 审批调拨单
     */
    TransferOrderDTO approveTransferOrder(Long id, TransferOrderDTO.ApprovalRequest request);

    /**
     * 执行调拨操作
     */
    TransferOrderDTO executeTransferOrder(Long id);

    /**
     * 取消调拨单
     */
    void cancelTransferOrder(Long id, String reason);

    /**
     * 检查单号是否存在
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * 生成调拨单号
     */
    String generateOrderNumber();

    /**
     * 查找今日调拨单
     */
    List<TransferOrderDTO> findTodayOrders();

    /**
     * 查找逾期未执行的调拨单
     */
    List<TransferOrderDTO> findOverdueOrders();

    /**
     * 获取调拨统计信息
     */
    TransferStatistics getTransferStatistics();

    /**
     * 根据仓库和日期范围统计调拨数量（作为源仓库）
     */
    long countTransferOutByWarehouseAndDateRange(Long warehouseId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据仓库和日期范围统计调拨数量（作为目标仓库）
     */
    long countTransferInByWarehouseAndDateRange(Long warehouseId, LocalDate startDate, LocalDate endDate);

    /**
     * 调拨统计信息
     */
    class TransferStatistics {
        private long totalOrders;
        private long pendingOrders;
        private long approvedOrders;
        private long executedOrders;
        private long cancelledOrders;
        private long overdueOrders;

        // Constructors
        public TransferStatistics() {}

        public TransferStatistics(long totalOrders, long pendingOrders, long approvedOrders, 
                                long executedOrders, long cancelledOrders, long overdueOrders) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.approvedOrders = approvedOrders;
            this.executedOrders = executedOrders;
            this.cancelledOrders = cancelledOrders;
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
        public long getOverdueOrders() { return overdueOrders; }
        public void setOverdueOrders(long overdueOrders) { this.overdueOrders = overdueOrders; }
    }
}
