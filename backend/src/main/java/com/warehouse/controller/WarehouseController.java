package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.PageResponse;
import com.warehouse.dto.WarehouseDTO;
import com.warehouse.exception.BusinessException;
import com.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 仓库控制器
 * 
 * @author Warehouse Team
 */
@RestController
@RequestMapping("/api/warehouses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    /**
     * 创建仓库
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<WarehouseDTO> createWarehouse(@Valid @RequestBody WarehouseDTO.CreateRequest request) {
        WarehouseDTO warehouse = warehouseService.createWarehouse(request);
        return ApiResponse.success("仓库创建成功", warehouse);
    }

    /**
     * 更新仓库
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or (hasAuthority('WAREHOUSE_ADMIN') and @warehouseService.isWarehouseManagedByUser(#id, authentication.principal.id))")
    public ApiResponse<WarehouseDTO> updateWarehouse(@PathVariable Long id, @Valid @RequestBody WarehouseDTO.UpdateRequest request) {
        WarehouseDTO warehouse = warehouseService.updateWarehouse(id, request);
        return ApiResponse.success("仓库更新成功", warehouse);
    }

    /**
     * 删除仓库
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ApiResponse.success("仓库删除成功");
    }

    /**
     * 根据ID查找仓库
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<WarehouseDTO> getWarehouseById(@PathVariable Long id) {
        return warehouseService.findById(id)
                .map(warehouse -> ApiResponse.success(warehouse))
                .orElse(ApiResponse.notFound("仓库不存在"));
    }

    /**
     * 查找所有仓库
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or hasAuthority('ROLE_USER')")
    public ApiResponse<List<WarehouseDTO>> getAllWarehouses() {
        List<WarehouseDTO> warehouses = warehouseService.findAll();
        return ApiResponse.success(warehouses);
    }

    /**
     * 分页查询仓库
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<PageResponse<WarehouseDTO>> getWarehouses(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "code") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) Boolean enabled) {

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<WarehouseDTO> warehouses = warehouseService.findByPage(keyword, enabled, pageable);
        return ApiResponse.success(warehouses);
    }

    /**
     * 根据启用状态分页查询仓库
     */
    @GetMapping("/enabled/{enabled}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<PageResponse<WarehouseDTO>> getWarehousesByEnabled(
            @PathVariable Boolean enabled,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        PageResponse<WarehouseDTO> warehouses = warehouseService.findByEnabledAndPage(enabled, keyword, pageable);
        return ApiResponse.success(warehouses);
    }

    /**
     * 查找所有启用的仓库
     */
    @GetMapping("/enabled")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WarehouseDTO>> getAllEnabledWarehouses() {
        List<WarehouseDTO> warehouses = warehouseService.findAllEnabled();
        return ApiResponse.success(warehouses);
    }

    /**
     * 根据用户查找可访问的仓库
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or authentication.principal.id == #userId")
    public ApiResponse<List<WarehouseDTO>> getWarehousesByUser(@PathVariable Long userId) {
        List<WarehouseDTO> warehouses = warehouseService.findByUserId(userId);
        return ApiResponse.success(warehouses);
    }

    /**
     * 根据用户查找管理的仓库
     */
    @GetMapping("/managed-by/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN') or authentication.principal.id == #userId")
    public ApiResponse<List<WarehouseDTO>> getWarehousesManagedByUser(@PathVariable Long userId) {
        List<WarehouseDTO> warehouses = warehouseService.findManagedByUserId(userId);
        return ApiResponse.success(warehouses);
    }

    /**
     * 查找有库存的仓库
     */
    @GetMapping("/with-inventory")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<WarehouseDTO>> getWarehousesWithInventory() {
        List<WarehouseDTO> warehouses = warehouseService.findWarehousesWithInventory();
        return ApiResponse.success(warehouses);
    }

    /**
     * 查找指定货物有库存的仓库
     */
    @GetMapping("/with-goods-inventory/{goodsId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<WarehouseDTO>> getWarehousesWithGoodsInventory(@PathVariable Long goodsId) {
        List<WarehouseDTO> warehouses = warehouseService.findWarehousesWithGoodsInventory(goodsId);
        return ApiResponse.success(warehouses);
    }

    /**
     * 启用仓库
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> enableWarehouse(@PathVariable Long id) {
        warehouseService.enableWarehouse(id);
        return ApiResponse.success("仓库启用成功");
    }

    /**
     * 禁用仓库
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> disableWarehouse(@PathVariable Long id) {
        warehouseService.disableWarehouse(id);
        return ApiResponse.success("仓库禁用成功");
    }

    /**
     * 切换仓库状态
     */
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> toggleWarehouseStatus(@PathVariable Long id) {
        warehouseService.toggleStatus(id);
        return ApiResponse.success("仓库状态切换成功");
    }

    /**
     * 分配管理员
     */
    @PutMapping("/{id}/assign-managers")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> assignManagers(@PathVariable Long id, @RequestBody AssignManagersRequest request) {
        warehouseService.assignManagers(id, request.getUserIds());
        return ApiResponse.success("管理员分配成功");
    }

    /**
     * 移除管理员
     */
    @PutMapping("/{id}/remove-managers")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> removeManagers(@PathVariable Long id, @RequestBody RemoveManagersRequest request) {
        warehouseService.removeManagers(id, request.getUserIds());
        return ApiResponse.success("管理员移除成功");
    }

    /**
     * 检查编码是否存在
     */
    @GetMapping("/check-code")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Boolean> checkCode(@RequestParam String code, @RequestParam(required = false) Long excludeId) {
        boolean exists = excludeId != null ? 
                warehouseService.existsByCodeAndIdNot(code, excludeId) :
                warehouseService.existsByCode(code);
        return ApiResponse.success(exists);
    }

    /**
     * 检查名称是否存在
     */
    @GetMapping("/check-name")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Boolean> checkName(@RequestParam String name, @RequestParam(required = false) Long excludeId) {
        boolean exists = excludeId != null ? 
                warehouseService.existsByNameAndIdNot(name, excludeId) :
                warehouseService.existsByName(name);
        return ApiResponse.success(exists);
    }

    /**
     * 根据地区查找仓库
     */
    @GetMapping("/region/{region}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<WarehouseDTO>> getWarehousesByRegion(@PathVariable String region) {
        List<WarehouseDTO> warehouses = warehouseService.findByRegion(region);
        return ApiResponse.success(warehouses);
    }

    /**
     * 根据最小容量查找仓库
     */
    @GetMapping("/min-capacity/{minCapacity}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<WarehouseDTO>> getWarehousesByMinCapacity(@PathVariable Double minCapacity) {
        List<WarehouseDTO> warehouses = warehouseService.findByMinCapacity(minCapacity);
        return ApiResponse.success(warehouses);
    }

    /**
     * 根据最小面积查找仓库
     */
    @GetMapping("/min-area/{minArea}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<WarehouseDTO>> getWarehousesByMinArea(@PathVariable Double minArea) {
        List<WarehouseDTO> warehouses = warehouseService.findByMinArea(minArea);
        return ApiResponse.success(warehouses);
    }

    /**
     * 获取仓库统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<WarehouseService.WarehouseStatistics> getWarehouseStatistics() {
        WarehouseService.WarehouseStatistics statistics = warehouseService.getWarehouseStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 获取仓库库存统计
     */
    @GetMapping("/{id}/inventory-statistics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<WarehouseDTO.InventoryStatistics> getWarehouseInventoryStatistics(@PathVariable Long id) {
        WarehouseDTO.InventoryStatistics statistics = warehouseService.getWarehouseInventoryStatistics(id);
        return ApiResponse.success(statistics);
    }

    /**
     * 处理导出接口的OPTIONS预检请求
     */
    @RequestMapping(value = "/export", method = RequestMethod.OPTIONS)
    @CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
    public ResponseEntity<Void> exportWarehousesOptions() {
        return ResponseEntity.ok().build();
    }

    /**
     * 导出仓库数据
     */
    @GetMapping("/export")
    @CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ResponseEntity<byte[]> exportWarehouses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled) {
        try {
            byte[] excelData = warehouseService.exportWarehousesToExcel(keyword, enabled);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                "warehouses_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    // 内部DTO类

    /**
     * 分配管理员请求
     */
    public static class AssignManagersRequest {
        private List<Long> userIds;

        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
    }

    /**
     * 移除管理员请求
     */
    public static class RemoveManagersRequest {
        private List<Long> userIds;

        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
    }
}
