package com.warehouse.service.impl;

import com.warehouse.dto.InventoryDTO;
import com.warehouse.dto.TransferOrderDTO;
import com.warehouse.dto.InboundOrderDTO;
import com.warehouse.dto.OutboundOrderDTO;
import com.warehouse.dto.PageResponse;
import com.warehouse.entity.*;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.*;
import com.warehouse.service.TransferOrderService;
import com.warehouse.service.InventoryService;
import com.warehouse.service.InboundOrderService;
import com.warehouse.service.OutboundOrderService;
import org.springframework.beans.BeanUtils;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 调拨单服务实现类
 * 
 * @author Warehouse Team
 */
@Service
@Transactional
public class TransferOrderServiceImpl implements TransferOrderService {

    @Autowired
    private TransferOrderRepository transferOrderRepository;

    @Autowired
    private TransferOrderDetailRepository transferOrderDetailRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InboundOrderService inboundOrderService;

    @Autowired
    private OutboundOrderService outboundOrderService;

    @Override
    public TransferOrderDTO createTransferOrder(TransferOrderDTO.CreateRequest request) {
        // 验证源仓库和目标仓库
        if (request.getSourceWarehouseId().equals(request.getTargetWarehouseId())) {
            throw new BusinessException("源仓库和目标仓库不能相同");
        }

        Warehouse sourceWarehouse = warehouseRepository.findById(request.getSourceWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("源仓库", "id", request.getSourceWarehouseId()));

        Warehouse targetWarehouse = warehouseRepository.findById(request.getTargetWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("目标仓库", "id", request.getTargetWarehouseId()));

        // 验证源仓库库存是否充足
        for (TransferOrderDTO.TransferOrderDetailDTO detailDTO : request.getDetails()) {
            if (!inventoryService.checkStockAvailable(request.getSourceWarehouseId(), detailDTO.getGoodsId(), detailDTO.getQuantity())) {
                Goods goods = goodsRepository.findById(detailDTO.getGoodsId()).orElse(null);
                String goodsName = goods != null ? goods.getName() : "ID:" + detailDTO.getGoodsId();
                throw new BusinessException("源仓库货物 " + goodsName + " 库存不足，无法创建调拨单");
            }
        }

        // 创建调拨单
        TransferOrder order = new TransferOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setFromWarehouse(sourceWarehouse);
        order.setToWarehouse(targetWarehouse);
        order.setPlannedDate(request.getPlannedDate());
        order.setTransferReason(request.getReason());
        order.setPriority(request.getPriority());
        order.setRemark(request.getRemark());
        order.setStatus(ApprovalStatus.PENDING);

        // 设置创建人（从当前认证用户获取）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            order.setCreatedBy(authentication.getName());
        }

        order = transferOrderRepository.save(order);

        // 创建调拨单明细
        for (TransferOrderDTO.TransferOrderDetailDTO detailDTO : request.getDetails()) {
            Goods goods = goodsRepository.findById(detailDTO.getGoodsId())
                    .orElseThrow(() -> new ResourceNotFoundException("货物", "id", detailDTO.getGoodsId()));

            TransferOrderDetail detail = new TransferOrderDetail();
            detail.setTransferOrder(order);
            detail.setGoods(goods);
            detail.setSpecification(detailDTO.getSpecification());
            detail.setUnitPrice(detailDTO.getUnitPrice());
            detail.setQuantity(detailDTO.getQuantity());
            detail.setRemark(detailDTO.getRemark());

            transferOrderDetailRepository.save(detail);
        }

        // 重新计算总数量
        order.recalculateTotal();
        order = transferOrderRepository.save(order);

        return convertToDTO(order);
    }

    @Override
    public TransferOrderDTO updateTransferOrder(Long id, TransferOrderDTO.UpdateRequest request) {
        TransferOrder order = transferOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("调拨单", "id", id));

        // 只有待审批状态的单据可以修改
        if (order.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("只有待审批状态的调拨单可以修改");
        }

        // 更新调拨单信息
        if (request.getPlannedDate() != null) {
            order.setPlannedDate(request.getPlannedDate());
        }
        order.setTransferReason(request.getReason());
        order.setPriority(request.getPriority());
        order.setRemark(request.getRemark());

        // 更新明细（简化处理）
        if (request.getDetails() != null) {
            // 验证源仓库库存是否充足
            for (TransferOrderDTO.TransferOrderDetailDTO detailDTO : request.getDetails()) {
                if (!inventoryService.checkStockAvailable(order.getFromWarehouse().getId(), detailDTO.getGoodsId(), detailDTO.getQuantity())) {
                    Goods goods = goodsRepository.findById(detailDTO.getGoodsId()).orElse(null);
                    String goodsName = goods != null ? goods.getName() : "ID:" + detailDTO.getGoodsId();
                    throw new BusinessException("源仓库货物 " + goodsName + " 库存不足");
                }
            }

            // 删除原有明细
            transferOrderDetailRepository.deleteByTransferOrderId(id);

            // 创建新明细
            for (TransferOrderDTO.TransferOrderDetailDTO detailDTO : request.getDetails()) {
                Goods goods = goodsRepository.findById(detailDTO.getGoodsId())
                        .orElseThrow(() -> new ResourceNotFoundException("货物", "id", detailDTO.getGoodsId()));

                TransferOrderDetail detail = new TransferOrderDetail();
                detail.setTransferOrder(order);
                detail.setGoods(goods);
                detail.setSpecification(detailDTO.getSpecification());
                detail.setUnitPrice(detailDTO.getUnitPrice());
                detail.setQuantity(detailDTO.getQuantity());
                detail.setRemark(detailDTO.getRemark());

                transferOrderDetailRepository.save(detail);
            }
        }

        // 重新计算总数量
        order.recalculateTotal();
        order = transferOrderRepository.save(order);
        return convertToDTO(order);
    }

    @Override
    public void deleteTransferOrder(Long id) {
        TransferOrder order = transferOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("调拨单", "id", id));

        // 只有待审批状态的单据可以删除
        if (order.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("只有待审批状态的调拨单可以删除");
        }

        order.setDeleted(true);
        transferOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransferOrderDTO> findById(Long id) {
        return transferOrderRepository.findById(id)
                .filter(order -> !order.getDeleted())
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransferOrderDTO> findByOrderNumber(String orderNumber) {
        return transferOrderRepository.findByOrderNumberAndDeletedFalse(orderNumber)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderDTO> findAll() {
        return transferOrderRepository.findByDeletedFalseOrderByCreatedTimeDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderDTO> findBySourceWarehouse(Long sourceWarehouseId) {
        return transferOrderRepository.findByFromWarehouseIdAndDeletedFalseOrderByCreatedTimeDesc(sourceWarehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderDTO> findByTargetWarehouse(Long targetWarehouseId) {
        return transferOrderRepository.findByToWarehouseIdAndDeletedFalseOrderByCreatedTimeDesc(targetWarehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderDTO> findByStatus(ApprovalStatus status) {
        return transferOrderRepository.findByStatusAndDeletedFalseOrderByCreatedTimeDesc(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderDTO> findPendingOrders() {
        return transferOrderRepository.findPendingOrders()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderDTO> findPendingOrdersBySourceWarehouse(Long sourceWarehouseId) {
        return transferOrderRepository.findPendingOrdersBySourceWarehouse(sourceWarehouseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderDTO> findApprovedButNotExecuted() {
        return transferOrderRepository.findApprovedButNotExecuted()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransferOrderDTO> findByPage(String keyword, Pageable pageable) {
        Page<TransferOrder> page = transferOrderRepository.findByKeyword(keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransferOrderDTO> findByPage(String orderNumber, ApprovalStatus status,
                                                   Long sourceWarehouseId, Long targetWarehouseId, Pageable pageable) {
        Page<TransferOrder> page = transferOrderRepository.findByMultipleConditions(
                orderNumber, status, sourceWarehouseId, targetWarehouseId, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransferOrderDTO> findByPage(String orderNumber, ApprovalStatus status,
                                                   Long sourceWarehouseId, Long targetWarehouseId,
                                                   LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<TransferOrder> page = transferOrderRepository.findByMultipleConditionsWithDateRange(
                orderNumber, status, sourceWarehouseId, targetWarehouseId, startDate, endDate, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransferOrderDTO> findBySourceWarehouseAndPage(Long sourceWarehouseId, String keyword, Pageable pageable) {
        Page<TransferOrder> page = transferOrderRepository.findBySourceWarehouseAndKeyword(sourceWarehouseId, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransferOrderDTO> findByStatusAndPage(ApprovalStatus status, String keyword, Pageable pageable) {
        Page<TransferOrder> page = transferOrderRepository.findByStatusAndKeyword(status, keyword, pageable);
        return PageResponse.of(page.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return transferOrderRepository.findByDateRange(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TransferOrderDTO approveTransferOrder(Long id, TransferOrderDTO.ApprovalRequest request) {
        TransferOrder order = transferOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("调拨单", "id", id));

        // 检查状态是否可以审批
        if (!order.canApprove()) {
            throw new BusinessException("当前状态不允许审批");
        }

        order.setStatus(request.getStatus());
        order.setApprovalTime(LocalDateTime.now());
        order.setApprovalComment(request.getApprovalRemark());

        // 如果审批通过，锁定源仓库库存
        if (request.getStatus() == ApprovalStatus.APPROVED) {
            lockInventoryForTransferOrder(order);
        }

        order = transferOrderRepository.save(order);
        return convertToDTO(order);
    }

    @Override
    public TransferOrderDTO executeTransferOrder(Long id) {
        TransferOrder order = transferOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("调拨单", "id", id));

        // 检查状态是否可以执行
        if (!order.canExecute()) {
            throw new BusinessException("当前状态不允许执行调拨操作");
        }

        // 执行调拨操作
        List<TransferOrderDetail> details = transferOrderDetailRepository.findByTransferOrderId(id);
        for (TransferOrderDetail detail : details) {
            // 查找源仓库对应的库存记录
            Optional<Inventory> inventoryOpt = inventoryRepository.findByWarehouseIdAndGoodsId(
                order.getFromWarehouse().getId(), detail.getGoods().getId());

            if (inventoryOpt.isPresent()) {
                Inventory inventory = inventoryOpt.get();

                // 先解锁库存
                InventoryDTO.UnlockRequest unlockRequest = new InventoryDTO.UnlockRequest();
                unlockRequest.setInventoryId(inventory.getId());
                unlockRequest.setUnlockQuantity(detail.getQuantity());
                unlockRequest.setReason("调拨单执行，解锁源仓库库存：" + order.getOrderNumber());

                inventoryService.unlockInventory(unlockRequest);

                // 从源仓库出库
                inventoryService.outboundInventory(
                        order.getFromWarehouse().getId(),
                        detail.getGoods().getId(),
                        detail.getQuantity()
                );

                // 向目标仓库入库
                inventoryService.inboundInventory(
                        order.getToWarehouse().getId(),
                        detail.getGoods().getId(),
                        detail.getQuantity(),
                        detail.getUnitPrice(), // 使用调拨单价
                        null, // 调拨不涉及批次号
                        null, // 调拨不涉及生产日期
                        null, // 调拨不涉及过期日期
                        null  // 调拨不涉及库位
                );
            } else {
                throw new BusinessException("源仓库货物 " + detail.getGoods().getName() + " 库存记录不存在，无法执行调拨");
            }
        }

        // 更新调拨单状态
        order.setStatus(ApprovalStatus.EXECUTED);
        order.setOperationTime(LocalDateTime.now());
        order.setActualDate(LocalDate.now());

        order = transferOrderRepository.save(order);

        // 在库存操作完成后自动生成出入库单
        generateInboundAndOutboundOrders(order);

        return convertToDTO(order);
    }

    @Override
    public void cancelTransferOrder(Long id, String reason) {
        TransferOrder order = transferOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("调拨单", "id", id));

        // 已执行的单据不能取消
        if (order.getOperationTime() != null) {
            throw new BusinessException("已执行的调拨单不能取消");
        }

        order.setStatus(ApprovalStatus.CANCELLED);
        order.setApprovalComment(reason);
        order.setApprovalTime(LocalDateTime.now());

        transferOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrderNumber(String orderNumber) {
        return transferOrderRepository.existsByOrderNumberAndDeletedFalse(orderNumber);
    }

    @Override
    public String generateOrderNumber() {
        String orderNumber = transferOrderRepository.generateOrderNumber();
        return orderNumber != null ? orderNumber : "TR" + System.currentTimeMillis();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderDTO> findTodayOrders() {
        return transferOrderRepository.findTodayOrders()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferOrderDTO> findOverdueOrders() {
        return transferOrderRepository.findOverdueOrders()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransferStatistics getTransferStatistics() {
        long totalOrders = transferOrderRepository.countNotDeleted();
        long pendingOrders = transferOrderRepository.countPendingOrders();
        long approvedOrders = transferOrderRepository.countByStatusAndNotDeleted(ApprovalStatus.APPROVED);
        long executedOrders = transferOrderRepository.countExecutedOrders();
        long cancelledOrders = transferOrderRepository.countByStatusAndNotDeleted(ApprovalStatus.CANCELLED);
        long overdueOrders = transferOrderRepository.findOverdueOrders().size();

        return new TransferStatistics(totalOrders, pendingOrders, approvedOrders, 
                                    executedOrders, cancelledOrders, overdueOrders);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTransferOutByWarehouseAndDateRange(Long warehouseId, LocalDate startDate, LocalDate endDate) {
        return transferOrderRepository.countTransferOutByWarehouseAndDateRange(warehouseId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTransferInByWarehouseAndDateRange(Long warehouseId, LocalDate startDate, LocalDate endDate) {
        return transferOrderRepository.countTransferInByWarehouseAndDateRange(warehouseId, startDate, endDate);
    }

    /**
     * 转换为DTO
     */
    private TransferOrderDTO convertToDTO(TransferOrder order) {
        TransferOrderDTO dto = new TransferOrderDTO();
        BeanUtils.copyProperties(order, dto);
        
        // 设置源仓库信息
        if (order.getFromWarehouse() != null) {
            dto.setSourceWarehouseId(order.getFromWarehouse().getId());
            dto.setSourceWarehouseName(order.getFromWarehouse().getName());
        }

        // 设置目标仓库信息
        if (order.getToWarehouse() != null) {
            dto.setTargetWarehouseId(order.getToWarehouse().getId());
            dto.setTargetWarehouseName(order.getToWarehouse().getName());
        }

        // 设置调拨原因
        dto.setReason(order.getTransferReason());

        // 设置创建人信息
        dto.setCreatedBy(order.getCreatedBy());

        // 设置明细信息
        List<TransferOrderDetail> details = transferOrderDetailRepository.findByTransferOrderId(order.getId());
        List<TransferOrderDTO.TransferOrderDetailDTO> detailDTOs = details.stream()
                .map(this::convertDetailToDTO)
                .collect(Collectors.toList());
        dto.setDetails(detailDTOs);

        // 确保总数量正确计算
        if (details != null && !details.isEmpty()) {
            BigDecimal totalQuantity = details.stream()
                    .map(TransferOrderDetail::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalQuantity(totalQuantity);
        } else {
            dto.setTotalQuantity(BigDecimal.ZERO);
        }

        return dto;
    }

    /**
     * 转换明细为DTO
     */
    private TransferOrderDTO.TransferOrderDetailDTO convertDetailToDTO(TransferOrderDetail detail) {
        TransferOrderDTO.TransferOrderDetailDTO dto = new TransferOrderDTO.TransferOrderDetailDTO();
        BeanUtils.copyProperties(detail, dto);
        
        if (detail.getGoods() != null) {
            dto.setGoodsId(detail.getGoods().getId());
            dto.setGoodsCode(detail.getGoods().getCode());
            dto.setGoodsName(detail.getGoods().getName());
            dto.setGoodsUnit(detail.getGoods().getUnit());
        }
        
        return dto;
    }

    /**
     * 自动生成出入库单
     */
    private void generateInboundAndOutboundOrders(TransferOrder transferOrder) {
        try {
            // 生成出库单（从源仓库）
            generateOutboundOrder(transferOrder);

            // 生成入库单（到目标仓库）
            generateInboundOrder(transferOrder);

        } catch (Exception e) {
            // 记录日志但不影响调拨单执行
            System.err.println("自动生成出入库单失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成出库单
     */
    private void generateOutboundOrder(TransferOrder transferOrder) {
        try {
            OutboundOrderDTO.CreateRequest outboundRequest = new OutboundOrderDTO.CreateRequest();
            outboundRequest.setWarehouseId(transferOrder.getFromWarehouse().getId());
            outboundRequest.setBusinessType(BusinessType.TRANSFER_OUT);
            outboundRequest.setPlannedDate(transferOrder.getActualDate());
            outboundRequest.setRemark("调拨单自动生成 - " + transferOrder.getOrderNumber());
            outboundRequest.setReferenceNumber(transferOrder.getOrderNumber());
            outboundRequest.setCreatedBy("系统自动生成");
            outboundRequest.setRecipientName(transferOrder.getToWarehouse().getName());

            // 转换明细
            List<TransferOrderDetail> details = transferOrderDetailRepository.findByTransferOrderId(transferOrder.getId());
            List<OutboundOrderDTO.OutboundOrderDetailDTO> outboundDetails = details.stream()
                    .map(this::convertToOutboundDetail)
                    .collect(Collectors.toList());
            outboundRequest.setDetails(outboundDetails);

            // 创建出库单（跳过库存检查）
            outboundOrderService.createOutboundOrderWithoutStockCheck(outboundRequest);
        } catch (Exception e) {
            // 记录错误但不影响调拨执行
            System.err.println("生成出库单失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成入库单
     */
    private void generateInboundOrder(TransferOrder transferOrder) {
        try {
            InboundOrderDTO.CreateRequest inboundRequest = new InboundOrderDTO.CreateRequest();
            inboundRequest.setWarehouseId(transferOrder.getToWarehouse().getId());
            inboundRequest.setBusinessType(BusinessType.TRANSFER_IN);
            inboundRequest.setPlannedDate(transferOrder.getActualDate());
            inboundRequest.setRemark("调拨单自动生成 - " + transferOrder.getOrderNumber());
            inboundRequest.setReferenceNumber(transferOrder.getOrderNumber());
            inboundRequest.setCreatedBy("系统自动生成");

            // 转换明细
            List<TransferOrderDetail> details = transferOrderDetailRepository.findByTransferOrderId(transferOrder.getId());
            List<InboundOrderDTO.InboundOrderDetailDTO> inboundDetails = details.stream()
                    .map(this::convertToInboundDetail)
                    .collect(Collectors.toList());
            inboundRequest.setDetails(inboundDetails);

            // 创建入库单（跳过库存检查）
            inboundOrderService.createInboundOrderWithoutStockCheck(inboundRequest);
        } catch (Exception e) {
            // 记录错误但不影响调拨执行
            System.err.println("生成入库单失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 转换为出库单明细
     */
    private OutboundOrderDTO.OutboundOrderDetailDTO convertToOutboundDetail(TransferOrderDetail detail) {
        OutboundOrderDTO.OutboundOrderDetailDTO outboundDetail = new OutboundOrderDTO.OutboundOrderDetailDTO();
        // 不设置ID，让数据库自动生成
        outboundDetail.setGoodsId(detail.getGoods().getId());
        outboundDetail.setQuantity(detail.getQuantity());
        outboundDetail.setUnitPrice(detail.getUnitPrice());
        outboundDetail.setRemark(detail.getRemark());
        return outboundDetail;
    }

    /**
     * 转换为入库单明细
     */
    private InboundOrderDTO.InboundOrderDetailDTO convertToInboundDetail(TransferOrderDetail detail) {
        InboundOrderDTO.InboundOrderDetailDTO inboundDetail = new InboundOrderDTO.InboundOrderDetailDTO();
        // 不设置ID，让数据库自动生成
        inboundDetail.setGoodsId(detail.getGoods().getId());
        inboundDetail.setQuantity(detail.getQuantity());
        inboundDetail.setUnitPrice(detail.getUnitPrice());
        inboundDetail.setRemark(detail.getRemark());
        return inboundDetail;
    }

    /**
     * 为调拨单锁定源仓库库存
     */
    private void lockInventoryForTransferOrder(TransferOrder order) {
        List<TransferOrderDetail> details = transferOrderDetailRepository.findByTransferOrderId(order.getId());
        for (TransferOrderDetail detail : details) {
            // 查找源仓库对应的库存记录
            Optional<Inventory> inventoryOpt = inventoryRepository.findByWarehouseIdAndGoodsId(
                order.getFromWarehouse().getId(), detail.getGoods().getId());

            if (inventoryOpt.isPresent()) {
                Inventory inventory = inventoryOpt.get();

                // 创建锁定请求
                InventoryDTO.LockRequest lockRequest = new InventoryDTO.LockRequest();
                lockRequest.setInventoryId(inventory.getId());
                lockRequest.setLockQuantity(detail.getQuantity());
                lockRequest.setReason("调拨单审批通过，锁定源仓库库存：" + order.getOrderNumber());

                // 锁定库存
                inventoryService.lockInventory(lockRequest);
            }
        }
    }
}
