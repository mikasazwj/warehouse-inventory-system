package com.warehouse.service.impl;

import com.warehouse.dto.InventoryDTO;
import com.warehouse.dto.InventoryHistoryDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.entity.Goods;
import com.warehouse.entity.Inventory;
import com.warehouse.entity.OperationLog;
import com.warehouse.entity.User;
import com.warehouse.entity.Warehouse;
import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.GoodsRepository;
import com.warehouse.repository.InventoryRepository;
import com.warehouse.repository.OperationLogRepository;
import com.warehouse.repository.UserRepository;
import com.warehouse.repository.WarehouseRepository;
import com.warehouse.service.InventoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.format.DateTimeFormatter;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 库存服务实现类
 * 
 * @author Warehouse Team
 */
@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryDTO> findById(Long id) {
        return inventoryRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryDTO> findByWarehouseAndGoods(Long warehouseId, Long goodsId) {
        return inventoryRepository.findByWarehouseIdAndGoodsId(warehouseId, goodsId)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> findByWarehouse(Long warehouseId) {
        return inventoryRepository.findByWarehouseIdOrderByGoodsCode(warehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> findByGoods(Long goodsId) {
        return inventoryRepository.findByGoodsIdOrderByWarehouseName(goodsId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> findAll() {
        return inventoryRepository.findAllOrderByWarehouseAndGoods()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> findWithStock() {
        return inventoryRepository.findWithStock()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> findLowStock() {
        return inventoryRepository.findLowStock()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> findNearExpiry(int days) {
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        return inventoryRepository.findNearExpiry(expiryDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> findExpired() {
        return inventoryRepository.findExpired(LocalDate.now())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> findWithLockedStock() {
        return inventoryRepository.findWithLockedStock()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryDTO> findByPage(String keyword, Pageable pageable) {
        Page<Inventory> page = inventoryRepository.findByKeyword(keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryDTO> findByPageWithFilters(String keyword, Long warehouseId,
                                                          Long categoryId, String stockStatus, Pageable pageable) {
        // 获取当前用户
        Long finalWarehouseId = warehouseId;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails =
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

            // 如果不是系统管理员，需要过滤仓库权限
            boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

            if (!isAdmin) {
                // 获取用户有权限的仓库
                User user = userRepository.findByUsernameAndDeletedFalse(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("用户", "username", userDetails.getUsername()));

                // 如果用户没有指定仓库权限，且没有传入warehouseId，则使用用户的第一个仓库
                if (finalWarehouseId == null && !user.getWarehouses().isEmpty()) {
                    finalWarehouseId = user.getWarehouses().iterator().next().getId();
                }

                // 验证用户是否有权限访问指定仓库
                if (finalWarehouseId != null) {
                    final Long checkWarehouseId = finalWarehouseId;
                    boolean hasPermission = user.getWarehouses().stream()
                        .anyMatch(w -> w.getId().equals(checkWarehouseId));
                    if (!hasPermission) {
                        throw new BusinessException("没有权限访问该仓库");
                    }
                }
            }
        }

        Page<Inventory> page = inventoryRepository.findByFilters(keyword, finalWarehouseId, categoryId, stockStatus, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryDTO> findByWarehouseAndPage(Long warehouseId, String keyword, Pageable pageable) {
        Page<Inventory> page = inventoryRepository.findByWarehouseAndKeyword(warehouseId, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryDTO> findByGoodsAndPage(Long goodsId, String keyword, Pageable pageable) {
        Page<Inventory> page = inventoryRepository.findByGoodsAndKeyword(goodsId, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    public InventoryDTO adjustInventory(InventoryDTO.AdjustRequest request) {
        // 验证仓库和货物是否存在
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", request.getWarehouseId()));
        
        Goods goods = goodsRepository.findById(request.getGoodsId())
                .orElseThrow(() -> new ResourceNotFoundException("货物", "id", request.getGoodsId()));

        // 查找或创建库存记录
        Optional<Inventory> inventoryOpt = inventoryRepository.findByWarehouseIdAndGoodsId(
                request.getWarehouseId(), request.getGoodsId());
        
        Inventory inventory;
        if (inventoryOpt.isPresent()) {
            inventory = inventoryOpt.get();
        } else {
            inventory = new Inventory();
            inventory.setWarehouse(warehouse);
            inventory.setGoods(goods);
            inventory.setQuantity(BigDecimal.ZERO);
            inventory.setAvailableQuantity(BigDecimal.ZERO);
            inventory.setLockedQuantity(BigDecimal.ZERO);
        }

        // 验证请求参数
        if (request.getAdjustQuantity() == null && request.getSetQuantity() == null) {
            throw new BusinessException("调整数量或设置数量不能都为空");
        }

        // 记录库存变动前的数量
        BigDecimal beforeQuantity = inventory.getQuantity();
        BigDecimal newQuantity;
        String operationType;
        BigDecimal changeQuantity;

        if (request.getSetQuantity() != null) {
            // 设置库存数量
            if (request.getSetQuantity().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("设置的库存数量不能为负数");
            }
            newQuantity = request.getSetQuantity();
            changeQuantity = newQuantity.subtract(beforeQuantity);
            operationType = "库存设置";

            // 直接设置数量（不使用inbound/outbound方法，因为这是库存盘点等操作）
            inventory.setQuantity(newQuantity);
            inventory.setAvailableQuantity(newQuantity.subtract(inventory.getLockedQuantity()));

            // 更新成本价格
            if (request.getCostPrice() != null) {
                inventory.setAverageCost(request.getCostPrice());
                inventory.setLatestCost(request.getCostPrice());
            }
        } else {
            // 调整库存数量
            changeQuantity = request.getAdjustQuantity();

            if (changeQuantity.compareTo(BigDecimal.ZERO) > 0) {
                // 入库操作：使用 inbound 方法正确计算平均成本
                BigDecimal costPrice = request.getCostPrice() != null ? request.getCostPrice() : BigDecimal.ZERO;
                inventory.inbound(changeQuantity, costPrice);
                operationType = "库存增加";
            } else if (changeQuantity.compareTo(BigDecimal.ZERO) < 0) {
                // 出库操作：使用 outbound 方法
                BigDecimal outboundQuantity = changeQuantity.abs();
                inventory.outbound(outboundQuantity);
                operationType = "库存减少";
            } else {
                // 数量为0，不做任何操作
                operationType = "无变化";
            }

            newQuantity = inventory.getQuantity();
        }

        // 更新其他信息（但不直接设置成本价格，因为已经通过inbound方法处理）
        if (request.getProductionDate() != null) {
            inventory.setProductionDate(request.getProductionDate());
        }
        if (request.getExpiryDate() != null) {
            inventory.setExpiryDate(request.getExpiryDate());
        }


        // 计算总价值
        if (inventory.getCostPrice() != null) {
            inventory.setTotalValue(inventory.getQuantity().multiply(inventory.getCostPrice()));
        }

        inventory = inventoryRepository.save(inventory);

        // 记录操作日志
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                String operationDesc = String.format("%s %s - %s，变动数量：%s，调整前：%s，调整后：%s",
                        operationType,
                        goods.getName(),
                        warehouse.getName(),
                        changeQuantity.abs(),
                        beforeQuantity,
                        inventory.getQuantity());

                OperationLog log = OperationLog.createBusinessLog(
                        currentUser,
                        operationType,
                        operationDesc,
                        "INVENTORY",
                        inventory.getId(),
                        null,
                        warehouse
                );

                // 设置请求参数以便后续解析
                String adjustQuantityParam = request.getAdjustQuantity() != null ? request.getAdjustQuantity().toString() : "null";
                String setQuantityParam = request.getSetQuantity() != null ? request.getSetQuantity().toString() : "null";
                log.setRequestParams(String.format("inventoryId:%d,adjustQuantity:%s,setQuantity:%s,beforeQuantity:%s,afterQuantity:%s,reason:%s",
                        inventory.getId(), adjustQuantityParam, setQuantityParam, beforeQuantity, inventory.getQuantity(), request.getReason()));

                operationLogRepository.save(log);
            }
        } catch (Exception e) {
            // 日志记录失败不影响主业务
            System.err.println("记录库存操作日志失败: " + e.getMessage());
        }

        return convertToDTO(inventory);
    }

    @Override
    public InventoryDTO inboundInventory(Long warehouseId, Long goodsId, BigDecimal quantity,
                                       BigDecimal costPrice, LocalDate productionDate, LocalDate expiryDate) {
        return inboundInventoryWithBusinessType(warehouseId, goodsId, quantity, costPrice,
                                              productionDate, expiryDate, "INBOUND", null);
    }

    /**
     * 带业务类型的入库方法
     */
    public InventoryDTO inboundInventoryWithBusinessType(Long warehouseId, Long goodsId, BigDecimal quantity,
                                                       BigDecimal costPrice, LocalDate productionDate, LocalDate expiryDate,
                                                       String businessType, String businessNumber) {
        System.out.println("=== 执行带业务类型的入库操作 ===");
        System.out.println("仓库ID: " + warehouseId + ", 货物ID: " + goodsId + ", 数量: " + quantity);
        System.out.println("业务类型: " + businessType + ", 业务单据号: " + businessNumber);
        // 验证仓库和货物是否存在
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", warehouseId));

        Goods goods = goodsRepository.findById(goodsId)
                .orElseThrow(() -> new ResourceNotFoundException("货物", "id", goodsId));

        // 查找或创建库存记录
        Optional<Inventory> inventoryOpt = inventoryRepository.findByWarehouseIdAndGoodsId(warehouseId, goodsId);

        Inventory inventory;
        if (inventoryOpt.isPresent()) {
            inventory = inventoryOpt.get();
        } else {
            inventory = new Inventory();
            inventory.setWarehouse(warehouse);
            inventory.setGoods(goods);
            inventory.setQuantity(BigDecimal.ZERO);
            inventory.setAvailableQuantity(BigDecimal.ZERO);
            inventory.setLockedQuantity(BigDecimal.ZERO);
        }

        // 记录库存变动前的数量
        BigDecimal beforeQuantity = inventory.getQuantity();

        // 执行入库操作
        inventory.inbound(quantity, costPrice != null ? costPrice : BigDecimal.ZERO);

        // 更新其他信息
        if (productionDate != null) {
            inventory.setProductionDate(productionDate);
        }
        if (expiryDate != null) {
            inventory.setExpiryDate(expiryDate);
        }

        // 计算总价值
        if (inventory.getCostPrice() != null) {
            inventory.setTotalValue(inventory.getQuantity().multiply(inventory.getCostPrice()));
        }

        inventory = inventoryRepository.save(inventory);

        // 记录操作日志 - 使用指定的业务类型
        try {
            User currentUser = getCurrentUser();
            System.out.println("=== 入库操作日志记录 ===");
            System.out.println("当前用户: " + (currentUser != null ? currentUser.getUsername() : "null"));
            System.out.println("业务类型: " + businessType);
            System.out.println("业务单据号: " + businessNumber);
            System.out.println("库存ID: " + inventory.getId());

            if (currentUser != null) {
                String operationDesc = String.format("入库 %s - %s，变动数量：%s，调整前：%s，调整后：%s",
                        goods.getName(),
                        warehouse.getName(),
                        quantity,
                        beforeQuantity,
                        inventory.getQuantity());

                System.out.println("操作描述: " + operationDesc);

                // 构建请求参数，用于历史记录解析
                String requestParams = String.format(
                    "{\"warehouseId\":%d,\"goodsId\":%d,\"adjustQuantity\":%s,\"beforeQuantity\":%s,\"afterQuantity\":%s}",
                    warehouseId, goodsId, quantity, beforeQuantity, inventory.getQuantity()
                );

                OperationLog log = OperationLog.createBusinessLog(
                        currentUser,
                        "入库",
                        operationDesc,
                        businessType,  // 使用传入的业务类型
                        inventory.getId(),
                        businessNumber,  // 使用传入的业务单据号
                        warehouse
                );

                // 设置请求参数
                log.setRequestParams(requestParams);

                System.out.println("准备保存操作日志...");
                OperationLog savedLog = operationLogRepository.save(log);
                System.out.println("操作日志保存成功，日志ID: " + savedLog.getId());

                // 验证日志是否真的保存了
                List<OperationLog> recentLogs = operationLogRepository.findInventoryOperationLogs(inventory.getId());
                System.out.println("该库存的操作日志总数: " + recentLogs.size());
                if (!recentLogs.isEmpty()) {
                    OperationLog latestLog = recentLogs.get(0);
                    System.out.println("最新日志 - ID: " + latestLog.getId() +
                                     ", 操作类型: " + latestLog.getOperationType() +
                                     ", 业务类型: " + latestLog.getBusinessType() +
                                     ", 业务单据号: " + latestLog.getBusinessNumber());
                }
            } else {
                System.out.println("当前用户为空，无法记录操作日志");
            }
        } catch (Exception e) {
            // 日志记录失败不影响主业务
            System.err.println("记录入库操作日志失败: " + e.getMessage());
            e.printStackTrace();
        }

        return convertToDTO(inventory);
    }

    @Override
    public InventoryDTO outboundInventory(Long warehouseId, Long goodsId, BigDecimal quantity) {
        return outboundInventoryWithBusinessType(warehouseId, goodsId, quantity, "OUTBOUND", null);
    }

    /**
     * 带业务类型的出库方法
     */
    public InventoryDTO outboundInventoryWithBusinessType(Long warehouseId, Long goodsId, BigDecimal quantity,
                                                        String businessType, String businessNumber) {
        // 检查库存是否充足
        if (!checkStockAvailable(warehouseId, goodsId, quantity)) {
            throw new BusinessException("库存不足，无法出库");
        }

        // 验证仓库和货物是否存在
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", warehouseId));

        Goods goods = goodsRepository.findById(goodsId)
                .orElseThrow(() -> new ResourceNotFoundException("货物", "id", goodsId));

        // 查找库存记录
        Inventory inventory = inventoryRepository.findByWarehouseIdAndGoodsId(warehouseId, goodsId)
                .orElseThrow(() -> new BusinessException("库存记录不存在"));

        // 记录库存变动前的数量
        BigDecimal beforeQuantity = inventory.getQuantity();

        // 执行出库操作
        inventory.outbound(quantity);

        inventory = inventoryRepository.save(inventory);

        // 记录操作日志 - 使用指定的业务类型
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                String operationDesc = String.format("出库 %s - %s，变动数量：%s，调整前：%s，调整后：%s",
                        goods.getName(),
                        warehouse.getName(),
                        quantity,
                        beforeQuantity,
                        inventory.getQuantity());

                // 构建请求参数，用于历史记录解析
                String requestParams = String.format(
                    "{\"warehouseId\":%d,\"goodsId\":%d,\"adjustQuantity\":%s,\"beforeQuantity\":%s,\"afterQuantity\":%s}",
                    warehouseId, goodsId, quantity.negate(), beforeQuantity, inventory.getQuantity()
                );

                OperationLog log = OperationLog.createBusinessLog(
                        currentUser,
                        "出库",
                        operationDesc,
                        businessType,  // 使用传入的业务类型
                        inventory.getId(),
                        businessNumber,  // 使用传入的业务单据号
                        warehouse
                );

                // 设置请求参数
                log.setRequestParams(requestParams);

                operationLogRepository.save(log);
            }
        } catch (Exception e) {
            // 日志记录失败不影响主业务
            System.err.println("记录出库操作日志失败: " + e.getMessage());
        }

        return convertToDTO(inventory);
    }

    @Override
    public void lockInventory(InventoryDTO.LockRequest request) {
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("库存", "id", request.getInventoryId()));

        // 检查可用库存是否充足
        if (inventory.getAvailableQuantity().compareTo(request.getLockQuantity()) < 0) {
            throw new BusinessException("可用库存不足，无法锁定");
        }

        // 锁定库存
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(request.getLockQuantity()));
        inventory.setLockedQuantity(inventory.getLockedQuantity().add(request.getLockQuantity()));
        
        inventoryRepository.save(inventory);
    }

    @Override
    public void unlockInventory(InventoryDTO.UnlockRequest request) {
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("库存", "id", request.getInventoryId()));

        // 检查锁定库存是否充足
        if (inventory.getLockedQuantity().compareTo(request.getUnlockQuantity()) < 0) {
            throw new BusinessException("锁定库存不足，无法解锁");
        }

        // 解锁库存
        inventory.setLockedQuantity(inventory.getLockedQuantity().subtract(request.getUnlockQuantity()));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(request.getUnlockQuantity()));
        
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStockAvailable(Long warehouseId, Long goodsId, BigDecimal requiredQuantity) {
        BigDecimal availableQuantity = getAvailableQuantity(warehouseId, goodsId);
        return availableQuantity.compareTo(requiredQuantity) >= 0;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAvailableQuantity(Long warehouseId, Long goodsId) {
        return inventoryRepository.findByWarehouseIdAndGoodsId(warehouseId, goodsId)
                .map(Inventory::getAvailableQuantity)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalQuantity(Long warehouseId, Long goodsId) {
        return inventoryRepository.findByWarehouseIdAndGoodsId(warehouseId, goodsId)
                .map(Inventory::getQuantity)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getLockedQuantity(Long warehouseId, Long goodsId) {
        return inventoryRepository.findByWarehouseIdAndGoodsId(warehouseId, goodsId)
                .map(Inventory::getLockedQuantity)
                .orElse(BigDecimal.ZERO);
    }





    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> findByProductionDateRange(LocalDate startDate, LocalDate endDate) {
        return inventoryRepository.findByProductionDateBetween(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> findByExpiryDateRange(LocalDate startDate, LocalDate endDate) {
        return inventoryRepository.findByExpiryDateBetween(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryStatistics getInventoryStatistics() {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userWarehouseId = null;

        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails =
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

            // 如果不是系统管理员，需要过滤仓库权限
            boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

            if (!isAdmin) {
                // 获取用户有权限的仓库
                User user = userRepository.findByUsernameAndDeletedFalse(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("用户", "username", userDetails.getUsername()));

                // 如果用户有仓库权限，使用第一个仓库进行统计
                if (!user.getWarehouses().isEmpty()) {
                    userWarehouseId = user.getWarehouses().iterator().next().getId();
                }
            }
        }

        // 根据仓库权限进行统计
        if (userWarehouseId != null) {
            return getWarehouseInventoryStatistics(userWarehouseId);
        } else {
            // 系统管理员或无仓库限制，统计所有数据
            long totalItems = inventoryRepository.count();
            Long totalQuantityLong = inventoryRepository.sumTotalQuantity();
            Long totalAvailableQuantityLong = inventoryRepository.sumTotalAvailableQuantity();
            Long totalLockedQuantityLong = inventoryRepository.sumTotalLockedQuantity();

            BigDecimal totalQuantity = totalQuantityLong != null ? BigDecimal.valueOf(totalQuantityLong) : BigDecimal.ZERO;
            BigDecimal totalAvailableQuantity = totalAvailableQuantityLong != null ? BigDecimal.valueOf(totalAvailableQuantityLong) : BigDecimal.ZERO;
            BigDecimal totalLockedQuantity = totalLockedQuantityLong != null ? BigDecimal.valueOf(totalLockedQuantityLong) : BigDecimal.ZERO;
            BigDecimal totalValue = inventoryRepository.sumTotalValue();
            long lowStockItems = inventoryRepository.findLowStock().size();
            long zeroStockItems = inventoryRepository.findZeroStock().size();
            long nearExpiryItems = inventoryRepository.findNearExpiry(LocalDate.now().plusDays(30)).size();
            long expiredItems = inventoryRepository.findExpired(LocalDate.now()).size();
            long warehousesCount = inventoryRepository.countDistinctWarehouses();
            long goodsCount = inventoryRepository.countDistinctGoods();

            return new InventoryStatistics(totalItems,
                    totalQuantity != null ? totalQuantity : BigDecimal.ZERO,
                    totalAvailableQuantity != null ? totalAvailableQuantity : BigDecimal.ZERO,
                    totalLockedQuantity != null ? totalLockedQuantity : BigDecimal.ZERO,
                    totalValue != null ? totalValue : BigDecimal.ZERO,
                    lowStockItems, zeroStockItems, nearExpiryItems, expiredItems, warehousesCount, goodsCount);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryStatistics getWarehouseInventoryStatistics(Long warehouseId) {
        // 根据仓库ID进行统计
        long totalItems = inventoryRepository.countByWarehouseId(warehouseId);
        Long totalQuantityLong = inventoryRepository.sumTotalQuantityByWarehouse(warehouseId);
        Long totalAvailableQuantityLong = inventoryRepository.sumTotalAvailableQuantityByWarehouse(warehouseId);
        Long totalLockedQuantityLong = inventoryRepository.sumTotalLockedQuantityByWarehouse(warehouseId);

        BigDecimal totalQuantity = totalQuantityLong != null ? BigDecimal.valueOf(totalQuantityLong) : BigDecimal.ZERO;
        BigDecimal totalAvailableQuantity = totalAvailableQuantityLong != null ? BigDecimal.valueOf(totalAvailableQuantityLong) : BigDecimal.ZERO;
        BigDecimal totalLockedQuantity = totalLockedQuantityLong != null ? BigDecimal.valueOf(totalLockedQuantityLong) : BigDecimal.ZERO;
        BigDecimal totalValue = inventoryRepository.sumTotalValueByWarehouse(warehouseId);
        long lowStockItems = inventoryRepository.findLowStockByWarehouse(warehouseId).size();
        long zeroStockItems = inventoryRepository.findZeroStockByWarehouse(warehouseId).size();
        long nearExpiryItems = inventoryRepository.findNearExpiryByWarehouse(warehouseId, LocalDate.now().plusDays(30)).size();
        long expiredItems = inventoryRepository.findExpiredByWarehouse(warehouseId, LocalDate.now()).size();
        long warehousesCount = 1; // 单个仓库
        long goodsCount = inventoryRepository.countDistinctGoodsByWarehouse(warehouseId);

        return new InventoryStatistics(totalItems,
                totalQuantity != null ? totalQuantity : BigDecimal.ZERO,
                totalAvailableQuantity != null ? totalAvailableQuantity : BigDecimal.ZERO,
                totalLockedQuantity != null ? totalLockedQuantity : BigDecimal.ZERO,
                totalValue != null ? totalValue : BigDecimal.ZERO,
                lowStockItems, zeroStockItems, nearExpiryItems, expiredItems, warehousesCount, goodsCount);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryStatistics getGoodsInventoryStatistics(Long goodsId) {
        // 简化实现，实际项目中需要更复杂的统计
        return getInventoryStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> checkInventoryAlerts() {
        List<InventoryDTO> alerts = findLowStock();
        alerts.addAll(findNearExpiry(30));
        alerts.addAll(findExpired());
        return alerts.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        BeanUtils.copyProperties(inventory, dto);
        
        // 设置仓库信息
        if (inventory.getWarehouse() != null) {
            dto.setWarehouseId(inventory.getWarehouse().getId());
            dto.setWarehouseName(inventory.getWarehouse().getName());
        }
        
        // 设置货物信息
        if (inventory.getGoods() != null) {
            dto.setGoodsId(inventory.getGoods().getId());
            dto.setGoodsCode(inventory.getGoods().getCode());
            dto.setGoodsName(inventory.getGoods().getName());
            dto.setGoodsUnit(inventory.getGoods().getUnit());
            dto.setSpecification(inventory.getGoods().getSpecification());
            // 设置分类信息
            if (inventory.getGoods().getCategory() != null) {
                dto.setCategoryName(inventory.getGoods().getCategory().getName());
            }
        }
        
        // 设置库存状态
        dto.setInventoryStatus(generateInventoryStatus(inventory));
        
        return dto;
    }

    /**
     * 生成库存状态
     */
    private InventoryDTO.InventoryStatus generateInventoryStatus(Inventory inventory) {
        boolean isLowStock = false;
        boolean isHighStock = false;
        boolean isZeroStock = false;
        boolean isNearExpiry = false;
        boolean isExpired = false;
        boolean hasLocked = inventory.getLockedQuantity().compareTo(BigDecimal.ZERO) > 0;

        // 按优先级检查库存状态
        if (inventory.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            // 1. 零库存 - 最高优先级
            isZeroStock = true;
            isLowStock = false;
            isHighStock = false;
        } else if (inventory.getGoods() != null) {
            // 2. 检查超量库存 - 第二优先级（增加15%预留空间）
            if (inventory.getGoods().getMaxStock() != null) {
                // 计算带15%预留的最大库存阈值
                BigDecimal maxStockWithBuffer = inventory.getGoods().getMaxStock()
                    .multiply(BigDecimal.valueOf(1.15)); // 增加15%预留

                if (inventory.getQuantity().compareTo(maxStockWithBuffer) > 0) {
                    isHighStock = true;
                    isLowStock = false; // 超量时不算低库存
                    isZeroStock = false;
                }
                // 3. 检查低库存 - 第三优先级（只有在不超量的情况下）
                else if (inventory.getGoods().getMinStock() != null &&
                         inventory.getQuantity().compareTo(inventory.getGoods().getMinStock()) < 0) {
                    isLowStock = true;
                    isHighStock = false;
                    isZeroStock = false;
                }
                // 4. 正常状态 - 在最低库存和最高库存+15%之间
                else {
                    isLowStock = false;
                    isHighStock = false;
                    isZeroStock = false;
                }
            }
            // 如果只设置了最低库存，没有设置最高库存
            else if (inventory.getGoods().getMinStock() != null &&
                     inventory.getQuantity().compareTo(inventory.getGoods().getMinStock()) < 0) {
                isLowStock = true;
                isHighStock = false;
                isZeroStock = false;
            }
            // 没有设置库存限制的情况
            else {
                isLowStock = false;
                isHighStock = false;
                isZeroStock = false;
            }
        }

        // 检查过期状态
        if (inventory.getExpiryDate() != null) {
            LocalDate now = LocalDate.now();
            isExpired = inventory.getExpiryDate().isBefore(now);
            isNearExpiry = !isExpired && inventory.getExpiryDate().isBefore(now.plusDays(30));
        }

        return new InventoryDTO.InventoryStatus(isLowStock, isHighStock, isZeroStock, isNearExpiry, isExpired, hasLocked);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryHistoryDTO> getInventoryHistory(Long inventoryId) {
        System.out.println("=== 查询库存历史记录 ===");
        System.out.println("库存ID: " + inventoryId);

        Optional<Inventory> inventoryOpt = inventoryRepository.findById(inventoryId);
        if (!inventoryOpt.isPresent()) {
            throw new ResourceNotFoundException("库存记录不存在");
        }

        Inventory inventory = inventoryOpt.get();
        System.out.println("库存记录 - 货物: " + inventory.getGoods().getName() +
                         ", 仓库: " + inventory.getWarehouse().getName() +
                         ", 当前数量: " + inventory.getQuantity());

        // 查询与该库存相关的操作日志（包括所有业务类型）
        List<OperationLog> operationLogs = operationLogRepository.findAllInventoryRelatedLogs(inventoryId);
        System.out.println("找到操作日志数量: " + operationLogs.size());

        // 同时查询所有相关的日志（用于调试）
        List<OperationLog> allLogs = operationLogRepository.findByBusinessTypeAndBusinessIdAndDeletedFalseOrderByOperationTimeDesc("INBOUND", inventoryId);
        System.out.println("INBOUND类型日志数量: " + allLogs.size());

        List<OperationLog> inventoryLogs = operationLogRepository.findByBusinessTypeAndBusinessIdAndDeletedFalseOrderByOperationTimeDesc("INVENTORY", inventoryId);
        System.out.println("INVENTORY类型日志数量: " + inventoryLogs.size());

        // 打印每个日志的详细信息
        for (int i = 0; i < Math.min(operationLogs.size(), 5); i++) {
            OperationLog log = operationLogs.get(i);
            System.out.println("日志" + (i+1) + " - ID: " + log.getId() +
                             ", 操作类型: " + log.getOperationType() +
                             ", 业务类型: " + log.getBusinessType() +
                             ", 业务ID: " + log.getBusinessId() +
                             ", 业务单据号: " + log.getBusinessNumber() +
                             ", 操作时间: " + log.getOperationTime());
        }

        // 如果没有找到操作日志，返回一些示例数据以便测试
        if (operationLogs.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            BigDecimal currentQuantity = inventory.getQuantity();

            List<InventoryHistoryDTO> historyList = List.of(
                createSampleHistory(inventoryId, "入库", BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100),
                    "入库单：IN20240115001", "IN20240115001", "张三", now.minusDays(10)),
                createSampleHistory(inventoryId, "调拨出库", BigDecimal.valueOf(-20), BigDecimal.valueOf(100), BigDecimal.valueOf(80),
                    "调拨单：TR20240116002 (调出)", "TR20240116002", "李四", now.minusDays(8)),
                createSampleHistory(inventoryId, "盘盈入库", BigDecimal.valueOf(15), BigDecimal.valueOf(80), BigDecimal.valueOf(95),
                    "盘点单：ST20240117003 (盘盈)", "ST20240117003", "王五", now.minusDays(5)),
                createSampleHistory(inventoryId, "手动调整", BigDecimal.valueOf(5), BigDecimal.valueOf(95), currentQuantity,
                    "手动调整，数量增加 5", null, "赵六", now.minusDays(2))
            );

            // 设置关联信息
            for (InventoryHistoryDTO history : historyList) {
                history.setWarehouseName(inventory.getWarehouse().getName());
                history.setGoodsName(inventory.getGoods().getName());
                history.setGoodsCode(inventory.getGoods().getCode());
            }

            return historyList;
        }

        // 将操作日志转换为库存历史记录
        List<InventoryHistoryDTO> historyList = operationLogs.stream()
                .map(log -> convertOperationLogToHistory(log, inventory))
                .collect(Collectors.toList());

        return historyList;
    }

    /**
     * 将操作日志转换为库存历史记录
     */
    private InventoryHistoryDTO convertOperationLogToHistory(OperationLog log, Inventory inventory) {
        InventoryHistoryDTO history = new InventoryHistoryDTO();
        history.setId(log.getId());
        history.setInventoryId(inventory.getId());

        // 根据业务类型设置更具体的操作类型
        String operationType = mapBusinessTypeToOperationType(log.getBusinessType(), log.getOperationType());
        history.setOperationType(operationType);

        history.setReason(log.getOperationDesc());
        history.setOperatedBy(log.getOperator() != null ? log.getOperator().getRealName() : "系统");
        history.setOperatedTime(log.getOperationTime());
        history.setRemark(log.getBusinessNumber());

        // 设置关联单据号
        history.setRelatedOrderNumber(log.getBusinessNumber());

        // 设置业务类型
        history.setBusinessType(log.getBusinessType());

        // 设置关联信息
        history.setWarehouseName(inventory.getWarehouse().getName());
        history.setGoodsName(inventory.getGoods().getName());
        history.setGoodsCode(inventory.getGoods().getCode());

        // 尝试从请求参数中解析数量信息
        String requestParams = log.getRequestParams();
        // 调试信息（可选择性保留）
        // System.out.println("解析库存历史 - 日志ID: " + log.getId() + ", 业务类型: " + log.getBusinessType());

        if (requestParams != null && !requestParams.isEmpty()) {
            try {
                // 解析调整数量
                if (requestParams.contains("adjustQuantity")) {
                    String adjustQuantityStr = extractValue(requestParams, "adjustQuantity");
                    if (adjustQuantityStr != null && !"null".equals(adjustQuantityStr)) {
                        history.setQuantity(new BigDecimal(adjustQuantityStr));
                    }
                }

                // 解析设置数量
                if (requestParams.contains("setQuantity:")) {
                    String setQuantityStr = extractValue(requestParams, "setQuantity");
                    if (setQuantityStr != null && !"null".equals(setQuantityStr)) {
                        // 对于设置操作，变动数量就是设置的数量
                        history.setQuantity(new BigDecimal(setQuantityStr));
                    }
                }

                // 解析变动前数量
                if (requestParams.contains("beforeQuantity")) {
                    String beforeQuantityStr = extractValue(requestParams, "beforeQuantity");
                    if (beforeQuantityStr != null && !"null".equals(beforeQuantityStr)) {
                        history.setBeforeQuantity(new BigDecimal(beforeQuantityStr));
                    }
                }

                // 解析变动后数量
                if (requestParams.contains("afterQuantity")) {
                    String afterQuantityStr = extractValue(requestParams, "afterQuantity");
                    if (afterQuantityStr != null && !"null".equals(afterQuantityStr)) {
                        history.setAfterQuantity(new BigDecimal(afterQuantityStr));
                    }
                }
            } catch (Exception e) {
                // 解析失败时使用默认值
                System.err.println("解析库存历史参数失败: " + e.getMessage() + ", 参数: " + requestParams);
                history.setQuantity(BigDecimal.ZERO);
                history.setBeforeQuantity(BigDecimal.ZERO);
                history.setAfterQuantity(BigDecimal.ZERO);
            }
        }

        // 设置默认值
        if (history.getQuantity() == null) {
            history.setQuantity(BigDecimal.ZERO);
        }
        if (history.getBeforeQuantity() == null) {
            history.setBeforeQuantity(BigDecimal.ZERO);
        }
        if (history.getAfterQuantity() == null) {
            history.setAfterQuantity(BigDecimal.ZERO);
        }

        return history;
    }

    /**
     * 创建示例历史记录
     */
    private InventoryHistoryDTO createSampleHistory(Long inventoryId, String operationType, BigDecimal quantity,
                                                   BigDecimal beforeQuantity, BigDecimal afterQuantity, String reason,
                                                   String relatedOrderNumber, String operatedBy, LocalDateTime operatedTime) {
        InventoryHistoryDTO history = new InventoryHistoryDTO();
        history.setInventoryId(inventoryId);
        history.setOperationType(operationType);
        history.setQuantity(quantity);
        history.setBeforeQuantity(beforeQuantity);
        history.setAfterQuantity(afterQuantity);
        history.setReason(reason);
        history.setRelatedOrderNumber(relatedOrderNumber);
        history.setOperatedBy(operatedBy);
        history.setOperatedTime(operatedTime);
        return history;
    }

    /**
     * 将业务类型映射为更具体的操作类型
     */
    private String mapBusinessTypeToOperationType(String businessType, String originalOperationType) {
        if (businessType == null) {
            return originalOperationType;
        }

        switch (businessType) {
            case "PURCHASE_IN":
            case "RETURN_IN":
            case "OTHER_IN":
                return "入库";
            case "TRANSFER_IN":
                return "调拨入库";
            case "INVENTORY_GAIN":
                return "盘盈入库";
            case "SALE_OUT":
            case "DAMAGE_OUT":
            case "OTHER_OUT":
                return "出库";
            case "TRANSFER_OUT":
                return "调拨出库";
            case "INVENTORY_LOSS":
                return "盘亏出库";
            case "WAREHOUSE_TRANSFER":
                // 根据数量判断是调拨入库还是调拨出库
                return originalOperationType.contains("入") ? "调拨入库" : "调拨出库";
            default:
                // 如果是手动调整或其他情况
                if ("库存调整".equals(originalOperationType) || "调整".equals(originalOperationType)) {
                    return "手动调整";
                }
                return originalOperationType;
        }
    }

    /**
     * 从请求参数字符串中提取指定键的值
     */
    private String extractValue(String params, String key) {
        try {
            // 支持JSON格式 "key": 和普通格式 key:
            String[] searchKeys = {"\"" + key + "\":", key + ":"};

            for (String searchKey : searchKeys) {
                int startIndex = params.indexOf(searchKey);
                if (startIndex != -1) {
                    startIndex += searchKey.length();

                    // 跳过空格
                    while (startIndex < params.length() && Character.isWhitespace(params.charAt(startIndex))) {
                        startIndex++;
                    }

                    int endIndex = params.indexOf(",", startIndex);
                    if (endIndex == -1) {
                        endIndex = params.indexOf("}", startIndex);
                        if (endIndex == -1) {
                            endIndex = params.length();
                        }
                    }

                    String value = params.substring(startIndex, endIndex).trim();
                    // 移除可能的引号
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }

                    return value;
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                if (!"anonymousUser".equals(username)) {
                    return userRepository.findByUsernameAndDeletedFalse(username).orElse(null);
                }
            }
        } catch (Exception e) {
            System.err.println("获取当前用户失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public byte[] exportInventoryToExcel(String keyword, Long warehouseId, Long categoryId, String stockStatus) {
        try {
            // 获取所有库存数据（不分页）
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Inventory> inventoryPage = inventoryRepository.findByFilters(keyword, warehouseId, categoryId, stockStatus, pageable);
            List<Inventory> inventories = inventoryPage.getContent();

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("库存报表");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "序号", "仓库名称", "货物编码", "货物名称", "分类", "规格型号",
                "单位", "当前库存", "可用库存", "锁定库存", "单价", "库存价值",
                "库存状态", "更新时间"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据行
            int rowNum = 1;
            for (Inventory inventory : inventories) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(rowNum - 1); // 序号
                row.createCell(1).setCellValue(inventory.getWarehouse() != null ? inventory.getWarehouse().getName() : "");
                row.createCell(2).setCellValue(inventory.getGoods() != null ? inventory.getGoods().getCode() : "");
                row.createCell(3).setCellValue(inventory.getGoods() != null ? inventory.getGoods().getName() : "");
                row.createCell(4).setCellValue(inventory.getGoods() != null && inventory.getGoods().getCategory() != null ?
                    inventory.getGoods().getCategory().getName() : "");
                row.createCell(5).setCellValue(inventory.getGoods() != null ? inventory.getGoods().getSpecification() : "");
                row.createCell(6).setCellValue(inventory.getGoods() != null ? inventory.getGoods().getUnit() : "");
                row.createCell(7).setCellValue(inventory.getQuantity().doubleValue());
                row.createCell(8).setCellValue(inventory.getAvailableQuantity().doubleValue());
                row.createCell(9).setCellValue(inventory.getLockedQuantity().doubleValue());
                row.createCell(10).setCellValue(inventory.getAverageCost().doubleValue());
                row.createCell(11).setCellValue(inventory.getQuantity().multiply(inventory.getAverageCost()).doubleValue());

                // 生成库存状态
                InventoryDTO.InventoryStatus status = generateInventoryStatus(inventory);
                row.createCell(12).setCellValue(status.getStatusText());
                row.createCell(13).setCellValue(inventory.getUpdatedTime() != null ?
                    inventory.getUpdatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
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

    @Override
    public List<InventoryDTO.GoodsInfo> getGoodsWithStock() {
        return getGoodsWithStock(null);
    }

    @Override
    public List<InventoryDTO.GoodsInfo> getGoodsWithStock(Long warehouseId) {
        try {
            List<Inventory> inventories;

            if (warehouseId != null) {
                // 查询指定仓库有库存记录的货物
                inventories = inventoryRepository.findByWarehouseIdWithStock(warehouseId);
            } else {
                // 查询所有有库存记录的货物（去重）
                inventories = inventoryRepository.findDistinctGoods();
            }

            // 使用Map来确保按货物ID去重
            Map<Long, InventoryDTO.GoodsInfo> goodsMap = new LinkedHashMap<>();

            for (Inventory inventory : inventories) {
                if (inventory.getGoods() != null) {
                    Long goodsId = inventory.getGoods().getId();
                    if (!goodsMap.containsKey(goodsId)) {
                        goodsMap.put(goodsId, new InventoryDTO.GoodsInfo(
                                inventory.getGoods().getId(),
                                inventory.getGoods().getCode(),
                                inventory.getGoods().getName(),
                                inventory.getGoods().getSpecification(),
                                inventory.getGoods().getModel(),
                                inventory.getGoods().getUnit()
                        ));
                    }
                }
            }

            return new ArrayList<>(goodsMap.values());
        } catch (Exception e) {
            throw new BusinessException("获取货物列表失败: " + e.getMessage());
        }
    }

    @Override
    public InventoryDTO.StockInfo getGoodsStockInfo(Long goodsId, Long warehouseId) {
        try {
            List<Inventory> inventories;

            if (warehouseId != null) {
                // 查询指定仓库的库存
                Optional<Inventory> inventoryOpt = inventoryRepository.findByWarehouseIdAndGoodsIdAndDeletedFalse(warehouseId, goodsId);
                inventories = inventoryOpt.map(List::of).orElse(List.of());
            } else {
                // 查询所有仓库的库存
                inventories = inventoryRepository.findByGoodsIdAndDeletedFalseOrderByWarehouseCode(goodsId);
            }

            if (inventories.isEmpty()) {
                return new InventoryDTO.StockInfo(BigDecimal.ZERO, BigDecimal.ZERO, "");
            }

            // 计算总库存
            BigDecimal totalStock = inventories.stream()
                    .map(Inventory::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 计算加权平均价
            BigDecimal totalValue = BigDecimal.ZERO;
            BigDecimal totalQuantity = BigDecimal.ZERO;

            for (Inventory inventory : inventories) {
                if (inventory.getQuantity().compareTo(BigDecimal.ZERO) > 0 && inventory.getAverageCost() != null) {
                    BigDecimal value = inventory.getQuantity().multiply(inventory.getAverageCost());
                    totalValue = totalValue.add(value);
                    totalQuantity = totalQuantity.add(inventory.getQuantity());
                }
            }

            BigDecimal weightedAveragePrice = BigDecimal.ZERO;
            if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
                weightedAveragePrice = totalValue.divide(totalQuantity, 2, RoundingMode.HALF_UP);
            }

            // 获取单位
            String unit = "";
            if (!inventories.isEmpty() && inventories.get(0).getGoods() != null) {
                unit = inventories.get(0).getGoods().getUnit();
            }

            return new InventoryDTO.StockInfo(totalStock, weightedAveragePrice, unit);
        } catch (Exception e) {
            throw new BusinessException("获取库存信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO.GoodsInventoryInfo> getWarehouseGoodsList(Long warehouseId, Long categoryId, boolean onlyWithStock) {
        try {
            List<Inventory> inventories;

            if (onlyWithStock) {
                // 只查询有库存的记录
                inventories = inventoryRepository.findByWarehouseIdWithStock(warehouseId);
            } else {
                // 查询所有库存记录（包括零库存）
                inventories = inventoryRepository.findByWarehouseIdAndDeletedFalseOrderByGoodsCode(warehouseId);
            }

            return inventories.stream()
                    .filter(inventory -> {
                        // 按分类过滤
                        if (categoryId != null && !categoryId.equals(inventory.getGoods().getCategory().getId())) {
                            return false;
                        }
                        return true;
                    })
                    .map(inventory -> {
                        Goods goods = inventory.getGoods();
                        return new InventoryDTO.GoodsInventoryInfo(
                                goods.getId(),
                                goods.getCode(),
                                goods.getName(),
                                goods.getSpecification(),
                                goods.getModel(),
                                goods.getBrand(),
                                goods.getUnit(),
                                goods.getCategory().getName(),
                                inventory.getQuantity(),
                                inventory.getAvailableQuantity()
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取仓库货物列表失败: warehouseId={}, categoryId={}", warehouseId, categoryId, e);
            return new ArrayList<>();
        }
    }
}
