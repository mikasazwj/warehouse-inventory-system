package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.GoodsDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.service.GoodsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 货物控制器
 * 
 * @author Warehouse Team
 */
@RestController
@RequestMapping("/api/goods")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 创建货物
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<GoodsDTO> createGoods(@Valid @RequestBody GoodsDTO.CreateRequest request) {
        GoodsDTO goods = goodsService.createGoods(request);
        return ApiResponse.success("货物创建成功", goods);
    }

    /**
     * 更新货物
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<GoodsDTO> updateGoods(@PathVariable Long id, @Valid @RequestBody GoodsDTO.UpdateRequest request) {
        GoodsDTO goods = goodsService.updateGoods(id, request);
        return ApiResponse.success("货物更新成功", goods);
    }

    /**
     * 删除货物
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> deleteGoods(@PathVariable Long id) {
        goodsService.deleteGoods(id);
        return ApiResponse.success("货物删除成功");
    }

    /**
     * 根据ID查找货物
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GoodsDTO> getGoodsById(@PathVariable Long id) {
        return goodsService.findById(id)
                .map(goods -> ApiResponse.success(goods))
                .orElse(ApiResponse.notFound("货物不存在"));
    }

    /**
     * 根据编码查找货物
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GoodsDTO> getGoodsByCode(@PathVariable String code) {
        return goodsService.findByCode(code)
                .map(goods -> ApiResponse.success(goods))
                .orElse(ApiResponse.notFound("货物不存在"));
    }

    /**
     * 根据条形码查找货物
     */
    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GoodsDTO> getGoodsByBarcode(@PathVariable String barcode) {
        return goodsService.findByBarcode(barcode)
                .map(goods -> ApiResponse.success(goods))
                .orElse(ApiResponse.notFound("货物不存在"));
    }

    /**
     * 分页查询货物
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<GoodsDTO>> getGoods(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "code") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<GoodsDTO> goods = goodsService.findByPage(keyword, categoryId, enabled, pageable);
        return ApiResponse.success(goods);
    }

    /**
     * 根据分类分页查询货物
     */
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<GoodsDTO>> getGoodsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        PageResponse<GoodsDTO> goods = goodsService.findByCategoryAndPage(categoryId, keyword, pageable);
        return ApiResponse.success(goods);
    }



    /**
     * 根据启用状态分页查询货物
     */
    @GetMapping("/enabled/{enabled}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<GoodsDTO>> getGoodsByEnabled(
            @PathVariable Boolean enabled,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        PageResponse<GoodsDTO> goods = goodsService.findByEnabledAndPage(enabled, keyword, pageable);
        return ApiResponse.success(goods);
    }

    /**
     * 查找所有启用的货物
     */
    @GetMapping("/enabled")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GoodsDTO>> getAllEnabledGoods() {
        List<GoodsDTO> goods = goodsService.findAllEnabled();
        return ApiResponse.success(goods);
    }

    /**
     * 查找有库存的货物
     */
    @GetMapping("/with-inventory")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GoodsDTO>> getGoodsWithInventory() {
        List<GoodsDTO> goods = goodsService.findGoodsWithInventory();
        return ApiResponse.success(goods);
    }

    /**
     * 查找指定仓库有库存的货物
     */
    @GetMapping("/with-inventory/warehouse/{warehouseId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GoodsDTO>> getGoodsWithInventoryInWarehouse(@PathVariable Long warehouseId) {
        List<GoodsDTO> goods = goodsService.findGoodsWithInventoryInWarehouse(warehouseId);
        return ApiResponse.success(goods);
    }

    /**
     * 查找低库存货物
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<GoodsDTO>> getLowStockGoods() {
        List<GoodsDTO> goods = goodsService.findLowStockGoods();
        return ApiResponse.success(goods);
    }

    /**
     * 根据品牌查找货物
     */
    @GetMapping("/brand/{brand}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GoodsDTO>> getGoodsByBrand(@PathVariable String brand) {
        List<GoodsDTO> goods = goodsService.findByBrand(brand);
        return ApiResponse.success(goods);
    }

    /**
     * 根据型号查找货物
     */
    @GetMapping("/model/{model}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GoodsDTO>> getGoodsByModel(@PathVariable String model) {
        List<GoodsDTO> goods = goodsService.findByModel(model);
        return ApiResponse.success(goods);
    }

    /**
     * 查找有保质期的货物
     */
    @GetMapping("/with-shelf-life")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<GoodsDTO>> getGoodsWithShelfLife() {
        List<GoodsDTO> goods = goodsService.findGoodsWithShelfLife();
        return ApiResponse.success(goods);
    }

    /**
     * 查找设置了最低库存的货物
     */
    @GetMapping("/with-min-stock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<List<GoodsDTO>> getGoodsWithMinStock() {
        List<GoodsDTO> goods = goodsService.findGoodsWithMinStock();
        return ApiResponse.success(goods);
    }

    /**
     * 启用货物
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> enableGoods(@PathVariable Long id) {
        goodsService.enableGoods(id);
        return ApiResponse.success("货物启用成功");
    }

    /**
     * 禁用货物
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> disableGoods(@PathVariable Long id) {
        goodsService.disableGoods(id);
        return ApiResponse.success("货物禁用成功");
    }

    /**
     * 检查编码是否存在
     */
    @GetMapping("/check-code")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Boolean> checkCode(@RequestParam String code, @RequestParam(required = false) Long excludeId) {
        boolean exists = excludeId != null ? 
                goodsService.existsByCodeAndIdNot(code, excludeId) :
                goodsService.existsByCode(code);
        return ApiResponse.success(exists);
    }

    /**
     * 检查条形码是否存在
     */
    @GetMapping("/check-barcode")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Boolean> checkBarcode(@RequestParam String barcode, @RequestParam(required = false) Long excludeId) {
        boolean exists = excludeId != null ? 
                goodsService.existsByBarcodeAndIdNot(barcode, excludeId) :
                goodsService.existsByBarcode(barcode);
        return ApiResponse.success(exists);
    }

    /**
     * 获取货物统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<GoodsService.GoodsStatistics> getGoodsStatistics() {
        GoodsService.GoodsStatistics statistics = goodsService.getGoodsStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 获取货物库存统计
     */
    @GetMapping("/{id}/inventory-statistics")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GoodsDTO.InventoryStatistics> getGoodsInventoryStatistics(@PathVariable Long id) {
        GoodsDTO.InventoryStatistics statistics = goodsService.getGoodsInventoryStatistics(id);
        return ApiResponse.success(statistics);
    }

    /**
     * 切换货物启用/禁用状态
     */
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Void> toggleGoodsStatus(@PathVariable Long id) {
        goodsService.toggleStatus(id);
        return ApiResponse.success();
    }

    /**
     * 获取所有启用的货物列表（用于下拉选择等）
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GoodsDTO>> getAllGoods() {
        List<GoodsDTO> goods = goodsService.findAllEnabled();
        return ApiResponse.success(goods);
    }

    /**
     * 获取所有货物列表（包括启用和禁用的）
     */
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GoodsDTO>> getAllGoodsIncludingDisabled() {
        List<GoodsDTO> goods = goodsService.findAll();
        return ApiResponse.success(goods);
    }
}
