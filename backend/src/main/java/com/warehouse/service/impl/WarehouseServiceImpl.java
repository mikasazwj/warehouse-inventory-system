package com.warehouse.service.impl;

import com.warehouse.dto.PageResponse;
import com.warehouse.dto.UserDTO;
import com.warehouse.dto.WarehouseDTO;
import com.warehouse.entity.User;
import com.warehouse.entity.Warehouse;
import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.InventoryRepository;
import com.warehouse.repository.UserRepository;
import com.warehouse.repository.WarehouseRepository;
import com.warehouse.service.WarehouseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 仓库服务实现类
 * 
 * @author Warehouse Team
 */
@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public WarehouseDTO createWarehouse(WarehouseDTO.CreateRequest request) {
        // 检查编码是否存在
        if (warehouseRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            throw new BusinessException("仓库编码已存在");
        }

        // 检查名称是否存在
        if (warehouseRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new BusinessException("仓库名称已存在");
        }

        // 创建仓库
        Warehouse warehouse = new Warehouse();
        warehouse.setCode(request.getCode());
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setContactPerson(request.getContactPerson());
        warehouse.setContactPhone(request.getContactPhone());

        warehouse.setArea(request.getArea());
        warehouse.setCapacity(request.getCapacity());
        warehouse.setRemark(request.getRemark());

        // 分配管理员
        if (request.getManagerIds() != null && !request.getManagerIds().isEmpty()) {
            List<User> managers = userRepository.findAllById(request.getManagerIds());
            warehouse.setManagers(managers.stream().collect(Collectors.toSet()));
        }

        warehouse = warehouseRepository.save(warehouse);
        return convertToDTO(warehouse);
    }

    @Override
    public WarehouseDTO updateWarehouse(Long id, WarehouseDTO.UpdateRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", id));

        // 检查名称是否存在（排除当前仓库）
        if (warehouseRepository.existsByNameAndIdNotAndDeletedFalse(request.getName(), id)) {
            throw new BusinessException("仓库名称已存在");
        }

        // 更新仓库信息
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setContactPerson(request.getContactPerson());
        warehouse.setContactPhone(request.getContactPhone());

        warehouse.setArea(request.getArea());
        warehouse.setCapacity(request.getCapacity());
        if (request.getEnabled() != null) {
            warehouse.setEnabled(request.getEnabled());
        }
        warehouse.setRemark(request.getRemark());

        // 更新管理员
        if (request.getManagerIds() != null) {
            warehouse.getManagers().clear();
            if (!request.getManagerIds().isEmpty()) {
                List<User> managers = userRepository.findAllById(request.getManagerIds());
                warehouse.setManagers(managers.stream().collect(Collectors.toSet()));
            }
        }

        warehouse = warehouseRepository.save(warehouse);
        return convertToDTO(warehouse);
    }

    @Override
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", id));

        // 检查是否有库存
        long inventoryCount = inventoryRepository.countWithStockByWarehouse(id);
        if (inventoryCount > 0) {
            throw new BusinessException("仓库中还有库存，不能删除");
        }

        warehouse.setDeleted(true);
        warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WarehouseDTO> findById(Long id) {
        return warehouseRepository.findById(id)
                .filter(warehouse -> !warehouse.getDeleted())
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WarehouseDTO> findByCode(String code) {
        return warehouseRepository.findByCodeAndDeletedFalse(code)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WarehouseDTO> findByName(String name) {
        return warehouseRepository.findByNameAndDeletedFalse(name)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> findAll() {
        return warehouseRepository.findByDeletedFalseOrderByCode()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> findAllEnabled() {
        return warehouseRepository.findByEnabledTrueAndDeletedFalseOrderByCode()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> findByUserId(Long userId) {
        return warehouseRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> findManagedByUserId(Long userId) {
        return warehouseRepository.findManagedByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseDTO> findByPage(String keyword, Boolean enabled, Pageable pageable) {
        Page<Warehouse> page;
        if (enabled != null) {
            page = warehouseRepository.findByEnabledAndKeyword(enabled, keyword, pageable);
        } else {
            page = warehouseRepository.findByKeyword(keyword, pageable);
        }
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseDTO> findByEnabledAndPage(Boolean enabled, String keyword, Pageable pageable) {
        Page<Warehouse> page = warehouseRepository.findByEnabledAndKeyword(enabled, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseDTO> findByUserIdAndPage(Long userId, String keyword, Pageable pageable) {
        Page<Warehouse> page = warehouseRepository.findByUserIdAndKeyword(userId, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    public void enableWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", id));
        warehouse.setEnabled(true);
        warehouseRepository.save(warehouse);
    }

    @Override
    public void disableWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", id));
        warehouse.setEnabled(false);
        warehouseRepository.save(warehouse);
    }

    @Override
    public void toggleStatus(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", id));
        warehouse.setEnabled(!warehouse.getEnabled());
        warehouseRepository.save(warehouse);
    }

    @Override
    public boolean isWarehouseManagedByUser(Long warehouseId, Long userId) {
        List<WarehouseDTO> managedWarehouses = findManagedByUserId(userId);
        return managedWarehouses.stream()
                .anyMatch(warehouse -> warehouse.getId().equals(warehouseId));
    }

    @Override
    public void assignManagers(Long warehouseId, List<Long> userIds) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", warehouseId));

        List<User> users = userRepository.findAllById(userIds);
        users.forEach(warehouse::addManager);
        warehouseRepository.save(warehouse);
    }

    @Override
    public void removeManagers(Long warehouseId, List<Long> userIds) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", warehouseId));

        List<User> users = userRepository.findAllById(userIds);
        users.forEach(warehouse::removeManager);
        warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return warehouseRepository.existsByCodeAndDeletedFalse(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return warehouseRepository.existsByNameAndDeletedFalse(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return warehouseRepository.existsByCodeAndIdNotAndDeletedFalse(code, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndIdNot(String name, Long id) {
        return warehouseRepository.existsByNameAndIdNotAndDeletedFalse(name, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> findWarehousesWithInventory() {
        return warehouseRepository.findWarehousesWithInventory()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> findWarehousesWithGoodsInventory(Long goodsId) {
        return warehouseRepository.findWarehousesWithGoodsInventory(goodsId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> findByRegion(String region) {
        return warehouseRepository.findByRegion(region)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> findByMinCapacity(Double minCapacity) {
        return warehouseRepository.findByMinCapacity(minCapacity)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> findByMinArea(Double minArea) {
        return warehouseRepository.findByMinArea(minArea)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseStatistics getWarehouseStatistics() {
        long totalWarehouses = warehouseRepository.countNotDeleted();
        long enabledWarehouses = warehouseRepository.countEnabledAndNotDeleted();
        long warehousesWithInventory = warehouseRepository.findWarehousesWithInventory().size();
        
        // 这里简化处理，实际项目中可能需要更复杂的统计
        double totalArea = 0.0;
        double totalCapacity = 0.0;
        double averageCapacityUtilization = 0.0;

        return new WarehouseStatistics(totalWarehouses, enabledWarehouses, warehousesWithInventory,
                                     totalArea, totalCapacity, averageCapacityUtilization);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO.InventoryStatistics getWarehouseInventoryStatistics(Long warehouseId) {
        long totalItems = inventoryRepository.countByWarehouse(warehouseId);
        long lowStockItems = inventoryRepository.countLowStockByWarehouse(warehouseId);
        long nearExpiryItems = inventoryRepository.countNearExpiry(LocalDate.now().plusDays(30));
        
        BigDecimal totalValue = inventoryRepository.calculateTotalValueByWarehouse(warehouseId);
        
        // 这里简化处理，实际项目中需要计算总数量和总体积
        double totalQuantity = 0.0;
        double totalVolume = 0.0;

        return new WarehouseDTO.InventoryStatistics(totalItems, totalQuantity, 
                totalValue.doubleValue(), totalVolume, lowStockItems, nearExpiryItems);
    }

    /**
     * 转换为DTO
     */
    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        WarehouseDTO dto = new WarehouseDTO();
        BeanUtils.copyProperties(warehouse, dto);
        
        // 转换管理员信息
        if (warehouse.getManagers() != null) {
            List<UserDTO> managers = warehouse.getManagers().stream()
                    .map(this::convertUserToDTO)
                    .collect(Collectors.toList());
            dto.setManagers(managers);
        }
        
        // 获取库存统计信息
        dto.setInventoryStatistics(getWarehouseInventoryStatistics(warehouse.getId()));
        
        return dto;
    }

    /**
     * 转换用户为DTO（简化版）
     */
    private UserDTO convertUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setRole(user.getRole());
        return dto;
    }

    @Override
    public byte[] exportWarehousesToExcel(String keyword, Boolean enabled) {
        try {
            // 获取所有仓库数据（不分页）
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Warehouse> warehousePage;
            if (enabled != null) {
                warehousePage = warehouseRepository.findByEnabledAndKeyword(enabled, keyword, pageable);
            } else {
                warehousePage = warehouseRepository.findByKeyword(keyword, pageable);
            }
            List<Warehouse> warehouses = warehousePage.getContent();

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("仓库数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "序号", "仓库编码", "仓库名称", "仓库地址", "负责人", "联系电话",
                "状态", "创建时间", "更新时间", "备注"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                // 设置标题行样式
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据行
            for (int i = 0; i < warehouses.size(); i++) {
                Warehouse warehouse = warehouses.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(warehouse.getCode() != null ? warehouse.getCode() : "");
                row.createCell(2).setCellValue(warehouse.getName() != null ? warehouse.getName() : "");
                row.createCell(3).setCellValue(warehouse.getAddress() != null ? warehouse.getAddress() : "");
                row.createCell(4).setCellValue(warehouse.getContactPerson() != null ? warehouse.getContactPerson() : "");
                row.createCell(5).setCellValue(warehouse.getContactPhone() != null ? warehouse.getContactPhone() : "");
                row.createCell(6).setCellValue(warehouse.getEnabled() ? "启用" : "禁用");
                row.createCell(7).setCellValue(warehouse.getCreatedTime() != null ? warehouse.getCreatedTime().toString() : "");
                row.createCell(8).setCellValue(warehouse.getUpdatedTime() != null ? warehouse.getUpdatedTime().toString() : "");
                row.createCell(9).setCellValue(warehouse.getRemark() != null ? warehouse.getRemark() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 将工作簿写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }
}
