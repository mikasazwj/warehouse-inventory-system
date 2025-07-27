package com.warehouse.controller;

import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.GoodsCategoryDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.service.GoodsCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 * 
 * @author Warehouse Team
 */
@RestController
@RequestMapping("/api/goods-categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GoodsCategoryController {

    @Autowired
    private GoodsCategoryService categoryService;

    /**
     * 创建分类
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<GoodsCategoryDTO> createCategory(@Valid @RequestBody GoodsCategoryDTO.CreateRequest request) {
        GoodsCategoryDTO category = categoryService.createCategory(request);
        return ApiResponse.success("分类创建成功", category);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<GoodsCategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody GoodsCategoryDTO.UpdateRequest request) {
        GoodsCategoryDTO category = categoryService.updateCategory(id, request);
        return ApiResponse.success("分类更新成功", category);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success("分类删除成功");
    }

    /**
     * 根据ID查找分类
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GoodsCategoryDTO> getCategoryById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(category -> ApiResponse.success(category))
                .orElse(ApiResponse.notFound("分类不存在"));
    }

    /**
     * 获取所有分类
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GoodsCategoryDTO>> getAllCategories() {
        List<GoodsCategoryDTO> categories = categoryService.findAll();
        return ApiResponse.success(categories);
    }

    /**
     * 获取所有启用的分类
     */
    @GetMapping("/enabled")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GoodsCategoryDTO>> getEnabledCategories() {
        List<GoodsCategoryDTO> categories = categoryService.findAllEnabled();
        return ApiResponse.success(categories);
    }

    /**
     * 获取根分类
     */
    @GetMapping("/roots")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GoodsCategoryDTO>> getRootCategories() {
        List<GoodsCategoryDTO> categories = categoryService.findRootCategories();
        return ApiResponse.success(categories);
    }

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GoodsCategoryDTO>> getCategoryTree() {
        List<GoodsCategoryDTO> tree = categoryService.getCategoryTree();
        return ApiResponse.success(tree);
    }

    /**
     * 分页查询分类
     */
    @GetMapping("/page")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<GoodsCategoryDTO>> getCategories(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        PageResponse<GoodsCategoryDTO> categories = categoryService.findByPage(keyword, pageable);
        return ApiResponse.success(categories);
    }

    /**
     * 启用分类
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> enableCategory(@PathVariable Long id) {
        categoryService.enableCategory(id);
        return ApiResponse.success("分类启用成功");
    }

    /**
     * 禁用分类
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<String> disableCategory(@PathVariable Long id) {
        categoryService.disableCategory(id);
        return ApiResponse.success("分类禁用成功");
    }

    /**
     * 检查分类编码是否存在
     */
    @GetMapping("/check-code")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Boolean> checkCode(@RequestParam String code, @RequestParam(required = false) Long excludeId) {
        boolean exists = excludeId != null ? 
                categoryService.existsByCodeAndIdNot(code, excludeId) :
                categoryService.existsByCode(code);
        return ApiResponse.success(exists);
    }

    /**
     * 检查分类名称是否存在
     */
    @GetMapping("/check-name")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('WAREHOUSE_ADMIN')")
    public ApiResponse<Boolean> checkName(@RequestParam String name, @RequestParam(required = false) Long excludeId) {
        boolean exists = excludeId != null ? 
                categoryService.existsByNameAndIdNot(name, excludeId) :
                categoryService.existsByName(name);
        return ApiResponse.success(exists);
    }
}
