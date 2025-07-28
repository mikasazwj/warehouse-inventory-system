package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.InventoryDTO;
import com.warehouse.dto.InventoryHistoryDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.exception.BusinessException;
import com.warehouse.service.InventoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 库存控制器
 * 
 * @author Warehouse Team
 */
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("isAuthenticated()") // 临时：所有方法只需要认证
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryService inventoryService;

    /**
     * 根据ID查找库存
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<InventoryDTO> getInventoryById(@PathVariable Long id) {
        return inventoryService.findById(id)
                .map(inventory -> ApiResponse.success(inventory))
                .orElse(ApiResponse.notFound("库存记录不存在"));
    }

    /**
     * 根据仓库和货物查找库存
     */
    @GetMapping("/warehouse/{warehouseId}/goods/{goodsId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<InventoryDTO> getInventoryByWarehouseAndGoods(
            @PathVariable Long warehouseId, @PathVariable Long goodsId) {
        return inventoryService.findByWarehouseAndGoods(warehouseId, goodsId)
                .map(inventory -> ApiResponse.success(inventory))
                .orElse(ApiResponse.notFound("库存记录不存在"));
    }

    /**
     * 分页查询库存
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<InventoryDTO>> getInventory(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedTime") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<InventoryDTO> inventory = inventoryService.findByPageWithFilters(
                keyword, warehouseId, categoryId, stockStatus, pageable);
        return ApiResponse.success(inventory);
    }

    /**
     * 根据仓库分页查询库存
     */
    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<InventoryDTO>> getInventoryByWarehouse(
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "goodsCode"));
        PageResponse<InventoryDTO> inventory = inventoryService.findByWarehouseAndPage(warehouseId, keyword, pageable);
        return ApiResponse.success(inventory);
    }

    /**
     * 根据货物分页查询库存
     */
    @GetMapping("/goods/{goodsId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<InventoryDTO>> getInventoryByGoods(
            @PathVariable Long goodsId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "warehouseName"));
        PageResponse<InventoryDTO> inventory = inventoryService.findByGoodsAndPage(goodsId, keyword, pageable);
        return ApiResponse.success(inventory);
    }

    /**
     * 查找有库存的记录
     */
    @GetMapping("/with-stock")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<InventoryDTO>> getInventoryWithStock() {
        List<InventoryDTO> inventory = inventoryService.findWithStock();
        return ApiResponse.success(inventory);
    }

    /**
     * 查找低库存记录
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<InventoryDTO>> getLowStockInventory() {
        List<InventoryDTO> inventory = inventoryService.findLowStock();
        return ApiResponse.success(inventory);
    }

    /**
     * 查找即将过期的库存
     */
    @GetMapping("/near-expiry")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<InventoryDTO>> getNearExpiryInventory(
            @RequestParam(defaultValue = "30") int days) {
        List<InventoryDTO> inventory = inventoryService.findNearExpiry(days);
        return ApiResponse.success(inventory);
    }

    /**
     * 查找已过期的库存
     */
    @GetMapping("/expired")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<InventoryDTO>> getExpiredInventory() {
        List<InventoryDTO> inventory = inventoryService.findExpired();
        return ApiResponse.success(inventory);
    }

    /**
     * 查找有锁定库存的记录
     */
    @GetMapping("/with-locked")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<InventoryDTO>> getInventoryWithLocked() {
        List<InventoryDTO> inventory = inventoryService.findWithLockedStock();
        return ApiResponse.success(inventory);
    }

    /**
     * 库存调整
     */
    @PostMapping("/adjust")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<InventoryDTO> adjustInventory(@Valid @RequestBody InventoryDTO.AdjustRequest request) {
        InventoryDTO inventory = inventoryService.adjustInventory(request);
        return ApiResponse.success("库存调整成功", inventory);
    }

    /**
     * 库存锁定
     */
    @PostMapping("/lock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> lockInventory(@Valid @RequestBody InventoryDTO.LockRequest request) {
        inventoryService.lockInventory(request);
        return ApiResponse.success("库存锁定成功");
    }

    /**
     * 库存解锁
     */
    @PostMapping("/unlock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> unlockInventory(@Valid @RequestBody InventoryDTO.UnlockRequest request) {
        inventoryService.unlockInventory(request);
        return ApiResponse.success("库存解锁成功");
    }

    /**
     * 检查库存是否充足
     */
    @GetMapping("/check-available")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> checkStockAvailable(
            @RequestParam Long warehouseId,
            @RequestParam Long goodsId,
            @RequestParam BigDecimal requiredQuantity) {
        boolean available = inventoryService.checkStockAvailable(warehouseId, goodsId, requiredQuantity);
        return ApiResponse.success(available);
    }

    /**
     * 获取可用库存数量
     */
    @GetMapping("/available-quantity")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BigDecimal> getAvailableQuantity(
            @RequestParam Long warehouseId,
            @RequestParam Long goodsId) {
        BigDecimal quantity = inventoryService.getAvailableQuantity(warehouseId, goodsId);
        return ApiResponse.success(quantity);
    }

    /**
     * 获取总库存数量
     */
    @GetMapping("/total-quantity")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BigDecimal> getTotalQuantity(
            @RequestParam Long warehouseId,
            @RequestParam Long goodsId) {
        BigDecimal quantity = inventoryService.getTotalQuantity(warehouseId, goodsId);
        return ApiResponse.success(quantity);
    }

    /**
     * 获取锁定库存数量
     */
    @GetMapping("/locked-quantity")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BigDecimal> getLockedQuantity(
            @RequestParam Long warehouseId,
            @RequestParam Long goodsId) {
        BigDecimal quantity = inventoryService.getLockedQuantity(warehouseId, goodsId);
        return ApiResponse.success(quantity);
    }



    /**
     * 根据生产日期范围查找库存
     */
    @GetMapping("/production-date-range")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<InventoryDTO>> getInventoryByProductionDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<InventoryDTO> inventory = inventoryService.findByProductionDateRange(startDate, endDate);
        return ApiResponse.success(inventory);
    }

    /**
     * 根据过期日期范围查找库存
     */
    @GetMapping("/expiry-date-range")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<InventoryDTO>> getInventoryByExpiryDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<InventoryDTO> inventory = inventoryService.findByExpiryDateRange(startDate, endDate);
        return ApiResponse.success(inventory);
    }

    /**
     * 获取库存统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<InventoryService.InventoryStatistics> getInventoryStatistics() {
        InventoryService.InventoryStatistics statistics = inventoryService.getInventoryStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 获取仓库库存统计
     */
    @GetMapping("/statistics/warehouse/{warehouseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<InventoryService.InventoryStatistics> getWarehouseInventoryStatistics(@PathVariable Long warehouseId) {
        InventoryService.InventoryStatistics statistics = inventoryService.getWarehouseInventoryStatistics(warehouseId);
        return ApiResponse.success(statistics);
    }

    /**
     * 获取货物库存统计
     */
    @GetMapping("/statistics/goods/{goodsId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<InventoryService.InventoryStatistics> getGoodsInventoryStatistics(@PathVariable Long goodsId) {
        InventoryService.InventoryStatistics statistics = inventoryService.getGoodsInventoryStatistics(goodsId);
        return ApiResponse.success(statistics);
    }

    /**
     * 导出库存数据
     */
    @GetMapping("/export")
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ResponseEntity<byte[]> exportInventory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String stockStatus) {
        try {
            byte[] excelData = inventoryService.exportInventoryToExcel(keyword, warehouseId, categoryId, stockStatus);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                "inventory_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");

            // 添加CORS头
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "*");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            logger.error("导出库存数据失败", e);
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取有库存记录的货物列表
     */
    @GetMapping("/goods-with-stock")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<InventoryDTO.GoodsInfo>> getGoodsWithStock(
            @RequestParam(required = false) Long warehouseId) {
        List<InventoryDTO.GoodsInfo> goodsList = inventoryService.getGoodsWithStock(warehouseId);
        return ApiResponse.success(goodsList);
    }

    /**
     * 获取货物的库存信息和加权平均价
     */
    @GetMapping("/goods/{goodsId}/stock-info")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<InventoryDTO.StockInfo> getGoodsStockInfo(
            @PathVariable Long goodsId,
            @RequestParam(required = false) Long warehouseId) {
        InventoryDTO.StockInfo stockInfo = inventoryService.getGoodsStockInfo(goodsId, warehouseId);
        return ApiResponse.success(stockInfo);
    }

    /**
     * 获取指定仓库的库存货物列表（用于盘点选择）
     */
    @GetMapping("/warehouse/{warehouseId}/goods-list")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<InventoryDTO.GoodsInventoryInfo>> getWarehouseGoodsList(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean onlyWithStock) {
        List<InventoryDTO.GoodsInventoryInfo> goodsList = inventoryService.getWarehouseGoodsList(warehouseId, categoryId, onlyWithStock);
        return ApiResponse.success(goodsList);
    }

    /**
     * 导出库存数据 (POST方法，避免CORS问题)
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ResponseEntity<byte[]> exportInventoryPost(@RequestBody InventoryDTO.ExportRequest request) {
        try {
            byte[] excelData = inventoryService.exportInventoryToExcel(
                request.getKeyword(),
                request.getWarehouseId(),
                request.getCategoryId(),
                request.getStockStatus()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                "inventory_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            logger.error("导出库存数据失败", e);
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 库存预警检查
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<InventoryDTO>> checkInventoryAlerts() {
        List<InventoryDTO> alerts = inventoryService.checkInventoryAlerts();
        return ApiResponse.success(alerts);
    }

    /**
     * 获取库存历史记录
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<InventoryHistoryDTO>> getInventoryHistory(@PathVariable Long id) {
        List<InventoryHistoryDTO> history = inventoryService.getInventoryHistory(id);
        return ApiResponse.success(history);
    }
}
