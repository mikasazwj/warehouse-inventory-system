package com.warehouse.service.impl;

import com.warehouse.dto.PageResponse;
import com.warehouse.dto.StocktakeOrderDTO;
import com.warehouse.entity.*;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.StocktakeType;
import com.warehouse.repository.*;
import com.warehouse.service.StocktakeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 盘点单服务实现类
 *
 * @author Warehouse Team
 */
@Service
@Transactional
public class StocktakeOrderServiceImpl implements StocktakeOrderService {

    @Autowired
    private StocktakeOrderRepository stocktakeOrderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StocktakeOrderDetailRepository stocktakeOrderDetailRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public StocktakeOrderDTO createStocktakeOrder(StocktakeOrderDTO.CreateRequest request) {
        // 查找仓库
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("仓库不存在"));

        // 创建盘点单实体
        StocktakeOrder order = new StocktakeOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setWarehouse(warehouse);
        order.setStocktakeType(request.getStocktakeType());
        order.setStatus(ApprovalStatus.PENDING);
        order.setPlannedDate(request.getPlannedDate());
        order.setDescription(request.getRemark()); // 使用remark字段
        order.setStocktakeUserNames(request.getStocktakeUserNames());
        order.setCreatedTime(LocalDateTime.now());
        order.setCreatedBy(getCurrentUserRealName()); // 获取当前用户真实姓名

        // 保存到数据库
        StocktakeOrder savedOrder = stocktakeOrderRepository.save(order);

        // 创建盘点单时就生成明细数据
        generateStocktakeDetails(savedOrder);

        // 转换为DTO返回
        return convertToDTO(savedOrder);
    }

    @Override
    public StocktakeOrderDTO updateStocktakeOrder(Long id, StocktakeOrderDTO.UpdateRequest request) {
        StocktakeOrder order = stocktakeOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("盘点单不存在"));

        // 更新字段
        if (request.getPlannedDate() != null) {
            order.setPlannedDate(request.getPlannedDate());
        }
        if (request.getRemark() != null) {
            order.setDescription(request.getRemark()); // 使用remark字段
        }
        if (request.getStocktakeUserNames() != null) {
            order.setStocktakeUserNames(request.getStocktakeUserNames());
        }

        order.setUpdatedTime(LocalDateTime.now());
        order.setUpdatedBy("系统用户"); // TODO: 从安全上下文获取当前用户

        StocktakeOrder savedOrder = stocktakeOrderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    @Override
    public void deleteStocktakeOrder(Long id) {
        StocktakeOrder order = stocktakeOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("盘点单不存在"));
        order.setDeleted(true);
        order.setUpdatedTime(LocalDateTime.now());
        stocktakeOrderRepository.save(order);
    }

    @Override
    public Optional<StocktakeOrderDTO> findById(Long id) {
        return stocktakeOrderRepository.findById(id)
                .filter(order -> !order.getDeleted())
                .map(this::convertToDTO);
    }

    @Override
    public Optional<StocktakeOrderDTO> findByOrderNumber(String orderNumber) {
        return stocktakeOrderRepository.findByOrderNumberAndDeletedFalse(orderNumber)
                .map(this::convertToDTO);
    }

    @Override
    public List<StocktakeOrderDTO> findAll() {
        return stocktakeOrderRepository.findByDeletedFalseOrderByCreatedTimeDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StocktakeOrderDTO> findByWarehouse(Long warehouseId) {
        return stocktakeOrderRepository.findByWarehouseIdAndDeletedFalseOrderByCreatedTimeDesc(warehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StocktakeOrderDTO> findByStatus(ApprovalStatus status) {
        return stocktakeOrderRepository.findByStatusAndDeletedFalseOrderByCreatedTimeDesc(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StocktakeOrderDTO> findByStocktakeType(StocktakeType stocktakeType) {
        return stocktakeOrderRepository.findByStocktakeTypeAndDeletedFalseOrderByCreatedTimeDesc(stocktakeType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<StocktakeOrderDTO> findByPage(String keyword, Pageable pageable) {
        Page<StocktakeOrder> page;

        if (keyword != null && !keyword.trim().isEmpty()) {
            page = stocktakeOrderRepository.findByOrderNumberContainingAndDeletedFalse(keyword, pageable);
        } else {
            page = stocktakeOrderRepository.findByDeletedFalse(pageable);
        }

        List<StocktakeOrderDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                                 page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResponse<StocktakeOrderDTO> findByPageWithFilters(String orderNumber, StocktakeType stocktakeType,
                                                               ApprovalStatus status, Long warehouseId,
                                                               LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<StocktakeOrder> page = stocktakeOrderRepository.findByFilters(
                orderNumber, stocktakeType, status, warehouseId, startDate, endDate, pageable);

        List<StocktakeOrderDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                                 page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public List<StocktakeOrderDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return stocktakeOrderRepository.findByDateRangeAndDeletedFalse(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StocktakeOrderDTO approveStocktakeOrder(Long id, StocktakeOrderDTO.ApprovalRequest request) {
        StocktakeOrder order = stocktakeOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("盘点单不存在"));

        // 更新审批信息
        order.setStatus(request.getStatus());
        order.setApprovalRemark(request.getApprovalRemark());
        order.setApprovedTime(LocalDateTime.now());
        order.setApprovedBy("系统用户"); // TODO: 从安全上下文获取当前用户
        order.setUpdatedTime(LocalDateTime.now());
        order.setUpdatedBy("系统用户");

        // 如果审批通过，生成盘点明细数据
        if (request.getStatus() == ApprovalStatus.APPROVED) {
            List<StocktakeOrderDetail> existingDetails = stocktakeOrderDetailRepository.findByStocktakeOrderId(id);
            if (existingDetails.isEmpty()) {
                generateStocktakeDetails(order);
            }
        }

        // 保存到数据库
        StocktakeOrder savedOrder = stocktakeOrderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    @Override
    public StocktakeOrderDTO startStocktake(Long id) {
        StocktakeOrder order = stocktakeOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("盘点单不存在"));

        // 检查状态是否可以开始盘点
        if (order.getStatus() != ApprovalStatus.APPROVED) {
            throw new RuntimeException("只有已审批的盘点单才能开始盘点");
        }

        // 检查是否已有盘点明细，如果没有则生成
        List<StocktakeOrderDetail> existingDetails = stocktakeOrderDetailRepository.findByStocktakeOrderId(id);
        if (existingDetails.isEmpty()) {
            generateStocktakeDetails(order);
        }

        // 更新状态为进行中
        order.setStatus(ApprovalStatus.IN_PROGRESS);
        order.setStartedTime(LocalDateTime.now());
        order.setStartedBy("系统用户"); // TODO: 从安全上下文获取当前用户
        order.setUpdatedTime(LocalDateTime.now());
        order.setUpdatedBy("系统用户");

        // 保存到数据库
        StocktakeOrder savedOrder = stocktakeOrderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    @Override
    public StocktakeOrderDTO completeStocktake(Long id) {
        StocktakeOrder order = stocktakeOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("盘点单不存在"));

        // 检查状态是否可以完成盘点
        if (order.getStatus() != ApprovalStatus.IN_PROGRESS) {
            throw new RuntimeException("只有进行中的盘点单才能完成盘点");
        }

        // 调整库存
        adjustInventoryAfterStocktake(order);

        // 更新状态为已完成
        order.setStatus(ApprovalStatus.EXECUTED);
        order.setActualDate(LocalDate.now());
        order.setCompletedTime(LocalDateTime.now());
        order.setCompletedBy(getCurrentUserRealName());
        order.setUpdatedTime(LocalDateTime.now());
        order.setUpdatedBy(getCurrentUserRealName());

        // 保存到数据库
        StocktakeOrder savedOrder = stocktakeOrderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    @Override
    public void cancelStocktakeOrder(Long id, String reason) {
        StocktakeOrder order = stocktakeOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("盘点单不存在"));

        // 更新状态为已取消
        order.setStatus(ApprovalStatus.CANCELLED);
        order.setApprovalRemark(reason);
        order.setUpdatedTime(LocalDateTime.now());
        order.setUpdatedBy("系统用户");

        // 保存到数据库
        stocktakeOrderRepository.save(order);
    }

    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        return stocktakeOrderRepository.existsByOrderNumberAndDeletedFalse(orderNumber);
    }

    @Override
    public String generateOrderNumber() {
        return "PD" + System.currentTimeMillis();
    }

    @Override
    public List<StocktakeOrderDTO> findTodayOrders() {
        LocalDate today = LocalDate.now();
        return findByDateRange(today, today);
    }

    @Override
    public List<StocktakeOrderDTO> findOverdueOrders() {
        LocalDate today = LocalDate.now();
        List<StocktakeOrder> overdueOrders = stocktakeOrderRepository.findByDeletedFalseOrderByCreatedTimeDesc()
                .stream()
                .filter(order -> order.getPlannedDate() != null &&
                               order.getPlannedDate().isBefore(today) &&
                               (order.getStatus() == ApprovalStatus.PENDING ||
                                order.getStatus() == ApprovalStatus.APPROVED ||
                                order.getStatus() == ApprovalStatus.IN_PROGRESS))
                .collect(Collectors.toList());

        return overdueOrders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StocktakeStatistics getStocktakeStatistics() {
        // 统计各状态的盘点单数量
        List<StocktakeOrder> allOrders = stocktakeOrderRepository.findByDeletedFalseOrderByCreatedTimeDesc();

        long totalCount = allOrders.size();
        long pendingCount = allOrders.stream().filter(o -> o.getStatus() == ApprovalStatus.PENDING).count();
        long inProgressCount = allOrders.stream().filter(o -> o.getStatus() == ApprovalStatus.IN_PROGRESS).count();
        long completedCount = allOrders.stream().filter(o -> o.getStatus() == ApprovalStatus.EXECUTED).count();
        long cancelledCount = allOrders.stream().filter(o -> o.getStatus() == ApprovalStatus.CANCELLED).count();

        // 计算逾期订单数量
        LocalDate today = LocalDate.now();
        long overdueCount = allOrders.stream()
                .filter(order -> order.getPlannedDate() != null &&
                               order.getPlannedDate().isBefore(today) &&
                               (order.getStatus() == ApprovalStatus.PENDING ||
                                order.getStatus() == ApprovalStatus.APPROVED ||
                                order.getStatus() == ApprovalStatus.IN_PROGRESS))
                .count();

        return new StocktakeStatistics(totalCount, pendingCount, inProgressCount,
                                     completedCount, cancelledCount, overdueCount);
    }

    @Override
    public long countStocktakeByWarehouseAndDateRange(Long warehouseId, LocalDate startDate, LocalDate endDate) {
        return 0;
    }

    @Override
    public List<StocktakeOrderDTO.StocktakeOrderDetailDTO> getStocktakeOrderDetails(Long stocktakeOrderId) {
        List<StocktakeOrderDetail> details = stocktakeOrderDetailRepository.findByStocktakeOrderIdOrderByIdAsc(stocktakeOrderId);
        return details.stream()
                .map(this::convertDetailToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StocktakeOrderDTO.StocktakeOrderDetailDTO updateStocktakeOrderDetail(Long stocktakeOrderId, Long detailId, StocktakeOrderDTO.UpdateDetailRequest request) {
        // 查找盘点明细
        StocktakeOrderDetail detail = stocktakeOrderDetailRepository.findById(detailId)
                .orElseThrow(() -> new RuntimeException("盘点明细不存在"));

        // 验证明细是否属于指定的盘点单
        if (!detail.getStocktakeOrder().getId().equals(stocktakeOrderId)) {
            throw new RuntimeException("盘点明细不属于指定的盘点单");
        }

        // 更新实盘数量
        if (request.getActualQuantity() != null) {
            detail.setActualQuantity(request.getActualQuantity());

            // 计算差异数量和金额
            if (detail.getBookQuantity() != null) {
                detail.setDifferenceQuantity(request.getActualQuantity().subtract(detail.getBookQuantity()));

                // 计算实盘金额和差异金额
                if (detail.getUnitPrice() != null) {
                    detail.setActualAmount(request.getActualQuantity().multiply(detail.getUnitPrice()));
                    detail.setDifferenceAmount(detail.getDifferenceQuantity().multiply(detail.getUnitPrice()));
                }
            }
        }

        // 更新备注
        if (request.getRemark() != null) {
            detail.setRemark(request.getRemark());
        }

        // 更新盘点人员
        if (request.getStocktakeUser() != null) {
            detail.setStocktakeUser(request.getStocktakeUser());
        }

        // 更新完成状态
        if (request.getIsCompleted() != null) {
            detail.setIsCompleted(request.getIsCompleted());
        }

        // 更新时间和操作人
        detail.setUpdatedTime(LocalDateTime.now());
        detail.setUpdatedBy("系统用户"); // TODO: 从安全上下文获取当前用户

        // 保存到数据库
        StocktakeOrderDetail savedDetail = stocktakeOrderDetailRepository.save(detail);

        // 更新盘点单的统计信息
        updateStocktakeOrderStatistics(stocktakeOrderId);

        return convertDetailToDTO(savedDetail);
    }

    @Override
    public List<StocktakeOrderDTO.StocktakeOrderDetailDTO> batchUpdateStocktakeOrderDetails(Long stocktakeOrderId, List<StocktakeOrderDTO.UpdateDetailRequest> requests) {
        List<StocktakeOrderDTO.StocktakeOrderDetailDTO> results = new ArrayList<>();

        for (StocktakeOrderDTO.UpdateDetailRequest request : requests) {
            if (request.getId() != null) {
                StocktakeOrderDTO.StocktakeOrderDetailDTO updatedDetail =
                    updateStocktakeOrderDetail(stocktakeOrderId, request.getId(), request);
                results.add(updatedDetail);
            }
        }

        return results;
    }

    @Override
    public StocktakeOrderDTO.StocktakeReport generateStocktakeReport(Long stocktakeOrderId) {
        StocktakeOrder stocktakeOrder = stocktakeOrderRepository.findById(stocktakeOrderId)
                .orElseThrow(() -> new RuntimeException("盘点单不存在"));

        List<StocktakeOrderDetail> details = stocktakeOrderDetailRepository.findByStocktakeOrderId(stocktakeOrderId);

        StocktakeOrderDTO.StocktakeReport report = new StocktakeOrderDTO.StocktakeReport();
        report.setStocktakeOrderId(stocktakeOrderId);
        report.setOrderNumber(stocktakeOrder.getOrderNumber());
        report.setWarehouseName(stocktakeOrder.getWarehouse().getName());
        report.setStocktakeType(stocktakeOrder.getStocktakeType());
        report.setPlannedDate(stocktakeOrder.getPlannedDate());
        report.setActualDate(stocktakeOrder.getActualDate());
        report.setStatus(stocktakeOrder.getStatus());

        // 统计信息
        report.setTotalItems(stocktakeOrder.getTotalItems());
        report.setCompletedItems(stocktakeOrder.getCompletedItems());
        report.setDifferenceItems(stocktakeOrder.getDifferenceItems());
        report.setGainItems(stocktakeOrder.getGainItems());
        report.setLossItems(stocktakeOrder.getLossItems());
        report.setNormalItems(stocktakeOrder.getNormalItems());

        // 计算金额统计
        BigDecimal totalBookAmount = BigDecimal.ZERO;
        BigDecimal totalActualAmount = BigDecimal.ZERO;
        BigDecimal gainAmount = BigDecimal.ZERO;
        BigDecimal lossAmount = BigDecimal.ZERO;

        for (StocktakeOrderDetail detail : details) {
            if (detail.getBookAmount() != null) {
                totalBookAmount = totalBookAmount.add(detail.getBookAmount());
            }
            if (detail.getActualAmount() != null) {
                totalActualAmount = totalActualAmount.add(detail.getActualAmount());
            }
            if (detail.getDifferenceAmount() != null) {
                if (detail.getDifferenceAmount().compareTo(BigDecimal.ZERO) > 0) {
                    gainAmount = gainAmount.add(detail.getDifferenceAmount());
                } else if (detail.getDifferenceAmount().compareTo(BigDecimal.ZERO) < 0) {
                    lossAmount = lossAmount.add(detail.getDifferenceAmount().abs());
                }
            }
        }

        report.setTotalBookAmount(totalBookAmount);
        report.setTotalActualAmount(totalActualAmount);
        report.setTotalDifferenceAmount(totalActualAmount.subtract(totalBookAmount));
        report.setGainAmount(gainAmount);
        report.setLossAmount(lossAmount);

        report.setCreatedBy(stocktakeOrder.getCreatedBy());
        report.setCreatedTime(stocktakeOrder.getCreatedTime());
        report.setCompletedBy(stocktakeOrder.getCompletedBy());
        report.setCompletedTime(stocktakeOrder.getCompletedTime());

        // 分类明细数据
        List<StocktakeOrderDTO.StocktakeOrderDetailDTO> gainDetails = new ArrayList<>();
        List<StocktakeOrderDTO.StocktakeOrderDetailDTO> lossDetails = new ArrayList<>();
        List<StocktakeOrderDTO.StocktakeOrderDetailDTO> normalDetails = new ArrayList<>();

        for (StocktakeOrderDetail detail : details) {
            StocktakeOrderDTO.StocktakeOrderDetailDTO detailDTO = convertDetailToDTO(detail);

            if (detail.getDifferenceQuantity() != null) {
                if (detail.getDifferenceQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    gainDetails.add(detailDTO);
                } else if (detail.getDifferenceQuantity().compareTo(BigDecimal.ZERO) < 0) {
                    lossDetails.add(detailDTO);
                } else {
                    normalDetails.add(detailDTO);
                }
            } else {
                normalDetails.add(detailDTO);
            }
        }

        report.setGainDetails(gainDetails);
        report.setLossDetails(lossDetails);
        report.setNormalDetails(normalDetails);

        return report;
    }

    @Override
    public List<StocktakeOrderDTO.StocktakeOrderDetailDTO> getStocktakeDifferences(Long stocktakeOrderId) {
        List<StocktakeOrderDetail> details = stocktakeOrderDetailRepository.findByStocktakeOrderId(stocktakeOrderId);

        return details.stream()
                .filter(detail -> detail.getDifferenceQuantity() != null &&
                                detail.getDifferenceQuantity().compareTo(BigDecimal.ZERO) != 0)
                .map(this::convertDetailToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将实体转换为DTO
     */
    private StocktakeOrderDTO convertToDTO(StocktakeOrder entity) {
        StocktakeOrderDTO dto = new StocktakeOrderDTO();
        dto.setId(entity.getId());
        dto.setOrderNumber(entity.getOrderNumber());
        dto.setWarehouseId(entity.getWarehouse().getId());
        dto.setWarehouseName(entity.getWarehouse().getName());
        dto.setStocktakeType(entity.getStocktakeType());
        dto.setStatus(entity.getStatus());
        dto.setPlannedDate(entity.getPlannedDate());
        dto.setActualDate(entity.getActualDate());
        dto.setTotalItems(entity.getTotalItems());
        dto.setCompletedItems(entity.getCompletedItems());
        dto.setNormalItems(entity.getNormalItems());
        dto.setDifferenceItems(entity.getDifferenceItems());
        dto.setGainItems(entity.getGainItems());
        dto.setLossItems(entity.getLossItems());
        dto.setRemark(entity.getDescription()); // 实体的description映射到DTO的remark
        dto.setStocktakeUserNames(entity.getStocktakeUserNames());
        dto.setApprovalRemark(entity.getApprovalRemark());
        dto.setApprovalTime(entity.getApprovedTime());
        dto.setApprovedBy(entity.getApprovedBy());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedTime(entity.getUpdatedTime());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    /**
     * 生成盘点明细数据
     */
    private void generateStocktakeDetails(StocktakeOrder stocktakeOrder) {
        // 根据盘点类型生成明细
        List<Inventory> inventories;

        if (stocktakeOrder.getStocktakeType() == StocktakeType.FULL) {
            // 全盘：获取该仓库的所有有库存的记录
            inventories = inventoryRepository.findByWarehouseIdWithStock(
                stocktakeOrder.getWarehouse().getId());
        } else {
            // 其他类型的盘点暂时也使用全盘逻辑
            inventories = inventoryRepository.findByWarehouseIdWithStock(
                stocktakeOrder.getWarehouse().getId());
        }

        List<StocktakeOrderDetail> details = new ArrayList<>();
        for (Inventory inventory : inventories) {
            StocktakeOrderDetail detail = new StocktakeOrderDetail();
            detail.setStocktakeOrder(stocktakeOrder);
            detail.setGoods(inventory.getGoods());
            detail.setGoodsCode(inventory.getGoods().getCode());
            detail.setGoodsName(inventory.getGoods().getName());
            detail.setBookQuantity(inventory.getQuantity());
            detail.setLocation(inventory.getLocation());
            detail.setBatchNumber(inventory.getBatchNumber());
            detail.setUnit(inventory.getGoods().getUnit());
            detail.setUnitPrice(inventory.getAverageCost());
            detail.setBookAmount(inventory.getQuantity().multiply(inventory.getAverageCost()));
            detail.setActualQuantity(null); // 待盘点
            detail.setDifferenceQuantity(BigDecimal.ZERO);
            detail.setActualAmount(BigDecimal.ZERO);
            detail.setDifferenceAmount(BigDecimal.ZERO);
            detail.setIsCompleted(false);
            detail.setIsAdjusted(false);
            detail.setCreatedTime(LocalDateTime.now());
            detail.setCreatedBy("系统用户");
            detail.setUpdatedTime(LocalDateTime.now());
            detail.setUpdatedBy("系统用户");

            details.add(detail);
        }

        // 批量保存明细
        stocktakeOrderDetailRepository.saveAll(details);

        // 更新盘点单的总数量
        stocktakeOrder.setTotalItems(details.size());
        stocktakeOrderRepository.save(stocktakeOrder);
    }

    /**
     * 将盘点明细实体转换为DTO
     */
    private StocktakeOrderDTO.StocktakeOrderDetailDTO convertDetailToDTO(StocktakeOrderDetail detail) {
        StocktakeOrderDTO.StocktakeOrderDetailDTO dto = new StocktakeOrderDTO.StocktakeOrderDetailDTO();
        dto.setId(detail.getId());
        dto.setGoodsId(detail.getGoods().getId());
        dto.setGoodsCode(detail.getGoodsCode());
        dto.setGoodsName(detail.getGoodsName());
        dto.setGoodsSpecification(detail.getGoods().getSpecification());
        dto.setGoodsUnit(detail.getUnit());
        dto.setBookQuantity(detail.getBookQuantity());
        dto.setActualQuantity(detail.getActualQuantity());
        dto.setDifferenceQuantity(detail.getDifferenceQuantity());
        dto.setBatchNumber(detail.getBatchNumber());
        dto.setLocation(detail.getLocation());
        dto.setRemark(detail.getRemark());
        // 注意：StocktakeOrderDetailDTO中没有这些字段，只保留基本字段
        return dto;
    }

    /**
     * 更新盘点单统计信息
     */
    private void updateStocktakeOrderStatistics(Long stocktakeOrderId) {
        StocktakeOrder stocktakeOrder = stocktakeOrderRepository.findById(stocktakeOrderId)
                .orElseThrow(() -> new RuntimeException("盘点单不存在"));

        List<StocktakeOrderDetail> details = stocktakeOrderDetailRepository.findByStocktakeOrderId(stocktakeOrderId);

        int totalItems = details.size();
        int completedItems = 0;
        int differenceItems = 0;
        int gainItems = 0;
        int lossItems = 0;
        int normalItems = 0;

        for (StocktakeOrderDetail detail : details) {
            if (detail.getIsCompleted() != null && detail.getIsCompleted()) {
                completedItems++;
            }

            if (detail.getDifferenceQuantity() != null) {
                if (detail.getDifferenceQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    gainItems++;
                    differenceItems++;
                } else if (detail.getDifferenceQuantity().compareTo(BigDecimal.ZERO) < 0) {
                    lossItems++;
                    differenceItems++;
                } else {
                    normalItems++;
                }
            }
        }

        // 更新统计信息
        stocktakeOrder.setTotalItems(totalItems);
        stocktakeOrder.setCompletedItems(completedItems);
        stocktakeOrder.setDifferenceItems(differenceItems);
        stocktakeOrder.setGainItems(gainItems);
        stocktakeOrder.setLossItems(lossItems);
        stocktakeOrder.setNormalItems(normalItems);
        stocktakeOrder.setUpdatedTime(LocalDateTime.now());
        stocktakeOrder.setUpdatedBy("系统用户");

        stocktakeOrderRepository.save(stocktakeOrder);
    }

    /**
     * 盘点完成后调整库存
     */
    private void adjustInventoryAfterStocktake(StocktakeOrder stocktakeOrder) {
        List<StocktakeOrderDetail> details = stocktakeOrderDetailRepository.findByStocktakeOrderId(stocktakeOrder.getId());

        for (StocktakeOrderDetail detail : details) {
            if (detail.getActualQuantity() != null && detail.getDifferenceQuantity() != null
                && detail.getDifferenceQuantity().compareTo(BigDecimal.ZERO) != 0) {

                // 查找对应的库存记录
                Optional<Inventory> inventoryOpt = inventoryRepository.findByWarehouseIdAndGoodsId(
                    stocktakeOrder.getWarehouse().getId(), detail.getGoods().getId());

                if (inventoryOpt.isPresent()) {
                    Inventory inventory = inventoryOpt.get();

                    // 调整库存数量
                    BigDecimal newQuantity = detail.getActualQuantity();
                    if (newQuantity.compareTo(BigDecimal.ZERO) >= 0) {
                        inventory.setQuantity(newQuantity);
                        inventory.setUpdatedTime(LocalDateTime.now());
                        inventory.setUpdatedBy(getCurrentUserRealName());
                        inventoryRepository.save(inventory);

                        // 标记明细为已调整
                        detail.setIsAdjusted(true);
                        detail.setUpdatedTime(LocalDateTime.now());
                        detail.setUpdatedBy(getCurrentUserRealName());
                        stocktakeOrderDetailRepository.save(detail);
                    }
                }
            }
        }
    }

    /**
     * 获取当前用户真实姓名
     */
    private String getCurrentUserRealName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            try {
                // 根据用户名查找用户信息
                Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
                if (userOpt.isPresent()) {
                    return userOpt.get().getRealName();
                }
            } catch (Exception e) {
                // 如果查找失败，返回用户名
                return authentication.getName();
            }
        }
        return "系统用户";
    }
}