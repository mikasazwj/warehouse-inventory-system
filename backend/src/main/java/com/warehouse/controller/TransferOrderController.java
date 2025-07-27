package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.TransferOrderDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.service.TransferOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 调拨单控制器
 * 
 * @author Warehouse Team
 */
@RestController
@RequestMapping("/api/transfer-orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransferOrderController {

    @Autowired
    private TransferOrderService transferOrderService;

    /**
     * 分页查询调拨单
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<PageResponse<TransferOrderDTO>> getTransferOrders(
            @RequestParam(defaultValue = "") String orderNumber,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) Long sourceWarehouseId,
            @RequestParam(required = false) Long targetWarehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdTime") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        PageResponse<TransferOrderDTO> orders = transferOrderService.findByPage(
                orderNumber, status, sourceWarehouseId, targetWarehouseId, startDate, endDate, pageable);
        return ApiResponse.success(orders);
    }

    /**
     * 生成调拨单号
     */
    @GetMapping("/generate-order-number")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<String> generateOrderNumber() {
        String orderNumber = transferOrderService.generateOrderNumber();
        return ApiResponse.success(orderNumber);
    }

    /**
     * 创建调拨单
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<TransferOrderDTO> createTransferOrder(@Valid @RequestBody TransferOrderDTO.CreateRequest request) {
        TransferOrderDTO order = transferOrderService.createTransferOrder(request);
        return ApiResponse.success("调拨单创建成功", order);
    }

    /**
     * 根据ID查找调拨单
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<TransferOrderDTO> getTransferOrderById(@PathVariable Long id) {
        return transferOrderService.findById(id)
                .map(order -> ApiResponse.success(order))
                .orElse(ApiResponse.notFound("调拨单不存在"));
    }

    /**
     * 更新调拨单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<TransferOrderDTO> updateTransferOrder(@PathVariable Long id, @Valid @RequestBody TransferOrderDTO.UpdateRequest request) {
        TransferOrderDTO order = transferOrderService.updateTransferOrder(id, request);
        return ApiResponse.success("调拨单更新成功", order);
    }

    /**
     * 删除调拨单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> deleteTransferOrder(@PathVariable Long id) {
        transferOrderService.deleteTransferOrder(id);
        return ApiResponse.success("调拨单删除成功");
    }

    /**
     * 审批调拨单
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<TransferOrderDTO> approveTransferOrder(@PathVariable Long id, @Valid @RequestBody TransferOrderDTO.ApprovalRequest request) {
        TransferOrderDTO order = transferOrderService.approveTransferOrder(id, request);
        return ApiResponse.success("调拨单审批成功", order);
    }

    /**
     * 执行调拨操作
     */
    @PutMapping("/{id}/execute")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<TransferOrderDTO> executeTransferOrder(@PathVariable Long id) {
        TransferOrderDTO order = transferOrderService.executeTransferOrder(id);
        return ApiResponse.success("调拨操作执行成功", order);
    }

    /**
     * 取消调拨单
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> cancelTransferOrder(@PathVariable Long id, @RequestBody CancelRequest request) {
        transferOrderService.cancelTransferOrder(id, request.getReason());
        return ApiResponse.success("调拨单取消成功");
    }

    /**
     * 获取调拨统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<TransferOrderService.TransferStatistics> getTransferStatistics() {
        TransferOrderService.TransferStatistics statistics = transferOrderService.getTransferStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 查找待审批的调拨单
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<TransferOrderDTO>> getPendingTransferOrders() {
        List<TransferOrderDTO> orders = transferOrderService.findPendingOrders();
        return ApiResponse.success(orders);
    }

    /**
     * 查找今日调拨单
     */
    @GetMapping("/today")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<List<TransferOrderDTO>> getTodayTransferOrders() {
        List<TransferOrderDTO> orders = transferOrderService.findTodayOrders();
        return ApiResponse.success(orders);
    }

    /**
     * 检查单号是否存在
     */
    @GetMapping("/check-order-number")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<Boolean> checkOrderNumber(@RequestParam String orderNumber) {
        boolean exists = transferOrderService.existsByOrderNumber(orderNumber);
        return ApiResponse.success(exists);
    }

    // 内部DTO类
    public static class CancelRequest {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
