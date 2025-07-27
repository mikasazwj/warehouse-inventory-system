package com.warehouse.service.impl;

import com.warehouse.dto.GoodsCategoryDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.entity.GoodsCategory;
import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.GoodsCategoryRepository;
import com.warehouse.repository.GoodsRepository;
import com.warehouse.service.GoodsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 * 
 * @author Warehouse Team
 */
@Service
@Transactional
public class GoodsCategoryServiceImpl implements GoodsCategoryService {

    @Autowired
    private GoodsCategoryRepository categoryRepository;

    @Autowired
    private GoodsRepository goodsRepository;

    @Override
    public GoodsCategoryDTO createCategory(GoodsCategoryDTO.CreateRequest request) {
        // 检查编码是否存在
        if (categoryRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            throw new BusinessException("分类编码已存在");
        }

        // 检查名称是否存在
        if (categoryRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new BusinessException("分类名称已存在");
        }

        GoodsCategory category = new GoodsCategory();
        category.setCode(request.getCode());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());
        category.setEnabled(request.getEnabled());

        // 设置父分类
        if (request.getParentId() != null) {
            GoodsCategory parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父分类", "id", request.getParentId()));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    public GoodsCategoryDTO updateCategory(Long id, GoodsCategoryDTO.UpdateRequest request) {
        GoodsCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类", "id", id));

        // 检查名称是否存在（排除当前分类）
        if (request.getName() != null && 
            categoryRepository.existsByNameAndIdNotAndDeletedFalse(request.getName(), id)) {
            throw new BusinessException("分类名称已存在");
        }

        // 更新字段
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getEnabled() != null) {
            category.setEnabled(request.getEnabled());
        }

        // 更新父分类
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException("不能将自己设置为父分类");
            }
            GoodsCategory parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父分类", "id", request.getParentId()));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    public void deleteCategory(Long id) {
        GoodsCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类", "id", id));

        // 检查是否有子分类
        if (categoryRepository.existsByParentIdAndDeletedFalse(id)) {
            throw new BusinessException("该分类下还有子分类，无法删除");
        }

        // 检查是否有商品
        if (goodsRepository.existsByCategoryIdAndDeletedFalse(id)) {
            throw new BusinessException("该分类下还有商品，无法删除");
        }

        category.setDeleted(true);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GoodsCategoryDTO> findById(Long id) {
        return categoryRepository.findById(id)
                .filter(category -> !category.getDeleted())
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GoodsCategoryDTO> findByCode(String code) {
        return categoryRepository.findByCodeAndDeletedFalse(code)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GoodsCategoryDTO> findByName(String name) {
        return categoryRepository.findByNameAndDeletedFalse(name)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsCategoryDTO> findAll() {
        return categoryRepository.findByDeletedFalseOrderBySortOrderAscNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsCategoryDTO> findAllEnabled() {
        return categoryRepository.findByEnabledTrueAndDeletedFalseOrderBySortOrderAscNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsCategoryDTO> findRootCategories() {
        return categoryRepository.findByParentIsNullAndDeletedFalseOrderBySortOrderAscNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsCategoryDTO> findEnabledRootCategories() {
        return categoryRepository.findByParentIsNullAndEnabledTrueAndDeletedFalseOrderBySortOrderAscNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsCategoryDTO> findByParent(Long parentId) {
        return categoryRepository.findByParentIdAndDeletedFalseOrderBySortOrderAscNameAsc(parentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsCategoryDTO> findEnabledByParent(Long parentId) {
        return categoryRepository.findByParentIdAndEnabledTrueAndDeletedFalseOrderBySortOrderAscNameAsc(parentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsCategoryDTO> getCategoryTree() {
        List<GoodsCategory> rootCategories = categoryRepository.findByParentIsNullAndDeletedFalseOrderBySortOrderAscNameAsc();
        return rootCategories.stream()
                .map(this::convertToDTOWithChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsCategoryDTO> getEnabledCategoryTree() {
        List<GoodsCategory> rootCategories = categoryRepository.findByParentIsNullAndEnabledTrueAndDeletedFalseOrderBySortOrderAscNameAsc();
        return rootCategories.stream()
                .map(this::convertToDTOWithEnabledChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GoodsCategoryDTO> findByPage(String keyword, Pageable pageable) {
        Page<GoodsCategory> page = categoryRepository.findByKeyword(keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GoodsCategoryDTO> findByParentAndPage(Long parentId, String keyword, Pageable pageable) {
        Page<GoodsCategory> page = categoryRepository.findByParentAndKeyword(parentId, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    public void enableCategory(Long id) {
        GoodsCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类", "id", id));
        category.setEnabled(true);
        categoryRepository.save(category);
    }

    @Override
    public void disableCategory(Long id) {
        GoodsCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类", "id", id));
        category.setEnabled(false);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return categoryRepository.existsByCodeAndDeletedFalse(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameAndDeletedFalse(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return categoryRepository.existsByCodeAndIdNotAndDeletedFalse(code, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndIdNot(String name, Long id) {
        return categoryRepository.existsByNameAndIdNotAndDeletedFalse(name, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasChildren(Long id) {
        return categoryRepository.existsByParentIdAndDeletedFalse(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasGoods(Long id) {
        return goodsRepository.existsByCategoryIdAndDeletedFalse(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryStatistics getCategoryStatistics() {
        long totalCategories = categoryRepository.countByDeletedFalse();
        long enabledCategories = categoryRepository.countByEnabledTrueAndDeletedFalse();
        long rootCategories = categoryRepository.countByParentIsNullAndDeletedFalse();
        long categoriesWithGoods = categoryRepository.findCategoriesWithGoods().size();

        return new CategoryStatistics(totalCategories, enabledCategories, rootCategories, categoriesWithGoods);
    }

    /**
     * 转换为DTO
     */
    private GoodsCategoryDTO convertToDTO(GoodsCategory category) {
        GoodsCategoryDTO dto = new GoodsCategoryDTO();
        dto.setId(category.getId());
        dto.setCode(category.getCode());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setSortOrder(category.getSortOrder());
        dto.setEnabled(category.getEnabled());
        dto.setLevel(category.getLevel());
        dto.setPath(category.getPath());
        dto.setCreatedTime(category.getCreatedTime());
        dto.setUpdatedTime(category.getUpdatedTime());
        dto.setCreatedBy(category.getCreatedBy());
        dto.setUpdatedBy(category.getUpdatedBy());

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        return dto;
    }

    /**
     * 转换为DTO（包含子分类）
     */
    private GoodsCategoryDTO convertToDTOWithChildren(GoodsCategory category) {
        GoodsCategoryDTO dto = convertToDTO(category);
        
        List<GoodsCategoryDTO> children = category.getChildren().stream()
                .filter(child -> !child.getDeleted())
                .map(this::convertToDTOWithChildren)
                .collect(Collectors.toList());
        dto.setChildren(children);

        return dto;
    }

    /**
     * 转换为DTO（包含启用的子分类）
     */
    private GoodsCategoryDTO convertToDTOWithEnabledChildren(GoodsCategory category) {
        GoodsCategoryDTO dto = convertToDTO(category);
        
        List<GoodsCategoryDTO> children = category.getChildren().stream()
                .filter(child -> !child.getDeleted() && child.getEnabled())
                .map(this::convertToDTOWithEnabledChildren)
                .collect(Collectors.toList());
        dto.setChildren(children);

        return dto;
    }
}
