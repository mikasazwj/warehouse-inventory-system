package com.warehouse.service.impl;

import com.warehouse.dto.GoodsDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.entity.Goods;
import com.warehouse.entity.GoodsCategory;

import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.GoodsCategoryRepository;
import com.warehouse.repository.GoodsRepository;
import com.warehouse.repository.InventoryRepository;

import com.warehouse.service.GoodsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 货物服务实现类
 * 
 * @author Warehouse Team
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private GoodsCategoryRepository categoryRepository;



    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public GoodsDTO createGoods(GoodsDTO.CreateRequest request) {
        // 检查编码是否存在
        if (goodsRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            throw new BusinessException("货物编码已存在");
        }

        // 检查条形码是否存在
        if (request.getBarcode() != null && goodsRepository.existsByBarcodeAndDeletedFalse(request.getBarcode())) {
            throw new BusinessException("条形码已存在");
        }

        // 验证分类是否存在
        GoodsCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("货物分类", "id", request.getCategoryId()));



        // 创建货物
        Goods goods = new Goods();
        goods.setCode(request.getCode());
        goods.setName(request.getName());
        goods.setShortName(request.getShortName());
        goods.setModel(request.getModel());
        goods.setSpecification(request.getSpecification());
        goods.setBrand(request.getBrand());
        goods.setBarcode(request.getBarcode());
        goods.setCategory(category);

        goods.setUnit(request.getUnit());
        goods.setWeight(request.getWeight() != null ? BigDecimal.valueOf(request.getWeight()) : null);
        goods.setVolume(request.getVolume() != null ? BigDecimal.valueOf(request.getVolume()) : null);

        goods.setMinStock(request.getMinStock());
        goods.setMaxStock(request.getMaxStock());
        goods.setSafetyStock(request.getSafetyStock());
        goods.setShelfLifeDays(request.getShelfLifeDays());
        goods.setStorageConditions(request.getStorageConditions());
        goods.setRemark(request.getRemark());

        goods = goodsRepository.save(goods);
        return convertToDTO(goods);
    }

    @Override
    public GoodsDTO updateGoods(Long id, GoodsDTO.UpdateRequest request) {
        Goods goods = goodsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("货物", "id", id));

        // 检查条形码是否存在（排除当前货物）
        if (request.getBarcode() != null && 
            goodsRepository.existsByBarcodeAndIdNotAndDeletedFalse(request.getBarcode(), id)) {
            throw new BusinessException("条形码已存在");
        }

        // 验证分类是否存在（如果提供）
        if (request.getCategoryId() != null) {
            GoodsCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("货物分类", "id", request.getCategoryId()));
            goods.setCategory(category);
        }



        // 更新货物信息
        goods.setName(request.getName());
        goods.setShortName(request.getShortName());
        goods.setModel(request.getModel());
        goods.setSpecification(request.getSpecification());
        goods.setBrand(request.getBrand());
        goods.setBarcode(request.getBarcode());
        goods.setUnit(request.getUnit());
        goods.setWeight(request.getWeight() != null ? BigDecimal.valueOf(request.getWeight()) : null);
        goods.setVolume(request.getVolume() != null ? BigDecimal.valueOf(request.getVolume()) : null);

        goods.setMinStock(request.getMinStock());
        goods.setMaxStock(request.getMaxStock());
        goods.setSafetyStock(request.getSafetyStock());
        goods.setShelfLifeDays(request.getShelfLifeDays());
        goods.setStorageConditions(request.getStorageConditions());
        if (request.getEnabled() != null) {
            goods.setEnabled(request.getEnabled());
        }
        goods.setRemark(request.getRemark());

        goods = goodsRepository.save(goods);
        return convertToDTO(goods);
    }

    @Override
    public void deleteGoods(Long id) {
        Goods goods = goodsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("货物", "id", id));

        // 检查是否有库存
        long inventoryCount = inventoryRepository.countWithStockByGoods(id);
        if (inventoryCount > 0) {
            throw new BusinessException("货物还有库存，不能删除");
        }

        goods.setDeleted(true);
        goodsRepository.save(goods);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Goods goods = goodsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("货物", "id", id));

        goods.setEnabled(!goods.getEnabled());
        goodsRepository.save(goods);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsDTO> findAllEnabled() {
        List<Goods> goods = goodsRepository.findByEnabledTrueAndDeletedFalseOrderByCode();
        return goods.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GoodsDTO> findById(Long id) {
        return goodsRepository.findById(id)
                .filter(goods -> !goods.getDeleted())
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GoodsDTO> findByCode(String code) {
        return goodsRepository.findByCodeAndDeletedFalse(code)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GoodsDTO> findByBarcode(String barcode) {
        return goodsRepository.findByBarcodeAndDeletedFalse(barcode)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsDTO> findAll() {
        return goodsRepository.findByDeletedFalseOrderByCode()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional(readOnly = true)
    public List<GoodsDTO> findByCategory(Long categoryId) {
        return goodsRepository.findByCategoryIdAndDeletedFalseOrderByCode(categoryId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional(readOnly = true)
    public PageResponse<GoodsDTO> findByPage(String keyword, Long categoryId, Boolean enabled, Pageable pageable) {
        Page<Goods> page = goodsRepository.findByKeywordAndCategoryAndEnabled(keyword, categoryId, enabled, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GoodsDTO> findByCategoryAndPage(Long categoryId, String keyword, Pageable pageable) {
        Page<Goods> page = goodsRepository.findByCategoryAndKeyword(categoryId, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }



    @Override
    @Transactional(readOnly = true)
    public PageResponse<GoodsDTO> findByEnabledAndPage(Boolean enabled, String keyword, Pageable pageable) {
        Page<Goods> page = goodsRepository.findByEnabledAndKeyword(enabled, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    public void enableGoods(Long id) {
        Goods goods = goodsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("货物", "id", id));
        goods.setEnabled(true);
        goodsRepository.save(goods);
    }

    @Override
    public void disableGoods(Long id) {
        Goods goods = goodsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("货物", "id", id));
        goods.setEnabled(false);
        goodsRepository.save(goods);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return goodsRepository.existsByCodeAndDeletedFalse(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByBarcode(String barcode) {
        return goodsRepository.existsByBarcodeAndDeletedFalse(barcode);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return goodsRepository.existsByCodeAndIdNotAndDeletedFalse(code, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByBarcodeAndIdNot(String barcode, Long id) {
        return goodsRepository.existsByBarcodeAndIdNotAndDeletedFalse(barcode, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsDTO> findGoodsWithInventory() {
        return goodsRepository.findGoodsWithInventory()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsDTO> findGoodsWithInventoryInWarehouse(Long warehouseId) {
        return goodsRepository.findGoodsWithInventoryInWarehouse(warehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsDTO> findLowStockGoods() {
        return goodsRepository.findLowStockGoods()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsDTO> findByBrand(String brand) {
        return goodsRepository.findByBrandContaining(brand)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsDTO> findByModel(String model) {
        return goodsRepository.findByModelContaining(model)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsDTO> findGoodsWithShelfLife() {
        return goodsRepository.findGoodsWithShelfLife()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsDTO> findGoodsWithMinStock() {
        return goodsRepository.findGoodsWithMinStock()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsStatistics getGoodsStatistics() {
        long totalGoods = goodsRepository.countNotDeleted();
        long enabledGoods = goodsRepository.countEnabledAndNotDeleted();
        long goodsWithInventory = goodsRepository.findGoodsWithInventory().size();
        long lowStockGoods = goodsRepository.findLowStockGoods().size();
        long goodsWithShelfLife = goodsRepository.findGoodsWithShelfLife().size();
        long categoriesCount = categoryRepository.countNotDeleted();
        return new GoodsStatistics(totalGoods, enabledGoods, goodsWithInventory,
                                 lowStockGoods, goodsWithShelfLife, categoriesCount, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsDTO.InventoryStatistics getGoodsInventoryStatistics(Long goodsId) {
        // 获取货物的库存统计
        Long totalQuantityLong = inventoryRepository.sumQuantityByGoods(goodsId);
        Long availableQuantityLong = inventoryRepository.sumAvailableQuantityByGoods(goodsId);
        Long lockedQuantityLong = inventoryRepository.sumLockedQuantityByGoods(goodsId);
        BigDecimal totalValue = inventoryRepository.sumValueByGoods(goodsId);
        Long warehouseCountLong = inventoryRepository.countWarehousesByGoods(goodsId);

        BigDecimal totalQuantity = totalQuantityLong != null ? BigDecimal.valueOf(totalQuantityLong) : BigDecimal.ZERO;
        BigDecimal availableQuantity = availableQuantityLong != null ? BigDecimal.valueOf(availableQuantityLong) : BigDecimal.ZERO;
        BigDecimal lockedQuantity = lockedQuantityLong != null ? BigDecimal.valueOf(lockedQuantityLong) : BigDecimal.ZERO;
        int warehouseCount = warehouseCountLong != null ? warehouseCountLong.intValue() : 0;

        // 检查是否低库存
        Optional<Goods> goodsOpt = goodsRepository.findById(goodsId);
        boolean isLowStock = false;
        if (goodsOpt.isPresent() && goodsOpt.get().getMinStock() != null) {
            isLowStock = totalQuantity.compareTo(goodsOpt.get().getMinStock()) < 0;
        }

        GoodsDTO.InventoryStatistics statistics = new GoodsDTO.InventoryStatistics();
        statistics.setTotalQuantity(totalQuantity != null ? totalQuantity : BigDecimal.ZERO);
        statistics.setAvailableQuantity(availableQuantity != null ? availableQuantity : BigDecimal.ZERO);
        statistics.setLockedQuantity(lockedQuantity != null ? lockedQuantity : BigDecimal.ZERO);
        statistics.setTotalValue(totalValue != null ? totalValue : BigDecimal.ZERO);
        statistics.setWarehouseCount(warehouseCount);
        statistics.setIsLowStock(isLowStock);
        return statistics;
    }

    /**
     * 转换为DTO
     */
    private GoodsDTO convertToDTO(Goods goods) {
        GoodsDTO dto = new GoodsDTO();
        BeanUtils.copyProperties(goods, dto);
        
        // 设置分类信息
        if (goods.getCategory() != null) {
            dto.setCategoryId(goods.getCategory().getId());
            dto.setCategoryName(goods.getCategory().getName());
        }
        

        
        // 获取库存统计信息
        dto.setInventoryStatistics(getGoodsInventoryStatistics(goods.getId()));
        
        return dto;
    }
}
