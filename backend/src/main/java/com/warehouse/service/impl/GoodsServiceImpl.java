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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
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

    @PersistenceContext
    private EntityManager entityManager;



    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public GoodsDTO createGoods(GoodsDTO.CreateRequest request) {
        // 验证分类是否存在
        GoodsCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("货物分类", "id", request.getCategoryId()));
        }

        // 检查货物名称、分类、规格组合是否存在
        String specification = request.getSpecification() != null ? request.getSpecification() : "";
        Optional<Goods> existingGoods = goodsRepository.findByNameAndCategoryAndSpecificationAndDeletedFalse(
            request.getName(), category, specification);
        if (existingGoods.isPresent()) {
            throw new BusinessException("相同名称、分类和规格的货物已存在");
        }

        // 检查条形码是否存在
        if (request.getBarcode() != null && goodsRepository.existsByBarcodeAndDeletedFalse(request.getBarcode())) {
            throw new BusinessException("条形码已存在");
        }



        // 创建货物
        Goods goods = new Goods();
        // 如果没有提供编码，自动生成
        String code = request.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = generateGoodsCode();
        }
        goods.setCode(code);
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
    @Override
    public GoodsDTO.ImportResult importGoods(List<GoodsDTO.ImportData> importDataList) {
        GoodsDTO.ImportResult result = new GoodsDTO.ImportResult();
        int successCount = 0;
        int failCount = 0;
        int updateCount = 0;
        int skipCount = 0;

        System.out.println("=== 开始导入货物，总数: " + importDataList.size() + " ===");

        for (int i = 0; i < importDataList.size(); i++) {
            GoodsDTO.ImportData importData = importDataList.get(i);
            int rowNumber = i + 2; // Excel行号从2开始（第1行是表头）

            try {
                System.out.println("处理第 " + rowNumber + " 行: " + importData.getCode() + " - " + importData.getName());

                // 验证必填字段
                if (importData.getName() == null || importData.getName().trim().isEmpty()) {
                    result.addError(rowNumber, "货物名称不能为空");
                    failCount++;
                    continue;
                }

                // 使用独立事务处理单个货物
                boolean success = importSingleGoods(importData, rowNumber, result);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }

            } catch (Exception e) {
                System.out.println("处理第 " + rowNumber + " 行失败: " + e.getMessage());
                e.printStackTrace();
                result.addError(rowNumber, "导入失败: " + e.getMessage());
                failCount++;
            }
        }

        result.setSuccessCount(successCount);
        result.setFailCount(failCount);

        System.out.println("=== 导入完成 ===");
        System.out.println("新增: " + successCount + ", 更新: " + updateCount + ", 跳过: " + skipCount + ", 失败: " + failCount);

        return result;
    }

    /**
     * 查找或创建分类
     */
    private GoodsCategory findOrCreateCategory(String categoryCode, int rowNumber, GoodsDTO.ImportResult result) {
        try {
            // 查找现有分类
            Optional<GoodsCategory> existingCategory = categoryRepository.findByCodeAndDeletedFalse(categoryCode);
            if (existingCategory.isPresent()) {
                System.out.println("使用现有分类: " + categoryCode);
                return existingCategory.get();
            }

            // 创建新分类
            System.out.println("创建新分类: " + categoryCode);
            GoodsCategory category = new GoodsCategory();
            category.setCode(categoryCode);
            category.setName(categoryCode);
            category.setDescription("导入时自动创建的分类");
            category.setEnabled(true);
            category.setSortOrder(999);
            category = categoryRepository.save(category);

            result.addWarning(rowNumber, "自动创建了新分类: " + categoryCode);
            return category;

        } catch (Exception e) {
            System.out.println("处理分类失败: " + categoryCode + ", 错误: " + e.getMessage());
            result.addError(rowNumber, "创建分类失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 自动生成货物编码
     */
    private String generateGoodsCode() {
        // 简单的编码生成策略：GOODS + 时间戳
        return "GOODS" + System.currentTimeMillis();
    }

    /**
     * 查找已删除的货物
     */
    private Optional<Goods> findDeletedGoodsByNameCategorySpec(String name, GoodsCategory category, String specification) {
        return goodsRepository.findByNameAndCategoryAndSpecificationAndDeletedTrue(name, category, specification);
    }

    /**
     * 使用独立事务处理单个货物导入
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean importSingleGoods(GoodsDTO.ImportData importData, int rowNumber, GoodsDTO.ImportResult result) {
        try {
            String goodsName = importData.getName().trim();
            String specification = importData.getSpecification() != null ? importData.getSpecification().trim() : "";
            String categoryCode = importData.getCategoryCode() != null ? importData.getCategoryCode().trim() : "";

            // 查找或创建分类
            GoodsCategory category = null;
            if (!categoryCode.isEmpty()) {
                category = findOrCreateCategory(categoryCode, rowNumber, result);
                if (category == null) {
                    return false;
                }
            }

            // 检查是否存在相同的货物（包括已删除的记录）
            System.out.println("检查重复货物: 名称=" + goodsName + ", 分类ID=" + (category != null ? category.getId() : "null") + ", 规格=" + specification);

            // 先检查未删除的记录
            Optional<Goods> existingActiveGoods = goodsRepository.findByNameAndCategoryAndSpecificationAndDeletedFalse(
                goodsName, category, specification);

            if (existingActiveGoods.isPresent()) {
                System.out.println("跳过重复货物(活跃): " + goodsName + " (分类: " + categoryCode + ", 规格: " + specification + "), 已存在ID: " + existingActiveGoods.get().getId());
                result.addWarning(rowNumber, "跳过重复货物: " + goodsName + " (规格: " + specification + ")");
                return true; // 跳过不算失败
            }

            // 再检查已删除的记录，如果存在则恢复
            Optional<Goods> existingDeletedGoods = findDeletedGoodsByNameCategorySpec(goodsName, category, specification);
            if (existingDeletedGoods.isPresent()) {
                System.out.println("恢复已删除的货物: " + goodsName + " (分类: " + categoryCode + ", 规格: " + specification + "), ID: " + existingDeletedGoods.get().getId());
                Goods deletedGoods = existingDeletedGoods.get();

                // 恢复货物并更新数据
                deletedGoods.setDeleted(false);
                deletedGoods.setUnit(importData.getUnit() != null ? importData.getUnit().trim() : null);
                deletedGoods.setMinStock(importData.getMinStock() != null ? importData.getMinStock() : BigDecimal.ZERO);
                deletedGoods.setMaxStock(importData.getMaxStock() != null ? importData.getMaxStock() : BigDecimal.ZERO);
                deletedGoods.setRemark(importData.getDescription() != null ? importData.getDescription().trim() : null);
                deletedGoods.setEnabled(true);

                goodsRepository.save(deletedGoods);
                System.out.println("成功恢复货物: " + goodsName + " (分类: " + categoryCode + ", 规格: " + specification + ")");
                return true;
            }

            System.out.println("货物不存在，可以创建: " + goodsName);

            // 创建新货物
            Goods goods = new Goods();
            goods.setCode(generateGoodsCode()); // 自动生成编码
            goods.setName(goodsName);
            goods.setCategory(category);
            goods.setUnit(importData.getUnit() != null ? importData.getUnit().trim() : null);
            goods.setSpecification(specification);
            goods.setMinStock(importData.getMinStock() != null ? importData.getMinStock() : BigDecimal.ZERO);
            goods.setMaxStock(importData.getMaxStock() != null ? importData.getMaxStock() : BigDecimal.ZERO);
            goods.setRemark(importData.getDescription() != null ? importData.getDescription().trim() : null);
            goods.setEnabled(true);

            // 保存货物
            goodsRepository.save(goods);
            System.out.println("成功导入货物: " + goodsName + " (分类: " + categoryCode + ", 规格: " + specification + ")");
            return true;

        } catch (Exception e) {
            System.out.println("保存货物失败: " + importData.getName() + ", 详细错误: " + e.getMessage());
            e.printStackTrace();

            // 清理Session，避免Session污染
            try {
                entityManager.clear();
                System.out.println("Session已清理");
            } catch (Exception clearException) {
                System.out.println("清理Session失败: " + clearException.getMessage());
            }

            // 检查是否是约束冲突
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("constraint")) {
                result.addError(rowNumber, "数据约束冲突: " + importData.getName() + " 可能已存在相似记录");
            } else {
                result.addError(rowNumber, "保存失败: " + errorMsg);
            }
            return false;
        }
    }

    @Transactional
    private void cleanupDeletedGoods() {
        try {
            // 物理删除已软删除的货物，避免唯一约束冲突
            List<Goods> deletedGoods = goodsRepository.findByDeletedTrue();
            if (!deletedGoods.isEmpty()) {
                System.out.println("清理已删除的货物数量: " + deletedGoods.size());
                for (Goods goods : deletedGoods) {
                    System.out.println("物理删除货物: " + goods.getCode());
                }
                goodsRepository.deleteAll(deletedGoods);
                System.out.println("清理完成");
            }
        } catch (Exception e) {
            System.out.println("清理已删除货物失败: " + e.getMessage());
        }
    }





    @Override
    public byte[] exportGoodsToExcel(String keyword, Long categoryId, Boolean enabled) {
        try {
            // 获取所有货物数据（不分页）
            List<Goods> goodsList;
            if (keyword != null || categoryId != null || enabled != null) {
                // 使用筛选条件
                goodsList = goodsRepository.findAll().stream()
                    .filter(goods -> {
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            String lowerKeyword = keyword.toLowerCase();
                            if (!goods.getName().toLowerCase().contains(lowerKeyword) &&
                                !goods.getCode().toLowerCase().contains(lowerKeyword)) {
                                return false;
                            }
                        }
                        if (categoryId != null && !categoryId.equals(goods.getCategory().getId())) {
                            return false;
                        }
                        if (enabled != null && !enabled.equals(goods.getEnabled())) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
            } else {
                goodsList = goodsRepository.findAll();
            }

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("货物数据");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"货物编码", "货物名称", "分类名称", "分类编码", "单位", "规格/型号",
                              "最小库存", "最大库存", "状态", "创建时间", "备注"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            for (int i = 0; i < goodsList.size(); i++) {
                Goods goods = goodsList.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(goods.getCode() != null ? goods.getCode() : "");
                row.createCell(1).setCellValue(goods.getName() != null ? goods.getName() : "");
                row.createCell(2).setCellValue(goods.getCategory() != null ? goods.getCategory().getName() : "");
                row.createCell(3).setCellValue(goods.getCategory() != null ? goods.getCategory().getCode() : "");
                row.createCell(4).setCellValue(goods.getUnit() != null ? goods.getUnit() : "");
                row.createCell(5).setCellValue(goods.getSpecification() != null ? goods.getSpecification() : "");
                row.createCell(6).setCellValue(goods.getMinStock() != null ? goods.getMinStock().doubleValue() : 0);
                row.createCell(7).setCellValue(goods.getMaxStock() != null ? goods.getMaxStock().doubleValue() : 0);
                row.createCell(8).setCellValue(goods.getEnabled() != null && goods.getEnabled() ? "启用" : "禁用");
                row.createCell(9).setCellValue(goods.getCreatedTime() != null ? goods.getCreatedTime().toString() : "");
                row.createCell(10).setCellValue(goods.getRemark() != null ? goods.getRemark() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

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
