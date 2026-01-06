package com.fba.logi.domain.order.service.impl;

import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.order.model.entity.FbaInfo;
import com.fba.logi.domain.order.model.entity.Product;
import com.fba.logi.domain.order.model.entity.ShipmentOrder;
import com.fba.logi.domain.order.repository.IOrderRepository;
import com.fba.logi.domain.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;

    /**
     * 运费基准价格（每公斤）
     */
    private static final BigDecimal BASE_RATE_SEA = new BigDecimal("8");    // 海运
    private static final BigDecimal BASE_RATE_AIR = new BigDecimal("35");   // 空运
    private static final BigDecimal BASE_RATE_EXPRESS = new BigDecimal("80"); // 快递

    @Override
    @Transactional
    public Long createOrder(ShipmentOrder order) {
        // 生成订单号
        String orderNo = orderRepository.generateOrderNo();
        order.setOrderNo(orderNo);

        // 设置初始状态
        order.setState(Constants.OrderState.DRAFT);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        // 计算总重量和体积
        calculateTotals(order);

        // 保存订单
        orderRepository.saveOrder(order);

        // 保存商品
        if (order.getProducts() != null && !order.getProducts().isEmpty()) {
            orderRepository.saveProducts(order.getOrderId(), order.getProducts());
        }

        // 保存 FBA 信息
        if (order.getFbaInfo() != null) {
            order.getFbaInfo().setOrderId(order.getOrderId());
            orderRepository.saveFbaInfo(order.getFbaInfo());
        }

        log.info("订单创建成功，订单号: {}, 订单 ID: {}", orderNo, order.getOrderId());
        return order.getOrderId();
    }

    @Override
    public ShipmentOrder getOrderDetail(Long orderId) {
        ShipmentOrder order = orderRepository.queryOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        // 加载商品列表
        List<Product> products = orderRepository.queryProductsByOrderId(orderId);
        order.setProducts(products);

        // 加载 FBA 信息
        FbaInfo fbaInfo = orderRepository.queryFbaInfoByOrderId(orderId);
        order.setFbaInfo(fbaInfo);

        return order;
    }

    @Override
    public List<ShipmentOrder> getUserOrders(String userId) {
        return orderRepository.queryOrdersByUserId(userId);
    }

    @Override
    @Transactional
    public void submitOrder(Long orderId) {
        ShipmentOrder order = orderRepository.queryOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        order.submit();
        orderRepository.updateOrderState(orderId, order.getState());
        log.info("订单提交成功，订单 ID: {}", orderId);
    }

    @Override
    @Transactional
    public void auditOrder(Long orderId, boolean approved, String reason) {
        ShipmentOrder order = orderRepository.queryOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        if (approved) {
            order.approve();
            log.info("订单审核通过，订单 ID: {}", orderId);
        } else {
            order.reject(reason);
            log.info("订单审核拒绝，订单 ID: {}, 原因: {}", orderId, reason);
        }

        orderRepository.saveOrder(order);
    }

    @Override
    @Transactional
    public void addProducts(Long orderId, List<Product> products) {
        ShipmentOrder order = orderRepository.queryOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        // 只能在草稿状态添加商品
        if (!Constants.OrderState.DRAFT.equals(order.getState())) {
            throw new IllegalStateException("只能在草稿状态添加商品");
        }

        orderRepository.saveProducts(orderId, products);

        // 重新计算总重量和体积
        order.setProducts(orderRepository.queryProductsByOrderId(orderId));
        calculateTotals(order);
        orderRepository.saveOrder(order);

        log.info("商品添加成功，订单 ID: {}, 商品数量: {}", orderId, products.size());
    }

    @Override
    @Transactional
    public void updateFbaInfo(Long orderId, FbaInfo fbaInfo) {
        ShipmentOrder order = orderRepository.queryOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        fbaInfo.setOrderId(orderId);
        orderRepository.saveFbaInfo(fbaInfo);

        // 更新订单的 FBA 仓库代码
        order.setFbaWarehouseCode(fbaInfo.getWarehouseCode());
        orderRepository.saveOrder(order);

        log.info("FBA 信息更新成功，订单 ID: {}", orderId);
    }

    @Override
    public BigDecimal estimateShippingCost(Long orderId) {
        ShipmentOrder order = getOrderDetail(orderId);

        // 计算计费重量
        BigDecimal chargeableWeight = order.calculateChargeableWeight();

        // 根据运输方式选择费率
        BigDecimal rate;
        switch (order.getShippingMethod()) {
            case "sea":
                rate = BASE_RATE_SEA;
                break;
            case "air":
                rate = BASE_RATE_AIR;
                break;
            case "express":
                rate = BASE_RATE_EXPRESS;
                break;
            default:
                rate = BASE_RATE_AIR; // 默认空运费率
        }

        // 计算基础运费
        BigDecimal baseCost = chargeableWeight.multiply(rate);

        // 敏感货物加价 30%
        boolean hasSensitive = order.getProducts() != null &&
                order.getProducts().stream().anyMatch(Product::isSensitive);
        if (hasSensitive) {
            baseCost = baseCost.multiply(new BigDecimal("1.3"));
        }

        // 更新订单预估运费
        order.setEstimatedCost(baseCost.setScale(2, RoundingMode.HALF_UP));
        orderRepository.saveOrder(order);

        return order.getEstimatedCost();
    }

    @Override
    public List<ShipmentOrder> getPendingOrders() {
        return orderRepository.queryOrdersByState(Constants.OrderState.PENDING);
    }

    @Override
    @Transactional
    public void markAsShipped(Long orderId) {
        ShipmentOrder order = orderRepository.queryOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        order.ship();
        orderRepository.saveOrder(order);
        log.info("订单已发货，订单 ID: {}", orderId);
    }

    @Override
    @Transactional
    public void markAsDelivered(Long orderId) {
        ShipmentOrder order = orderRepository.queryOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        order.deliver();
        orderRepository.saveOrder(order);
        log.info("订单已送达，订单 ID: {}", orderId);
    }

    // ==================== 头程物流状态转换 ====================

    @Override
    @Transactional
    public void receiveOrder(Long orderId) {
        ShipmentOrder order = orderRepository.queryOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        order.receive();
        order.setReceivedTime(LocalDateTime.now());
        orderRepository.saveOrder(order);
        log.info("订单入仓成功，订单 ID: {}", orderId);
    }

    @Override
    @Transactional
    public void completeDimensionRecord(Long orderId) {
        ShipmentOrder order = orderRepository.queryOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        order.recordDimension();
        order.setDimensionRecordedTime(LocalDateTime.now());
        orderRepository.saveOrder(order);
        log.info("订单材积录入完成，订单 ID: {}", orderId);
    }

    @Override
    @Transactional
    public void startDelivery(Long orderId, String deliveryMethod, String trackingNo) {
        ShipmentOrder order = orderRepository.queryOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        order.startDelivery();
        order.setDeliveryStartTime(LocalDateTime.now());
        // 可以记录派送方式和跟踪单号
        orderRepository.saveOrder(order);
        log.info("订单开始派送，订单 ID: {}, 派送方式: {}, 跟踪号: {}", orderId, deliveryMethod, trackingNo);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, String signedBy) {
        ShipmentOrder order = orderRepository.queryOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        order.complete();
        order.setCompletedTime(LocalDateTime.now());
        orderRepository.saveOrder(order);
        log.info("订单完成，订单 ID: {}, 签收人: {}", orderId, signedBy);
    }

    // ==================== 客户相关 ====================

    @Override
    public List<ShipmentOrder> getCustomerOrders(Long customerId) {
        return orderRepository.queryOrdersByCustomerId(customerId);
    }

    @Override
    public ShipmentOrder getOrderByNo(String orderNo) {
        ShipmentOrder order = orderRepository.queryOrderByNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderNo);
        }

        // 加载商品列表
        List<Product> products = orderRepository.queryProductsByOrderId(order.getOrderId());
        order.setProducts(products);

        // 加载 FBA 信息
        FbaInfo fbaInfo = orderRepository.queryFbaInfoByOrderId(order.getOrderId());
        order.setFbaInfo(fbaInfo);

        return order;
    }

    // ==================== 批量查询 ====================

    @Override
    public List<ShipmentOrder> getOrdersByContainer(Long containerId) {
        return orderRepository.queryOrdersByContainerId(containerId);
    }

    @Override
    public List<ShipmentOrder> getOrdersByState(String state) {
        return orderRepository.queryOrdersByState(state);
    }

    /**
     * 计算订单总重量和体积
     */
    private void calculateTotals(ShipmentOrder order) {
        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            return;
        }

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        int totalPieces = 0;

        for (Product product : order.getProducts()) {
            totalWeight = totalWeight.add(product.calculateTotalWeight());
            totalVolume = totalVolume.add(product.calculateTotalVolume());
            totalPieces += product.getQuantity() != null ? product.getQuantity() : 0;
        }

        order.setTotalWeight(totalWeight);
        order.setTotalVolume(totalVolume);
        order.setTotalPieces(totalPieces);
    }

}
