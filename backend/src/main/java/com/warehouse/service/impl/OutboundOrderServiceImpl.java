package com.warehouse.service.impl;

import com.warehouse.dto.InventoryDTO;
import com.warehouse.dto.OutboundOrderDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.entity.*;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.*;
import com.warehouse.service.OutboundOrderService;
import com.warehouse.service.InventoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
 * 出库单服务实现类
 * 
 * @author Warehouse Team
 */
@Service
@Transactional
public class OutboundOrderServiceImpl implements OutboundOrderService {

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private OutboundOrderDetailRepository outboundOrderDetailRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    // 移除CustomerRepository依赖，改用领取人字段

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public OutboundOrderDTO createOutboundOrder(OutboundOrderDTO.CreateRequest request) {
        // 验证仓库是否存在
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", request.getWarehouseId()));

        // 客户字段保持为null，使用领取人字段
        Customer customer = null;

        // 验证库存是否充足
        for (OutboundOrderDTO.OutboundOrderDetailDTO detailDTO : request.getDetails()) {
            if (!inventoryService.checkStockAvailable(request.getWarehouseId(), detailDTO.getGoodsId(), detailDTO.getQuantity())) {
                Goods goods = goodsRepository.findById(detailDTO.getGoodsId()).orElse(null);
                String goodsName = goods != null ? goods.getName() : "ID:" + detailDTO.getGoodsId();
                throw new BusinessException("货物 " + goodsName + " 库存不足，无法创建出库单");
            }
        }

        // 创建出库单
        OutboundOrder order = new OutboundOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setWarehouse(warehouse);
        order.setCustomer(customer);
        order.setRecipientName(request.getRecipientName());
        order.setBusinessType(request.getBusinessType());
        order.setPlannedDate(request.getPlannedDate());
        order.setReferenceNumber(request.getReferenceNumber());
        order.setRemark(request.getRemark());
        order.setCreatedBy(request.getCreatedBy());
        order.setStatus(ApprovalStatus.PENDING);

        // 计算总金额和总数量
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;
        for (OutboundOrderDTO.OutboundOrderDetailDTO detailDTO : request.getDetails()) {
            if (detailDTO.getAmount() != null) {
                totalAmount = totalAmount.add(detailDTO.getAmount());
            }
            if (detailDTO.getQuantity() != null) {
                totalQuantity = totalQuantity.add(detailDTO.getQuantity());
            }
        }
        order.setTotalAmount(totalAmount);
        order.setTotalQuantity(totalQuantity);

        order = outboundOrderRepository.save(order);

        // 创建出库单明细
        for (OutboundOrderDTO.OutboundOrderDetailDTO detailDTO : request.getDetails()) {
            Goods goods = goodsRepository.findById(detailDTO.getGoodsId())
                    .orElseThrow(() -> new ResourceNotFoundException("货物", "id", detailDTO.getGoodsId()));

            OutboundOrderDetail detail = new OutboundOrderDetail();
            detail.setOutboundOrder(order);
            detail.setGoods(goods);
            detail.setQuantity(detailDTO.getQuantity());
            detail.setUnitPrice(detailDTO.getUnitPrice());
            detail.setAmount(detailDTO.getAmount());
            detail.setBatchNumber(detailDTO.getBatchNumber());
            detail.setLocation(detailDTO.getLocation());
            detail.setRemark(detailDTO.getRemark());

            outboundOrderDetailRepository.save(detail);
        }

        return convertToDTO(order);
    }

    @Override
    @Transactional
    public OutboundOrderDTO createOutboundOrderWithoutStockCheck(OutboundOrderDTO.CreateRequest request) {
        // 验证仓库是否存在
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("仓库", "id", request.getWarehouseId()));

        // 客户字段保持为null，使用领取人字段
        Customer customer = null;

        // 跳过库存检查，直接创建出库单

        // 创建出库单
        OutboundOrder order = new OutboundOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setWarehouse(warehouse);
        order.setCustomer(customer);
        order.setRecipientName(request.getRecipientName());
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

        for (OutboundOrderDTO.OutboundOrderDetailDTO detailDTO : request.getDetails()) {
            BigDecimal amount = detailDTO.getUnitPrice().multiply(detailDTO.getQuantity());
            totalAmount = totalAmount.add(amount);
            totalQuantity = totalQuantity.add(detailDTO.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        order.setTotalQuantity(totalQuantity);

        // 保存出库单
        order = outboundOrderRepository.save(order);

        // 创建出库单明细
        for (OutboundOrderDTO.OutboundOrderDetailDTO detailDTO : request.getDetails()) {
            Goods goods = goodsRepository.findById(detailDTO.getGoodsId())
                    .orElseThrow(() -> new ResourceNotFoundException("货物", "id", detailDTO.getGoodsId()));

            OutboundOrderDetail detail = new OutboundOrderDetail();
            detail.setOutboundOrder(order);
            detail.setGoods(goods);
            detail.setQuantity(detailDTO.getQuantity());
            detail.setUnitPrice(detailDTO.getUnitPrice());
            detail.setAmount(detailDTO.getUnitPrice().multiply(detailDTO.getQuantity()));
            detail.setBatchNumber(detailDTO.getBatchNumber());
            detail.setLocation(detailDTO.getLocation());
            detail.setRemark(detailDTO.getRemark());

            outboundOrderDetailRepository.save(detail);
        }

        return convertToDTO(order);
    }

    @Override
    public OutboundOrderDTO updateOutboundOrder(Long id, OutboundOrderDTO.UpdateRequest request) {
        OutboundOrder order = outboundOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("出库单", "id", id));

        // 只有待审批状态的单据可以修改
        if (order.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("只有待审批状态的出库单可以修改");
        }

        // 客户字段保持为null，使用领取人字段

        // 更新出库单信息
        if (request.getRecipientName() != null) {
            order.setRecipientName(request.getRecipientName());
        }
        if (request.getPlannedDate() != null) {
            order.setPlannedDate(request.getPlannedDate());
        }
        order.setReferenceNumber(request.getReferenceNumber());
        order.setRemark(request.getRemark());

        // 更新明细（简化处理）
        if (request.getDetails() != null) {
            // 验证库存是否充足
            for (OutboundOrderDTO.OutboundOrderDetailDTO detailDTO : request.getDetails()) {
                if (!inventoryService.checkStockAvailable(order.getWarehouse().getId(), detailDTO.getGoodsId(), detailDTO.getQuantity())) {
                    Goods goods = goodsRepository.findById(detailDTO.getGoodsId()).orElse(null);
                    String goodsName = goods != null ? goods.getName() : "ID:" + detailDTO.getGoodsId();
                    throw new BusinessException("货物 " + goodsName + " 库存不足");
                }
            }

            // 删除原有明细
            outboundOrderDetailRepository.deleteByOutboundOrderId(id);
            
            // 重新计算总金额和总数量
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalQuantity = BigDecimal.ZERO;
            for (OutboundOrderDTO.OutboundOrderDetailDTO detailDTO : request.getDetails()) {
                if (detailDTO.getAmount() != null) {
                    totalAmount = totalAmount.add(detailDTO.getAmount());
                }
                if (detailDTO.getQuantity() != null) {
                    totalQuantity = totalQuantity.add(detailDTO.getQuantity());
                }
            }
            order.setTotalAmount(totalAmount);
            order.setTotalQuantity(totalQuantity);

            // 创建新明细
            for (OutboundOrderDTO.OutboundOrderDetailDTO detailDTO : request.getDetails()) {
                Goods goods = goodsRepository.findById(detailDTO.getGoodsId())
                        .orElseThrow(() -> new ResourceNotFoundException("货物", "id", detailDTO.getGoodsId()));

                OutboundOrderDetail detail = new OutboundOrderDetail();
                detail.setOutboundOrder(order);
                detail.setGoods(goods);
                detail.setQuantity(detailDTO.getQuantity());
                detail.setUnitPrice(detailDTO.getUnitPrice());
                detail.setAmount(detailDTO.getAmount());
                detail.setBatchNumber(detailDTO.getBatchNumber());
                detail.setLocation(detailDTO.getLocation());
                detail.setRemark(detailDTO.getRemark());

                outboundOrderDetailRepository.save(detail);
            }
        }

        order = outboundOrderRepository.save(order);
        return convertToDTO(order);
    }

    @Override
    public void deleteOutboundOrder(Long id) {
        OutboundOrder order = outboundOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("出库单", "id", id));

        // 只有待审批状态的单据可以删除
        if (order.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("只有待审批状态的出库单可以删除");
        }

        order.setDeleted(true);
        outboundOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OutboundOrderDTO> findById(Long id) {
        return outboundOrderRepository.findById(id)
                .filter(order -> !order.getDeleted())
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OutboundOrderDTO> findByOrderNumber(String orderNumber) {
        return outboundOrderRepository.findByOrderNumberAndDeletedFalse(orderNumber)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> findAll() {
        return outboundOrderRepository.findAll()
                .stream()
                .filter(order -> !order.getDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> findByWarehouse(Long warehouseId) {
        return outboundOrderRepository.findByWarehouseIdAndDeletedFalseOrderByCreatedTimeDesc(warehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> findByCustomer(Long customerId) {
        return outboundOrderRepository.findByCustomerIdAndDeletedFalseOrderByCreatedTimeDesc(customerId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> findByStatus(ApprovalStatus status) {
        return outboundOrderRepository.findByStatusAndDeletedFalseOrderByCreatedTimeDesc(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> findByBusinessType(BusinessType businessType) {
        return outboundOrderRepository.findByBusinessTypeAndDeletedFalseOrderByCreatedTimeDesc(businessType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> findPendingOrders() {
        return outboundOrderRepository.findPendingOrders()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> findPendingOrdersByWarehouse(Long warehouseId) {
        return outboundOrderRepository.findPendingOrdersByWarehouse(warehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> findApprovedButNotExecuted() {
        return outboundOrderRepository.findApprovedButNotExecuted()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OutboundOrderDTO> findByPage(String keyword, Pageable pageable) {
        Page<OutboundOrder> page = outboundOrderRepository.findByKeyword(keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OutboundOrderDTO> findByPageWithFilters(String keyword, String orderNumber, Long warehouseId,
            BusinessType businessType, ApprovalStatus status, String startDate, String endDate, Pageable pageable) {
        // 获取当前用户并应用权限控制
        Long finalWarehouseId = warehouseId;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails =
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

            // 检查用户角色
            boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
            boolean isTeamLeader = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "TEAM_LEADER".equals(auth.getAuthority()));
            boolean isSquadLeader = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "SQUAD_LEADER".equals(auth.getAuthority()));

            // 管理员、队长、班长可以访问所有仓库的出库单
            if (!isAdmin && !isTeamLeader && !isSquadLeader) {
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

        Page<OutboundOrder> page = outboundOrderRepository.findByFilters(
                keyword, orderNumber, finalWarehouseId, businessType, status, startDate, endDate, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OutboundOrderDTO> findByWarehouseAndPage(Long warehouseId, String keyword, Pageable pageable) {
        Page<OutboundOrder> page = outboundOrderRepository.findByWarehouseAndKeyword(warehouseId, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OutboundOrderDTO> findByStatusAndPage(ApprovalStatus status, String keyword, Pageable pageable) {
        Page<OutboundOrder> page = outboundOrderRepository.findByStatusAndKeyword(status, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return outboundOrderRepository.findByDateRange(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OutboundOrderDTO submitOutboundOrder(Long id) {
        OutboundOrder order = outboundOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("出库单", "id", id));

        // 检查出库单状态
        if (!order.canSubmit()) {
            throw new BusinessException("只有草稿状态的出库单才能提交审核");
        }

        // 检查出库单是否有明细
        if (order.getDetails() == null || order.getDetails().isEmpty()) {
            throw new BusinessException("出库单没有明细，无法提交审核");
        }

        // 验证库存是否充足
        for (OutboundOrderDetail detail : order.getDetails()) {
            if (!inventoryService.checkStockAvailable(order.getWarehouse().getId(), detail.getGoods().getId(), detail.getQuantity())) {
                throw new BusinessException("货物 " + detail.getGoods().getName() + " 库存不足，无法提交审核");
            }
        }

        // 更新状态为待审批
        order.setStatus(ApprovalStatus.PENDING);
        // 注意：OutboundOrder实体中没有submitTime字段，使用updatedTime记录提交时间
        order.setUpdatedTime(LocalDateTime.now());

        order = outboundOrderRepository.save(order);
        return convertToDTO(order);
    }

    @Override
    @Transactional
    public OutboundOrderDTO approveOutboundOrder(Long id, OutboundOrderDTO.ApprovalRequest request) {
        OutboundOrder order = outboundOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("出库单", "id", id));

        // 检查出库单状态
        if (order.getStatus() == ApprovalStatus.APPROVED || order.getStatus() == ApprovalStatus.REJECTED) {
            throw new BusinessException("出库单已经审批完成，无法重复审批");
        }

        // 获取当前用户角色
        String currentUserRole = getCurrentUserRole();

        // 根据当前状态和用户角色确定下一个状态
        ApprovalStatus nextStatus = determineNextApprovalStatus(order.getStatus(), currentUserRole, request.getStatus());

        if (nextStatus == null) {
            throw new BusinessException("当前用户无权限审批此出库单或审批流程错误");
        }

        order.setStatus(nextStatus);
        order.setApprovalTime(LocalDateTime.now());
        order.setApprovalRemark(request.getApprovalRemark());

        // 如果最终审批通过，锁定库存
        if (nextStatus == ApprovalStatus.APPROVED) {
            lockInventoryForOutboundOrder(order);
        }

        order = outboundOrderRepository.save(order);
        return convertToDTO(order);
    }

    /**
     * 获取当前用户角色
     */
    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.equals("SQUAD_LEADER") || role.equals("TEAM_LEADER") ||
                    role.equals("WAREHOUSE_ADMIN") || role.equals("ROLE_ADMIN")) {
                    return role;
                }
            }
        }
        return "ROLE_USER";
    }

    /**
     * 为出库单锁定库存
     */
    private void lockInventoryForOutboundOrder(OutboundOrder order) {
        List<OutboundOrderDetail> details = outboundOrderDetailRepository.findByOutboundOrderId(order.getId());
        for (OutboundOrderDetail detail : details) {
            // 查找对应的库存记录
            Optional<Inventory> inventoryOpt = inventoryRepository.findByWarehouseIdAndGoodsId(
                order.getWarehouse().getId(), detail.getGoods().getId());

            if (inventoryOpt.isPresent()) {
                Inventory inventory = inventoryOpt.get();

                // 创建锁定请求
                InventoryDTO.LockRequest lockRequest = new InventoryDTO.LockRequest();
                lockRequest.setInventoryId(inventory.getId());
                lockRequest.setLockQuantity(detail.getQuantity());
                lockRequest.setReason("出库单审批通过，锁定库存：" + order.getOrderNumber());

                // 锁定库存
                inventoryService.lockInventory(lockRequest);
            }
        }
    }

    /**
     * 根据当前状态和用户角色确定下一个审批状态
     */
    private ApprovalStatus determineNextApprovalStatus(ApprovalStatus currentStatus, String userRole, ApprovalStatus requestedStatus) {
        // 如果是拒绝操作，直接返回拒绝状态
        if (requestedStatus == ApprovalStatus.REJECTED) {
            return ApprovalStatus.REJECTED;
        }

        // 库房管理员和系统管理员可以直接审批通过
        if ("WAREHOUSE_ADMIN".equals(userRole) || "ROLE_ADMIN".equals(userRole)) {
            return ApprovalStatus.APPROVED;
        }

        // 其他角色按照严格审批流程进行
        switch (currentStatus) {
            case PENDING:
                // 待审批状态，只有班长可以审批
                if ("SQUAD_LEADER".equals(userRole)) {
                    return ApprovalStatus.SQUAD_APPROVED;
                }
                break;
            case SQUAD_APPROVED:
                // 班长已审批，只有队长可以审批
                if ("TEAM_LEADER".equals(userRole)) {
                    return ApprovalStatus.TEAM_APPROVED;
                }
                break;
            case TEAM_APPROVED:
                // 队长已审批，只有库房管理员可以审批
                if ("WAREHOUSE_ADMIN".equals(userRole) || "ROLE_ADMIN".equals(userRole)) {
                    return ApprovalStatus.APPROVED;
                }
                break;
            default:
                break;
        }

        return null; // 无权限或流程错误
    }

    @Override
    public OutboundOrderDTO executeOutboundOrder(Long id) {
        OutboundOrder order = outboundOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("出库单", "id", id));

        // 检查状态是否可以执行（必须是APPROVED状态）
        if (order.getStatus() != ApprovalStatus.APPROVED) {
            throw new BusinessException("只有完成所有审批流程的出库单才能执行出库操作，当前状态：" + order.getStatus().getDisplayName());
        }

        // 执行出库操作
        List<OutboundOrderDetail> details = outboundOrderDetailRepository.findByOutboundOrderId(id);
        for (OutboundOrderDetail detail : details) {
            // 查找对应的库存记录
            Optional<Inventory> inventoryOpt = inventoryRepository.findByWarehouseIdAndGoodsId(
                order.getWarehouse().getId(), detail.getGoods().getId());

            if (inventoryOpt.isPresent()) {
                Inventory inventory = inventoryOpt.get();

                // 先解锁库存
                InventoryDTO.UnlockRequest unlockRequest = new InventoryDTO.UnlockRequest();
                unlockRequest.setInventoryId(inventory.getId());
                unlockRequest.setUnlockQuantity(detail.getQuantity());
                unlockRequest.setReason("出库单执行，解锁库存：" + order.getOrderNumber());

                inventoryService.unlockInventory(unlockRequest);

                // 再执行出库
                inventoryService.outboundInventory(
                        order.getWarehouse().getId(),
                        detail.getGoods().getId(),
                        detail.getQuantity()
                );
            } else {
                throw new BusinessException("货物 " + detail.getGoods().getName() + " 库存记录不存在，无法执行出库");
            }
        }

        // 更新出库单状态为已执行
        order.setStatus(ApprovalStatus.EXECUTED);
        order.setOperationTime(LocalDateTime.now());
        order.setActualDate(LocalDate.now());

        order = outboundOrderRepository.save(order);
        return convertToDTO(order);
    }

    @Override
    public void cancelOutboundOrder(Long id, String reason) {
        OutboundOrder order = outboundOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("出库单", "id", id));

        // 已执行的单据不能取消
        if (order.getOperationTime() != null) {
            throw new BusinessException("已执行的出库单不能取消");
        }

        order.setStatus(ApprovalStatus.CANCELLED);
        order.setApprovalRemark(reason);
        order.setApprovalTime(LocalDateTime.now());

        outboundOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrderNumber(String orderNumber) {
        return outboundOrderRepository.existsByOrderNumberAndDeletedFalse(orderNumber);
    }

    @Override
    public String generateOrderNumber() {
        String orderNumber = outboundOrderRepository.generateOrderNumber();
        return orderNumber != null ? orderNumber : "OUT" + System.currentTimeMillis();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> findTodayOrders() {
        return outboundOrderRepository.findTodayOrders()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundOrderDTO> findOverdueOrders() {
        return outboundOrderRepository.findOverdueOrders()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OutboundStatistics getOutboundStatistics() {
        long totalOrders = outboundOrderRepository.countNotDeleted();
        long pendingOrders = outboundOrderRepository.countPendingOrders();
        long approvedOrders = outboundOrderRepository.countByStatusAndNotDeleted(ApprovalStatus.APPROVED);
        long executedOrders = outboundOrderRepository.countExecutedOrders();
        long cancelledOrders = outboundOrderRepository.countByStatusAndNotDeleted(ApprovalStatus.CANCELLED);
        BigDecimal totalAmount = outboundOrderRepository.sumTotalAmount();
        BigDecimal executedAmount = outboundOrderRepository.sumExecutedAmount();
        long overdueOrders = outboundOrderRepository.findOverdueOrders().size();

        return new OutboundStatistics(totalOrders, pendingOrders, approvedOrders, executedOrders,
                                    cancelledOrders, totalAmount != null ? totalAmount : BigDecimal.ZERO,
                                    executedAmount != null ? executedAmount : BigDecimal.ZERO, overdueOrders);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumAmountByCustomerAndDateRange(Long customerId, LocalDate startDate, LocalDate endDate) {
        return outboundOrderRepository.sumAmountByCustomerAndDateRange(customerId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumAmountByWarehouseAndDateRange(Long warehouseId, LocalDate startDate, LocalDate endDate) {
        return outboundOrderRepository.sumAmountByWarehouseAndDateRange(warehouseId, startDate, endDate);
    }

    /**
     * 转换为DTO
     */
    private OutboundOrderDTO convertToDTO(OutboundOrder order) {
        OutboundOrderDTO dto = new OutboundOrderDTO();
        BeanUtils.copyProperties(order, dto);
        
        // 设置仓库信息
        if (order.getWarehouse() != null) {
            dto.setWarehouseId(order.getWarehouse().getId());
            dto.setWarehouseName(order.getWarehouse().getName());
        }
        
        // 设置客户信息
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getId());
            dto.setCustomerName(order.getCustomer().getName());
        }

        // 设置领取人信息
        dto.setRecipientName(order.getRecipientName());
        
        // 设置明细信息
        List<OutboundOrderDetail> details = outboundOrderDetailRepository.findByOutboundOrderId(order.getId());
        List<OutboundOrderDTO.OutboundOrderDetailDTO> detailDTOs = details.stream()
                .map(this::convertDetailToDTO)
                .collect(Collectors.toList());
        dto.setDetails(detailDTOs);

        // 计算总数量
        BigDecimal totalQuantity = details.stream()
                .map(OutboundOrderDetail::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalQuantity(totalQuantity);

        return dto;
    }

    /**
     * 转换明细为DTO
     */
    private OutboundOrderDTO.OutboundOrderDetailDTO convertDetailToDTO(OutboundOrderDetail detail) {
        OutboundOrderDTO.OutboundOrderDetailDTO dto = new OutboundOrderDTO.OutboundOrderDetailDTO();
        BeanUtils.copyProperties(detail, dto);

        if (detail.getGoods() != null) {
            dto.setGoodsId(detail.getGoods().getId());
            dto.setGoodsCode(detail.getGoods().getCode());
            dto.setGoodsName(detail.getGoods().getName());
            dto.setGoodsUnit(detail.getGoods().getUnit());
            dto.setGoodsSpecification(detail.getGoods().getSpecification());
            dto.setGoodsModel(detail.getGoods().getModel());
        }

        return dto;
    }
}
