package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.InboundOrderDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import com.warehouse.service.InboundOrderService;
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
 * 入库单控制器
 * 
 * @author Warehouse Team
 */
@RestController
@RequestMapping("/api/inbound-orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InboundOrderController {

    @Autowired
    private InboundOrderService inboundOrderService;

    /**
     * 分页查询入库单
     */
    @GetMapping
    public ApiResponse<PageResponse<InboundOrderDTO>> getInboundOrders(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) BusinessType businessType,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        System.out.println("=== 入库单查询请求 ===");
        System.out.println("keyword: " + keyword);
        System.out.println("warehouseId: " + warehouseId);
        System.out.println("businessType: " + businessType);
        System.out.println("status: " + status);
        System.out.println("startDate: " + startDate);
        System.out.println("endDate: " + endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        // 将LocalDate转换为String
        String startDateStr = startDate != null ? startDate.toString() : "";
        String endDateStr = endDate != null ? endDate.toString() : "";

        PageResponse<InboundOrderDTO> orders = inboundOrderService.findByPageWithFilters(
                keyword, warehouseId, businessType, status, startDateStr, endDateStr, pageable);
        
        System.out.println("查询结果数量: " + orders.getTotalElements());
        return ApiResponse.success(orders);
    }

    /**
     * 创建入库单
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<InboundOrderDTO> createInboundOrder(@Valid @RequestBody InboundOrderDTO.CreateRequest request) {
        try {
            InboundOrderDTO order = inboundOrderService.createInboundOrder(request);
            return ApiResponse.success("入库单创建成功", order);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("创建入库单失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查找入库单
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<InboundOrderDTO> getInboundOrderById(@PathVariable Long id) {
        return inboundOrderService.findById(id)
                .map(order -> ApiResponse.success(order))
                .orElse(ApiResponse.notFound("入库单不存在"));
    }

    /**
     * 更新入库单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<InboundOrderDTO> updateInboundOrder(@PathVariable Long id, @Valid @RequestBody InboundOrderDTO.UpdateRequest request) {
        try {
            InboundOrderDTO order = inboundOrderService.updateInboundOrder(id, request);
            return ApiResponse.success("入库单更新成功", order);
        } catch (Exception e) {
            return ApiResponse.error("更新入库单失败: " + e.getMessage());
        }
    }

    /**
     * 删除入库单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> deleteInboundOrder(@PathVariable Long id) {
        try {
            inboundOrderService.deleteInboundOrder(id);
            return ApiResponse.success("入库单删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除入库单失败: " + e.getMessage());
        }
    }

    /**
     * 审批入库单
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<InboundOrderDTO> approveInboundOrder(@PathVariable Long id, @Valid @RequestBody InboundOrderDTO.ApprovalRequest request) {
        try {
            InboundOrderDTO order = inboundOrderService.approveInboundOrder(id, request);
            return ApiResponse.success("入库单审批成功", order);
        } catch (Exception e) {
            return ApiResponse.error("审批入库单失败: " + e.getMessage());
        }
    }

    /**
     * 执行入库操作
     */
    @PutMapping("/{id}/execute")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<InboundOrderDTO> executeInboundOrder(@PathVariable Long id) {
        try {
            InboundOrderDTO order = inboundOrderService.executeInboundOrder(id);
            return ApiResponse.success("入库操作执行成功", order);
        } catch (Exception e) {
            return ApiResponse.error("执行入库操作失败: " + e.getMessage());
        }
    }

    /**
     * 取消入库单
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> cancelInboundOrder(@PathVariable Long id, @RequestBody CancelRequest request) {
        try {
            inboundOrderService.cancelInboundOrder(id, request.getReason());
            return ApiResponse.success("入库单取消成功");
        } catch (Exception e) {
            return ApiResponse.error("取消入库单失败: " + e.getMessage());
        }
    }

    /**
     * 获取入库单统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<InboundOrderService.InboundStatistics> getInboundStatistics() {
        try {
            InboundOrderService.InboundStatistics statistics = inboundOrderService.getInboundStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 查找今日入库单
     */
    @GetMapping("/today")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<List<InboundOrderDTO>> getTodayInboundOrders() {
        try {
            List<InboundOrderDTO> orders = inboundOrderService.findTodayOrders();
            return ApiResponse.success(orders);
        } catch (Exception e) {
            return ApiResponse.error("查询今日入库单失败: " + e.getMessage());
        }
    }

    /**
     * 生成入库单号
     */
    @GetMapping("/generate-order-number")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('TEAM_LEADER') or hasAuthority('SQUAD_LEADER') or hasAuthority('ROLE_USER')")
    public ApiResponse<String> generateOrderNumber() {
        try {
            String orderNumber = inboundOrderService.generateOrderNumber();
            return ApiResponse.success(orderNumber);
        } catch (Exception e) {
            return ApiResponse.error("生成单号失败: " + e.getMessage());
        }
    }

    /**
     * 检查单号是否存在
     */
    @GetMapping("/check-order-number")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Boolean> checkOrderNumber(@RequestParam String orderNumber) {
        try {
            boolean exists = inboundOrderService.existsByOrderNumber(orderNumber);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            return ApiResponse.error("检查单号失败: " + e.getMessage());
        }
    }

    // 内部DTO类
    public static class CancelRequest {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
