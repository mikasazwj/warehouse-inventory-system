package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.PageResponse;
import com.warehouse.dto.StocktakeOrderDTO;
import com.warehouse.service.StocktakeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/stocktake-orders")
@CrossOrigin(origins = "*")
public class StocktakeOrderController {

    @Autowired
    private StocktakeOrderService stocktakeOrderService;

    /**
     * 开始盘点
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StocktakeOrderDTO> startStocktakeOrder(@PathVariable Long id) {
        try {
            StocktakeOrderDTO stocktakeOrder = stocktakeOrderService.startStocktake(id);
            return ApiResponse.success("盘点开始成功", stocktakeOrder);
        } catch (Exception e) {
            return ApiResponse.error("开始盘点失败: " + e.getMessage());
        }
    }

    /**
     * 完成盘点
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StocktakeOrderDTO> completeStocktakeOrder(@PathVariable Long id) {
        try {
            StocktakeOrderDTO stocktakeOrder = stocktakeOrderService.completeStocktake(id);
            return ApiResponse.success("盘点完成成功", stocktakeOrder);
        } catch (Exception e) {
            return ApiResponse.error("完成盘点失败: " + e.getMessage());
        }
    }

    /**
     * 取消盘点单
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> cancelStocktakeOrder(@PathVariable Long id, @RequestParam String reason) {
        try {
            stocktakeOrderService.cancelStocktakeOrder(id, reason);
            return ApiResponse.success("盘点单取消成功");
        } catch (Exception e) {
            return ApiResponse.error("取消盘点单失败: " + e.getMessage());
        }
    }

    /**
     * 审批盘点单
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StocktakeOrderDTO> approveStocktakeOrder(@PathVariable Long id,
                                                               @Valid @RequestBody StocktakeOrderDTO.ApprovalRequest request) {
        try {
            StocktakeOrderDTO stocktakeOrder = stocktakeOrderService.approveStocktakeOrder(id, request);
            return ApiResponse.success("盘点单审批成功", stocktakeOrder);
        } catch (Exception e) {
            return ApiResponse.error("审批失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询盘点单
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<StocktakeOrderDTO>> getAllStocktakeOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            PageResponse<StocktakeOrderDTO> pageResponse = stocktakeOrderService.findByPage(keyword, pageable);
            return ApiResponse.success(pageResponse);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查找盘点单
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StocktakeOrderDTO> getStocktakeOrderById(@PathVariable Long id) {
        try {
            return stocktakeOrderService.findById(id)
                    .map(order -> ApiResponse.success(order))
                    .orElse(ApiResponse.notFound("盘点单不存在"));
        } catch (Exception e) {
            return ApiResponse.error("获取盘点单详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成盘点单号
     */
    @GetMapping("/generate-order-number")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> generateOrderNumber() {
        try {
            String orderNumber = stocktakeOrderService.generateOrderNumber();
            return ApiResponse.success(orderNumber);
        } catch (Exception e) {
            return ApiResponse.error("生成盘点单号失败: " + e.getMessage());
        }
    }

    /**
     * 创建盘点单
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StocktakeOrderDTO> createStocktakeOrder(@Valid @RequestBody StocktakeOrderDTO.CreateRequest request) {
        try {
            StocktakeOrderDTO stocktakeOrder = stocktakeOrderService.createStocktakeOrder(request);
            return ApiResponse.success("盘点单创建成功", stocktakeOrder);
        } catch (Exception e) {
            return ApiResponse.error("创建盘点单失败: " + e.getMessage());
        }
    }

    /**
     * 更新盘点单
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StocktakeOrderDTO> updateStocktakeOrder(@PathVariable Long id,
                                                              @Valid @RequestBody StocktakeOrderDTO.UpdateRequest request) {
        try {
            StocktakeOrderDTO stocktakeOrder = stocktakeOrderService.updateStocktakeOrder(id, request);
            return ApiResponse.success("盘点单更新成功", stocktakeOrder);
        } catch (Exception e) {
            return ApiResponse.error("更新盘点单失败: " + e.getMessage());
        }
    }

    /**
     * 删除盘点单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> deleteStocktakeOrder(@PathVariable Long id) {
        try {
            stocktakeOrderService.deleteStocktakeOrder(id);
            return ApiResponse.success("盘点单删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除盘点单失败: " + e.getMessage());
        }
    }

    /**
     * 获取盘点明细列表
     */
    @GetMapping("/{id}/details")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<StocktakeOrderDTO.StocktakeOrderDetailDTO>> getStocktakeOrderDetails(@PathVariable Long id) {
        try {
            List<StocktakeOrderDTO.StocktakeOrderDetailDTO> details = stocktakeOrderService.getStocktakeOrderDetails(id);
            return ApiResponse.success(details);
        } catch (Exception e) {
            return ApiResponse.error("获取盘点明细失败: " + e.getMessage());
        }
    }

    /**
     * 更新盘点明细
     */
    @PutMapping("/{id}/details/{detailId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StocktakeOrderDTO.StocktakeOrderDetailDTO> updateStocktakeOrderDetail(
            @PathVariable Long id,
            @PathVariable Long detailId,
            @Valid @RequestBody StocktakeOrderDTO.UpdateDetailRequest request) {
        try {
            StocktakeOrderDTO.StocktakeOrderDetailDTO detail = stocktakeOrderService.updateStocktakeOrderDetail(id, detailId, request);
            return ApiResponse.success("盘点明细更新成功", detail);
        } catch (Exception e) {
            return ApiResponse.error("更新盘点明细失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新盘点明细
     */
    @PutMapping("/{id}/details/batch")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<StocktakeOrderDTO.StocktakeOrderDetailDTO>> batchUpdateStocktakeOrderDetails(
            @PathVariable Long id,
            @Valid @RequestBody List<StocktakeOrderDTO.UpdateDetailRequest> requests) {
        try {
            List<StocktakeOrderDTO.StocktakeOrderDetailDTO> details = stocktakeOrderService.batchUpdateStocktakeOrderDetails(id, requests);
            return ApiResponse.success("批量更新盘点明细成功", details);
        } catch (Exception e) {
            return ApiResponse.error("批量更新盘点明细失败: " + e.getMessage());
        }
    }

    /**
     * 生成盘点报告
     */
    @GetMapping("/{id}/report")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StocktakeOrderDTO.StocktakeReport> generateStocktakeReport(@PathVariable Long id) {
        try {
            StocktakeOrderDTO.StocktakeReport report = stocktakeOrderService.generateStocktakeReport(id);
            return ApiResponse.success(report);
        } catch (Exception e) {
            return ApiResponse.error("生成盘点报告失败: " + e.getMessage());
        }
    }

    /**
     * 获取盘点统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StocktakeOrderService.StocktakeStatistics> getStocktakeStatistics() {
        try {
            StocktakeOrderService.StocktakeStatistics statistics = stocktakeOrderService.getStocktakeStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取盘点统计失败: " + e.getMessage());
        }
    }
}