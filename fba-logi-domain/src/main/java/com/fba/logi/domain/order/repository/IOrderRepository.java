package com.fba.logi.domain.order.repository;

import com.fba.logi.domain.order.model.entity.FbaInfo;
import com.fba.logi.domain.order.model.entity.Product;
import com.fba.logi.domain.order.model.entity.ShipmentOrder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单仓储接口
 */
public interface IOrderRepository {

    /**
     * 根据订单 ID 查询订单
     *
     * @param orderId 订单 ID
     * @return 订单实体
     */
    ShipmentOrder queryOrderById(Long orderId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单实体
     */
    ShipmentOrder queryOrderByOrderNo(String orderNo);

    /**
     * 根据用户 ID 查询订单列表
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    List<ShipmentOrder> queryOrdersByUserId(String userId);

    /**
     * 根据状态查询订单列表
     *
     * @param state 订单状态
     * @return 订单列表
     */
    List<ShipmentOrder> queryOrdersByState(String state);

    /**
     * 保存订单
     *
     * @param order 订单实体
     */
    void saveOrder(ShipmentOrder order);

    /**
     * 更新订单状态
     *
     * @param orderId 订单 ID
     * @param state   新状态
     */
    void updateOrderState(Long orderId, String state);

    /**
     * 删除订单
     *
     * @param orderId 订单 ID
     */
    void deleteOrder(Long orderId);

    /**
     * 查询订单商品列表
     *
     * @param orderId 订单 ID
     * @return 商品列表
     */
    List<Product> queryProductsByOrderId(Long orderId);

    /**
     * 保存订单商品
     *
     * @param orderId  订单 ID
     * @param products 商品列表
     */
    void saveProducts(Long orderId, List<Product> products);

    /**
     * 查询 FBA 信息
     *
     * @param orderId 订单 ID
     * @return FBA 信息
     */
    FbaInfo queryFbaInfoByOrderId(Long orderId);

    /**
     * 保存 FBA 信息
     *
     * @param fbaInfo FBA 信息
     */
    void saveFbaInfo(FbaInfo fbaInfo);

    /**
     * 生成订单号
     *
     * @return 订单号
     */
    String generateOrderNo();

    // ==================== 头程物流扩展方法 ====================

    /**
     * 根据订单号查询订单
     */
    ShipmentOrder queryOrderByNo(String orderNo);

    /**
     * 根据客户 ID 查询订单
     */
    List<ShipmentOrder> queryOrdersByCustomerId(Long customerId);

    /**
     * 根据柜子 ID 查询订单
     */
    List<ShipmentOrder> queryOrdersByContainerId(Long containerId);

    /**
     * 更新订单状态
     */
    void updateState(Long orderId, String state);

    /**
     * 更新订单柜子信息
     */
    void updateContainerInfo(Long orderId, Long containerId, String containerNo);

    /**
     * 更新装柜时间
     */
    void updateLoadedTime(Long orderId, LocalDateTime loadedTime);

    // ==================== 批量操作 ====================

    /**
     * 批量更新订单状态
     */
    void batchUpdateState(List<Long> orderIds, String state);

    /**
     * 批量更新开船时间
     */
    void batchUpdateDepartureTime(List<Long> orderIds, LocalDateTime departureTime);

    /**
     * 批量更新到港时间
     */
    void batchUpdateArrivalTime(List<Long> orderIds, LocalDateTime arrivalTime);

    /**
     * 批量更新提柜时间
     */
    void batchUpdatePickedTime(List<Long> orderIds, LocalDateTime pickedTime);

}
