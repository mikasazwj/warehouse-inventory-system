package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.OutboundOrderDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import com.warehouse.service.OutboundOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 出库单控制器
 *
 * @author Warehouse Team
 */
@RestController
@RequestMapping("/api/outbound-orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OutboundOrderController {

    @Autowired
    private OutboundOrderService outboundOrderService;

    /**
     * 分页查询出库单
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<PageResponse<OutboundOrderDTO>> getOutboundOrders(
            @RequestParam(defaultValue = "") String orderNumber,
            @RequestParam(required = false) BusinessType businessType,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        System.out.println("=== 出库单查询请求 ===");
        System.out.println("orderNumber: " + orderNumber);
        System.out.println("warehouseId: " + warehouseId);
        System.out.println("businessType: " + businessType);
        System.out.println("status: " + status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        PageResponse<OutboundOrderDTO> orders = outboundOrderService.findByPageWithFilters(
                "", orderNumber, warehouseId, businessType, status, "", "", pageable);

        System.out.println("查询结果数量: " + orders.getTotalElements());
        return ApiResponse.success(orders);
    }

    /**
     * 生成出库单号
     */
    @GetMapping("/generate-order-number")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<String> generateOrderNumber() {
        String orderNumber = outboundOrderService.generateOrderNumber();
        return ApiResponse.success(orderNumber);
    }

    /**
     * 创建出库单
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<OutboundOrderDTO> createOutboundOrder(@Valid @RequestBody OutboundOrderDTO.CreateRequest request) {
        OutboundOrderDTO order = outboundOrderService.createOutboundOrder(request);
        return ApiResponse.success("出库单创建成功", order);
    }

    /**
     * 根据ID查找出库单
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<OutboundOrderDTO> getOutboundOrderById(@PathVariable Long id) {
        return outboundOrderService.findById(id)
                .map(order -> ApiResponse.success(order))
                .orElse(ApiResponse.notFound("出库单不存在"));
    }

    /**
     * 更新出库单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<OutboundOrderDTO> updateOutboundOrder(@PathVariable Long id, @Valid @RequestBody OutboundOrderDTO.UpdateRequest request) {
        OutboundOrderDTO order = outboundOrderService.updateOutboundOrder(id, request);
        return ApiResponse.success("出库单更新成功", order);
    }

    /**
     * 删除出库单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> deleteOutboundOrder(@PathVariable Long id) {
        outboundOrderService.deleteOutboundOrder(id);
        return ApiResponse.success("出库单删除成功");
    }

    /**
     * 提交出库单审核
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<OutboundOrderDTO> submitOutboundOrder(@PathVariable Long id) {
        OutboundOrderDTO order = outboundOrderService.submitOutboundOrder(id);
        return ApiResponse.success("出库单提交成功", order);
    }

    /**
     * 审批出库单
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER')")
    public ApiResponse<OutboundOrderDTO> approveOutboundOrder(@PathVariable Long id, @Valid @RequestBody OutboundOrderDTO.ApprovalRequest request) {
        OutboundOrderDTO order = outboundOrderService.approveOutboundOrder(id, request);
        return ApiResponse.success("出库单审批成功", order);
    }

    /**
     * 执行出库操作
     */
    @PutMapping("/{id}/execute")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER')")
    public ApiResponse<OutboundOrderDTO> executeOutboundOrder(@PathVariable Long id) {
        OutboundOrderDTO order = outboundOrderService.executeOutboundOrder(id);
        return ApiResponse.success("出库操作执行成功", order);
    }

    /**
     * 取消出库单
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<String> cancelOutboundOrder(@PathVariable Long id, @RequestBody CancelRequest request) {
        try {
            outboundOrderService.cancelOutboundOrder(id, request.getReason());
            return ApiResponse.success("出库单取消成功");
        } catch (Exception e) {
            return ApiResponse.error("取消出库单失败: " + e.getMessage());
        }
    }

    /**
     * 获取出库统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<OutboundOrderService.OutboundStatistics> getOutboundStatistics() {
        OutboundOrderService.OutboundStatistics statistics = outboundOrderService.getOutboundStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 查找待审批的出库单
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<OutboundOrderDTO>> getPendingOutboundOrders() {
        List<OutboundOrderDTO> orders = outboundOrderService.findPendingOrders();
        return ApiResponse.success(orders);
    }

    /**
     * 查找今日出库单
     */
    @GetMapping("/today")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<List<OutboundOrderDTO>> getTodayOutboundOrders() {
        List<OutboundOrderDTO> orders = outboundOrderService.findTodayOrders();
        return ApiResponse.success(orders);
    }

    /**
     * 检查单号是否存在
     */
    @GetMapping("/check-order-number")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<Boolean> checkOrderNumber(@RequestParam String orderNumber) {
        boolean exists = outboundOrderService.existsByOrderNumber(orderNumber);
        return ApiResponse.success(exists);
    }

    // 内部DTO类
    public static class CancelRequest {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
