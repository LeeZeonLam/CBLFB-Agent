package com.fba.logi.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fba.logi.domain.order.model.entity.FbaInfo;
import com.fba.logi.domain.order.model.entity.Product;
import com.fba.logi.domain.order.model.entity.ShipmentOrder;
import com.fba.logi.domain.order.repository.IOrderRepository;
import com.fba.logi.infrastructure.persistent.dao.IShipmentOrderMapper;
import com.fba.logi.infrastructure.persistent.po.ShipmentOrderPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发货订单仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ShipmentOrderRepository implements IOrderRepository {

    private final IShipmentOrderMapper orderMapper;

    @Override
    public ShipmentOrder queryOrderById(Long orderId) {
        LambdaQueryWrapper<ShipmentOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShipmentOrderPO::getOrderId, orderId);
        ShipmentOrderPO po = orderMapper.selectOne(wrapper);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public ShipmentOrder queryOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<ShipmentOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShipmentOrderPO::getOrderNo, orderNo);
        ShipmentOrderPO po = orderMapper.selectOne(wrapper);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public ShipmentOrder queryOrderByNo(String orderNo) {
        return queryOrderByOrderNo(orderNo);
    }

    @Override
    public List<ShipmentOrder> queryOrdersByUserId(String userId) {
        LambdaQueryWrapper<ShipmentOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShipmentOrderPO::getUserId, userId);
        wrapper.orderByDesc(ShipmentOrderPO::getCreateTime);
        return orderMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentOrder> queryOrdersByCustomerId(Long customerId) {
        LambdaQueryWrapper<ShipmentOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShipmentOrderPO::getCustomerId, customerId);
        wrapper.orderByDesc(ShipmentOrderPO::getCreateTime);
        return orderMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentOrder> queryOrdersByContainerId(Long containerId) {
        LambdaQueryWrapper<ShipmentOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShipmentOrderPO::getContainerId, containerId);
        wrapper.orderByDesc(ShipmentOrderPO::getCreateTime);
        return orderMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentOrder> queryOrdersByState(String state) {
        LambdaQueryWrapper<ShipmentOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShipmentOrderPO::getState, state);
        wrapper.orderByDesc(ShipmentOrderPO::getCreateTime);
        return orderMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveOrder(ShipmentOrder order) {
        ShipmentOrderPO po = convertToPO(order);
        if (po.getId() == null) {
            if (po.getOrderId() == null) {
                po.setOrderId(System.currentTimeMillis());
            }
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            orderMapper.insert(po);
            order.setOrderId(po.getOrderId());
        } else {
            po.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(po);
        }
    }

    @Override
    public void updateOrderState(Long orderId, String state) {
        orderMapper.updateState(orderId, state);
    }

    @Override
    public void updateState(Long orderId, String state) {
        orderMapper.updateState(orderId, state);
    }

    @Override
    public void deleteOrder(Long orderId) {
        LambdaQueryWrapper<ShipmentOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShipmentOrderPO::getOrderId, orderId);
        orderMapper.delete(wrapper);
    }

    @Override
    public List<Product> queryProductsByOrderId(Long orderId) {
        // 商品存储在单独的表中，这里返回空列表
        // 实际应该调用 ProductMapper
        return new ArrayList<>();
    }

    @Override
    public void saveProducts(Long orderId, List<Product> products) {
        // 商品存储在单独的表中
        // 实际应该调用 ProductMapper
    }

    @Override
    public FbaInfo queryFbaInfoByOrderId(Long orderId) {
        // FBA 信息存储在单独的表中，这里返回 null
        // 实际应该调用 FbaInfoMapper
        return null;
    }

    @Override
    public void saveFbaInfo(FbaInfo fbaInfo) {
        // FBA 信息存储在单独的表中
        // 实际应该调用 FbaInfoMapper
    }

    @Override
    public String generateOrderNo() {
        String no = orderMapper.generateOrderNo();
        if (no == null || no.isEmpty()) {
            return "FBA" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "000001";
        }
        return no;
    }

    @Override
    public void updateContainerInfo(Long orderId, Long containerId, String containerNo) {
        orderMapper.updateContainerInfo(orderId, containerId, containerNo);
    }

    @Override
    public void updateLoadedTime(Long orderId, LocalDateTime loadedTime) {
        orderMapper.updateLoadedTime(orderId, loadedTime);
    }

    @Override
    public void batchUpdateState(List<Long> orderIds, String state) {
        if (orderIds != null && !orderIds.isEmpty()) {
            orderMapper.batchUpdateState(orderIds, state);
        }
    }

    @Override
    public void batchUpdateDepartureTime(List<Long> orderIds, LocalDateTime departureTime) {
        if (orderIds != null && !orderIds.isEmpty()) {
            orderMapper.batchUpdateDepartureTime(orderIds, departureTime);
        }
    }

    @Override
    public void batchUpdateArrivalTime(List<Long> orderIds, LocalDateTime arrivalTime) {
        if (orderIds != null && !orderIds.isEmpty()) {
            orderMapper.batchUpdateArrivalTime(orderIds, arrivalTime);
        }
    }

    @Override
    public void batchUpdatePickedTime(List<Long> orderIds, LocalDateTime pickedTime) {
        if (orderIds != null && !orderIds.isEmpty()) {
            orderMapper.batchUpdatePickedTime(orderIds, pickedTime);
        }
    }

    /**
     * PO 转换为实体
     */
    private ShipmentOrder convertToEntity(ShipmentOrderPO po) {
        return ShipmentOrder.builder()
                .orderId(po.getOrderId())
                .orderNo(po.getOrderNo())
                .userId(po.getUserId())
                .customerId(po.getCustomerId())
                .customerNo(po.getCustomerNo())
                .customerName(po.getCustomerName())
                .customerAddressId(po.getCustomerAddressId())
                .orderType(po.getOrderType())
                .originAddress(po.getOriginAddress())
                .destCountry(po.getDestCountry())
                .fbaWarehouseCode(po.getFbaWarehouseCode())
                .shippingMethod(po.getShippingMethod())
                .channelCode(po.getChannelCode())
                .channelName(po.getChannelName())
                .packagingType(po.getPackagingType())
                .deliveryAddressType(po.getDeliveryAddressType())
                .deliveryAddressDetail(po.getDeliveryAddressDetail())
                .deliveryWarehouseCode(po.getDeliveryWarehouseCode())
                .totalWeight(po.getTotalWeight())
                .totalVolume(po.getTotalVolume())
                .totalPieces(po.getTotalPieces())
                .estimatedCost(po.getEstimatedCost())
                .containerId(po.getContainerId())
                .containerNo(po.getContainerNo())
                .state(po.getState())
                .rejectReason(po.getRejectReason())
                .receivedTime(po.getReceivedTime())
                .dimensionRecordedTime(po.getDimensionRecordedTime())
                .loadedTime(po.getLoadedTime())
                .actualDepartureTime(po.getActualDepartureTime())
                .actualArrivalPortTime(po.getActualArrivalPortTime())
                .pickedTime(po.getPickedTime())
                .deliveryStartTime(po.getDeliveryStartTime())
                .completedTime(po.getCompletedTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    /**
     * 实体转换为 PO
     */
    private ShipmentOrderPO convertToPO(ShipmentOrder entity) {
        Long id = null;
        if (entity.getOrderId() != null) {
            LambdaQueryWrapper<ShipmentOrderPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShipmentOrderPO::getOrderId, entity.getOrderId());
            ShipmentOrderPO existing = orderMapper.selectOne(wrapper);
            if (existing != null) {
                id = existing.getId();
            }
        }

        return ShipmentOrderPO.builder()
                .id(id)
                .orderId(entity.getOrderId())
                .orderNo(entity.getOrderNo())
                .userId(entity.getUserId())
                .customerId(entity.getCustomerId())
                .customerNo(entity.getCustomerNo())
                .customerName(entity.getCustomerName())
                .customerAddressId(entity.getCustomerAddressId())
                .orderType(entity.getOrderType())
                .originAddress(entity.getOriginAddress())
                .destCountry(entity.getDestCountry())
                .fbaWarehouseCode(entity.getFbaWarehouseCode())
                .shippingMethod(entity.getShippingMethod())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .packagingType(entity.getPackagingType())
                .deliveryAddressType(entity.getDeliveryAddressType())
                .deliveryAddressDetail(entity.getDeliveryAddressDetail())
                .deliveryWarehouseCode(entity.getDeliveryWarehouseCode())
                .totalWeight(entity.getTotalWeight())
                .totalVolume(entity.getTotalVolume())
                .totalPieces(entity.getTotalPieces())
                .estimatedCost(entity.getEstimatedCost())
                .containerId(entity.getContainerId())
                .containerNo(entity.getContainerNo())
                .state(entity.getState())
                .rejectReason(entity.getRejectReason())
                .receivedTime(entity.getReceivedTime())
                .dimensionRecordedTime(entity.getDimensionRecordedTime())
                .loadedTime(entity.getLoadedTime())
                .actualDepartureTime(entity.getActualDepartureTime())
                .actualArrivalPortTime(entity.getActualArrivalPortTime())
                .pickedTime(entity.getPickedTime())
                .deliveryStartTime(entity.getDeliveryStartTime())
                .completedTime(entity.getCompletedTime())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
