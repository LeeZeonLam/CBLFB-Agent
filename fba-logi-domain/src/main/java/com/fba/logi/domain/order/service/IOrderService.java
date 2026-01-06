package com.fba.logi.domain.order.service;

import com.fba.logi.domain.order.model.entity.FbaInfo;
import com.fba.logi.domain.order.model.entity.Product;
import com.fba.logi.domain.order.model.entity.ShipmentOrder;

import java.util.List;

/**
 * 订单服务接口
 */
public interface IOrderService {

    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 订单 ID
     */
    Long createOrder(ShipmentOrder order);

    /**
     * 查询订单详情
     *
     * @param orderId 订单 ID
     * @return 订单实体
     */
    ShipmentOrder getOrderDetail(Long orderId);

    /**
     * 查询用户订单列表
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    List<ShipmentOrder> getUserOrders(String userId);

    /**
     * 提交订单（草稿 -> 待审核）
     *
     * @param orderId 订单 ID
     */
    void submitOrder(Long orderId);

    /**
     * 审核订单
     *
     * @param orderId  订单 ID
     * @param approved 是否通过
     * @param reason   拒绝原因（如果拒绝）
     */
    void auditOrder(Long orderId, boolean approved, String reason);

    /**
     * 添加商品到订单
     *
     * @param orderId  订单 ID
     * @param products 商品列表
     */
    void addProducts(Long orderId, List<Product> products);

    /**
     * 更新 FBA 信息
     *
     * @param orderId 订单 ID
     * @param fbaInfo FBA 信息
     */
    void updateFbaInfo(Long orderId, FbaInfo fbaInfo);

    /**
     * 计算运费估算
     *
     * @param orderId 订单 ID
     * @return 估算运费
     */
    java.math.BigDecimal estimateShippingCost(Long orderId);

    /**
     * 获取待审核订单列表
     *
     * @return 待审核订单列表
     */
    List<ShipmentOrder> getPendingOrders();

    /**
     * 标记订单已发货
     *
     * @param orderId 订单 ID
     */
    void markAsShipped(Long orderId);

    /**
     * 标记订单已送达
     *
     * @param orderId 订单 ID
     */
    void markAsDelivered(Long orderId);

    // ==================== 头程物流状态转换 ====================

    /**
     * 确认货物入仓
     *
     * @param orderId 订单 ID
     */
    void receiveOrder(Long orderId);

    /**
     * 完成材积录入
     *
     * @param orderId 订单 ID
     */
    void completeDimensionRecord(Long orderId);

    /**
     * 开始派送
     *
     * @param orderId        订单 ID
     * @param deliveryMethod 派送方式
     * @param trackingNo     跟踪单号
     */
    void startDelivery(Long orderId, String deliveryMethod, String trackingNo);

    /**
     * 完成订单
     *
     * @param orderId  订单 ID
     * @param signedBy 签收人
     */
    void completeOrder(Long orderId, String signedBy);

    // ==================== 客户相关 ====================

    /**
     * 查询客户订单列表
     *
     * @param customerId 客户 ID
     * @return 订单列表
     */
    List<ShipmentOrder> getCustomerOrders(Long customerId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单实体
     */
    ShipmentOrder getOrderByNo(String orderNo);

    // ==================== 批量查询 ====================

    /**
     * 查询指定柜子的订单
     *
     * @param containerId 柜子 ID
     * @return 订单列表
     */
    List<ShipmentOrder> getOrdersByContainer(Long containerId);

    /**
     * 查询指定状态的订单
     *
     * @param state 订单状态
     * @return 订单列表
     */
    List<ShipmentOrder> getOrdersByState(String state);

}
