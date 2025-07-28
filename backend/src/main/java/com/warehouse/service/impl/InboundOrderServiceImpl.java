package com.warehouse.service.impl;

import com.warehouse.dto.InboundOrderDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.entity.*;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.*;
import com.warehouse.service.InboundOrderService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 入库单服务实现类
 * 
 * @author Warehouse Team
 */
@Service
@Transactional
public class InboundOrderServiceImpl implements InboundOrderService {

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private InboundOrderDetailRepository inboundOrderDetailRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public InboundOrderDTO createInboundOrder(InboundOrderDTO.CreateRequest request) {
        // 调试日志
        System.out.println("创建入库单 - 接收到的createdBy: " + request.getCreatedBy());

        // 验证仓库是否存在
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", request.getWarehouseId()));

        // 创建入库单
        InboundOrder order = new InboundOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setWarehouse(warehouse);
        order.setBusinessType(request.getBusinessType());
        order.setPlannedDate(request.getPlannedDate());
        order.setReferenceNumber(request.getReferenceNumber());
        order.setRemark(request.getRemark());
        order.setStatus(ApprovalStatus.PENDING);

        // 设置创建人信息 - 直接存储前端传递的制单人姓名
        if (request.getCreatedBy() != null && !request.getCreatedBy().trim().isEmpty()) {
            order.setCreatedBy(request.getCreatedBy());
        }

        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (InboundOrderDTO.InboundOrderDetailDTO detailDTO : request.getDetails()) {
            if (detailDTO.getAmount() != null) {
                totalAmount = totalAmount.add(detailDTO.getAmount());
            }
        }
        order.setTotalAmount(totalAmount);

        order = inboundOrderRepository.save(order);

        // 创建入库单明细
        for (InboundOrderDTO.InboundOrderDetailDTO detailDTO : request.getDetails()) {
            Goods goods = goodsRepository.findById(detailDTO.getGoodsId())
                    .orElseThrow(() -> new ResourceNotFoundException("货物", "id", detailDTO.getGoodsId()));

            InboundOrderDetail detail = new InboundOrderDetail();
            detail.setInboundOrder(order);
            detail.setGoods(goods);
            detail.setQuantity(detailDTO.getQuantity());
            detail.setUnitPrice(detailDTO.getUnitPrice());
            detail.setAmount(detailDTO.getAmount());
            detail.setProductionDate(detailDTO.getProductionDate());
            detail.setExpiryDate(detailDTO.getExpiryDate());
            detail.setRemark(detailDTO.getRemark());

            inboundOrderDetailRepository.save(detail);
        }

        return convertToDTO(order);
    }

    @Override
    @Transactional
    public InboundOrderDTO createInboundOrderWithoutStockCheck(InboundOrderDTO.CreateRequest request) {
        // 验证仓库是否存在
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", request.getWarehouseId()));

        // 跳过库存检查，直接创建入库单

        // 创建入库单
        InboundOrder order = new InboundOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setWarehouse(warehouse);
        order.setBusinessType(request.getBusinessType());
        order.setPlannedDate(request.getPlannedDate());
        order.setReferenceNumber(request.getReferenceNumber());
        order.setRemark(request.getRemark());
        order.setCreatedBy(request.getCreatedBy());
        order.setStatus(ApprovalStatus.EXECUTED);
        order.setOperationTime(java.time.LocalDateTime.now());
        order.setActualDate(java.time.LocalDate.now());

        // 计算总金额和总数量
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;

        for (InboundOrderDTO.InboundOrderDetailDTO detailDTO : request.getDetails()) {
            BigDecimal amount = detailDTO.getUnitPrice().multiply(detailDTO.getQuantity());
            totalAmount = totalAmount.add(amount);
            totalQuantity = totalQuantity.add(detailDTO.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        order.setTotalQuantity(totalQuantity);

        // 保存入库单
        order = inboundOrderRepository.save(order);

        // 创建入库单明细
        for (InboundOrderDTO.InboundOrderDetailDTO detailDTO : request.getDetails()) {
            Goods goods = goodsRepository.findById(detailDTO.getGoodsId())
                    .orElseThrow(() -> new ResourceNotFoundException("货物", "id", detailDTO.getGoodsId()));

            InboundOrderDetail detail = new InboundOrderDetail();
            detail.setInboundOrder(order);
            detail.setGoods(goods);
            detail.setQuantity(detailDTO.getQuantity());
            detail.setUnitPrice(detailDTO.getUnitPrice());
            detail.setAmount(detailDTO.getUnitPrice().multiply(detailDTO.getQuantity()));
            detail.setProductionDate(detailDTO.getProductionDate());
            detail.setExpiryDate(detailDTO.getExpiryDate());
            detail.setRemark(detailDTO.getRemark());

            inboundOrderDetailRepository.save(detail);
        }

        return convertToDTO(order);
    }

    @Override
    public InboundOrderDTO updateInboundOrder(Long id, InboundOrderDTO.UpdateRequest request) {
        InboundOrder order = inboundOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("入库单", "id", id));

        // 只有待审批状态的单据可以修改
        if (order.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("只有待审批状态的入库单可以修改");
        }

        // 更新入库单信息
        if (request.getPlannedDate() != null) {
            order.setPlannedDate(request.getPlannedDate());
        }
        order.setReferenceNumber(request.getReferenceNumber());
        order.setRemark(request.getRemark());

        // 更新明细（简化处理，实际项目中需要更复杂的逻辑）
        if (request.getDetails() != null) {
            // 删除原有明细
            inboundOrderDetailRepository.deleteByInboundOrderId(id);
            
            // 重新计算总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (InboundOrderDTO.InboundOrderDetailDTO detailDTO : request.getDetails()) {
                if (detailDTO.getAmount() != null) {
                    totalAmount = totalAmount.add(detailDTO.getAmount());
                }
            }
            order.setTotalAmount(totalAmount);

            // 创建新明细
            for (InboundOrderDTO.InboundOrderDetailDTO detailDTO : request.getDetails()) {
                Goods goods = goodsRepository.findById(detailDTO.getGoodsId())
                        .orElseThrow(() -> new ResourceNotFoundException("货物", "id", detailDTO.getGoodsId()));

                InboundOrderDetail detail = new InboundOrderDetail();
                detail.setInboundOrder(order);
                detail.setGoods(goods);
                detail.setQuantity(detailDTO.getQuantity());
                detail.setUnitPrice(detailDTO.getUnitPrice());
                detail.setAmount(detailDTO.getAmount());
                detail.setProductionDate(detailDTO.getProductionDate());
                detail.setExpiryDate(detailDTO.getExpiryDate());
                detail.setRemark(detailDTO.getRemark());

                inboundOrderDetailRepository.save(detail);
            }
        }

        order = inboundOrderRepository.save(order);
        return convertToDTO(order);
    }

    @Override
    public void deleteInboundOrder(Long id) {
        InboundOrder order = inboundOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("入库单", "id", id));

        // 只有待审批状态的单据可以删除
        if (order.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("只有待审批状态的入库单可以删除");
        }

        order.setDeleted(true);
        inboundOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InboundOrderDTO> findById(Long id) {
        return inboundOrderRepository.findById(id)
                .filter(order -> !order.getDeleted())
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InboundOrderDTO> findByOrderNumber(String orderNumber) {
        return inboundOrderRepository.findByOrderNumberAndDeletedFalse(orderNumber)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InboundOrderDTO> findAll() {
        return inboundOrderRepository.findByDeletedFalseOrderByCreatedTimeDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InboundOrderDTO> findByWarehouse(Long warehouseId) {
        return inboundOrderRepository.findByWarehouseIdAndDeletedFalseOrderByCreatedTimeDesc(warehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional(readOnly = true)
    public List<InboundOrderDTO> findByStatus(ApprovalStatus status) {
        return inboundOrderRepository.findByStatusAndDeletedFalseOrderByCreatedTimeDesc(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InboundOrderDTO> findByBusinessType(BusinessType businessType) {
        return inboundOrderRepository.findByBusinessTypeAndDeletedFalseOrderByCreatedTimeDesc(businessType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InboundOrderDTO> findPendingOrders() {
        return inboundOrderRepository.findPendingOrders()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InboundOrderDTO> findPendingOrdersByWarehouse(Long warehouseId) {
        return inboundOrderRepository.findPendingOrdersByWarehouse(warehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InboundOrderDTO> findApprovedButNotExecuted() {
        return inboundOrderRepository.findApprovedButNotExecuted()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InboundOrderDTO> findByPage(String keyword, Pageable pageable) {
        Page<InboundOrder> page = inboundOrderRepository.findByKeyword(keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InboundOrderDTO> findByPageWithFilters(String keyword, Long warehouseId,
            BusinessType businessType, ApprovalStatus status, String startDate, String endDate, Pageable pageable) {
        // 获取当前用户并应用权限控制
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

        Page<InboundOrder> page = inboundOrderRepository.findByFilters(
                keyword, finalWarehouseId, businessType, status, startDate, endDate, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InboundOrderDTO> findByWarehouseAndPage(Long warehouseId, String keyword, Pageable pageable) {
        Page<InboundOrder> page = inboundOrderRepository.findByWarehouseAndKeyword(warehouseId, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InboundOrderDTO> findByStatusAndPage(ApprovalStatus status, String keyword, Pageable pageable) {
        Page<InboundOrder> page = inboundOrderRepository.findByStatusAndKeyword(status, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InboundOrderDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return inboundOrderRepository.findByDateRange(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InboundOrderDTO approveInboundOrder(Long id, InboundOrderDTO.ApprovalRequest request) {
        InboundOrder order = inboundOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("入库单", "id", id));

        // 检查状态是否可以审批
        if (!order.canApprove()) {
            throw new BusinessException("当前状态不允许审批");
        }

        order.setStatus(request.getStatus());
        order.setApprovalTime(LocalDateTime.now());
        order.setApprovalRemark(request.getApprovalRemark());

        order = inboundOrderRepository.save(order);
        return convertToDTO(order);
    }

    @Override
    public InboundOrderDTO executeInboundOrder(Long id) {
        InboundOrder order = inboundOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("入库单", "id", id));

        // 检查状态是否可以执行
        if (!order.canExecute()) {
            throw new BusinessException("当前状态不允许执行入库操作");
        }

        // 执行入库操作
        List<InboundOrderDetail> details = inboundOrderDetailRepository.findByInboundOrderId(id);
        for (InboundOrderDetail detail : details) {
            inventoryService.inboundInventory(
                    order.getWarehouse().getId(),
                    detail.getGoods().getId(),
                    detail.getQuantity(),
                    detail.getUnitPrice(),
                    detail.getProductionDate(),
                    detail.getExpiryDate()
            );
        }

        // 更新入库单状态为已执行
        order.setStatus(ApprovalStatus.EXECUTED);
        order.setOperationTime(LocalDateTime.now());
        order.setActualDate(LocalDate.now());

        order = inboundOrderRepository.save(order);
        return convertToDTO(order);
    }

    @Override
    public void cancelInboundOrder(Long id, String reason) {
        InboundOrder order = inboundOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("入库单", "id", id));

        // 已执行的单据不能取消
        if (order.getOperationTime() != null) {
            throw new BusinessException("已执行的入库单不能取消");
        }

        order.setStatus(ApprovalStatus.CANCELLED);
        order.setApprovalRemark(reason);
        order.setApprovalTime(LocalDateTime.now());

        inboundOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrderNumber(String orderNumber) {
        return inboundOrderRepository.existsByOrderNumberAndDeletedFalse(orderNumber);
    }

    @Override
    public String generateOrderNumber() {
        // 使用Java代码生成订单号：IN + 年月日 + 3位序号
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "IN" + dateStr;

        // 查找今天已有的最大序号
        List<InboundOrder> todayOrders = inboundOrderRepository.findByOrderNumberStartingWithOrderByOrderNumberDesc(prefix);

        int nextSeq = 1;
        if (!todayOrders.isEmpty()) {
            String lastOrderNumber = todayOrders.get(0).getOrderNumber();
            if (lastOrderNumber.length() >= prefix.length() + 3) {
                try {
                    String seqStr = lastOrderNumber.substring(prefix.length());
                    nextSeq = Integer.parseInt(seqStr) + 1;
                } catch (NumberFormatException e) {
                    nextSeq = 1;
                }
            }
        }

        return prefix + String.format("%03d", nextSeq);
    }



    @Override
    @Transactional(readOnly = true)
    public List<InboundOrderDTO> findTodayOrders() {
        return inboundOrderRepository.findTodayOrders()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InboundOrderDTO> findOverdueOrders() {
        return inboundOrderRepository.findOverdueOrders()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InboundStatistics getInboundStatistics() {
        long totalOrders = inboundOrderRepository.countNotDeleted();
        long pendingOrders = inboundOrderRepository.countPendingOrders();
        long approvedOrders = inboundOrderRepository.countByStatusAndNotDeleted(ApprovalStatus.APPROVED);
        long executedOrders = inboundOrderRepository.countExecutedOrders();
        long cancelledOrders = inboundOrderRepository.countByStatusAndNotDeleted(ApprovalStatus.CANCELLED);
        BigDecimal totalAmount = inboundOrderRepository.sumTotalAmount();
        BigDecimal executedAmount = inboundOrderRepository.sumExecutedAmount();
        long overdueOrders = inboundOrderRepository.findOverdueOrders().size();

        return new InboundStatistics(totalOrders, pendingOrders, approvedOrders, executedOrders,
                                   cancelledOrders, totalAmount != null ? totalAmount : BigDecimal.ZERO,
                                   executedAmount != null ? executedAmount : BigDecimal.ZERO, overdueOrders);
    }



    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumAmountByWarehouseAndDateRange(Long warehouseId, LocalDate startDate, LocalDate endDate) {
        return inboundOrderRepository.sumAmountByWarehouseAndDateRange(warehouseId, startDate, endDate);
    }

    /**
     * 转换为DTO
     */
    private InboundOrderDTO convertToDTO(InboundOrder order) {
        InboundOrderDTO dto = new InboundOrderDTO();
        BeanUtils.copyProperties(order, dto);

        // 直接使用数据库中存储的制单人姓名
        // 不需要转换，因为我们现在直接存储用户填写的姓名

        // 设置仓库信息
        if (order.getWarehouse() != null) {
            dto.setWarehouseId(order.getWarehouse().getId());
            dto.setWarehouseName(order.getWarehouse().getName());
        }

        // 设置明细信息
        List<InboundOrderDetail> details = inboundOrderDetailRepository.findByInboundOrderId(order.getId());
        List<InboundOrderDTO.InboundOrderDetailDTO> detailDTOs = details.stream()
                .map(this::convertDetailToDTO)
                .collect(Collectors.toList());
        dto.setDetails(detailDTOs);
        
        return dto;
    }

    /**
     * 转换明细为DTO
     */
    private InboundOrderDTO.InboundOrderDetailDTO convertDetailToDTO(InboundOrderDetail detail) {
        InboundOrderDTO.InboundOrderDetailDTO dto = new InboundOrderDTO.InboundOrderDetailDTO();
        BeanUtils.copyProperties(detail, dto);
        
        if (detail.getGoods() != null) {
            dto.setGoodsId(detail.getGoods().getId());
            dto.setGoodsCode(detail.getGoods().getCode());
            dto.setGoodsName(detail.getGoods().getName());
            dto.setGoodsModel(detail.getGoods().getModel());
            dto.setGoodsSpecification(detail.getGoods().getSpecification());
            dto.setGoodsUnit(detail.getGoods().getUnit());
        }
        
        return dto;
    }
}
