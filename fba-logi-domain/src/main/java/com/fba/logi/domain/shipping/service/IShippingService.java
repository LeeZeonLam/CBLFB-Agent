package com.fba.logi.domain.shipping.service;

import com.fba.logi.domain.shipping.model.entity.Container;
import com.fba.logi.domain.shipping.model.entity.Voyage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 船运服务接口
 */
public interface IShippingService {

    // ==================== 柜子操作 ====================

    /**
     * 创建柜子
     *
     * @param containerType 柜型
     * @param voyageId      航次 ID（可选）
     * @return 柜子 ID
     */
    Long createContainer(String containerType, Long voyageId);

    /**
     * 查询柜子详情
     */
    Container getContainerDetail(Long containerId);

    /**
     * 根据编号查询柜子
     */
    Container getContainerByNo(String containerNo);

    /**
     * 查询可用于排柜的柜子
     */
    List<Container> getAvailableContainers(String containerType);

    /**
     * 排柜 - 将订单分配到柜子
     *
     * @param orderId     订单 ID
     * @param containerId 柜子 ID
     * @param volume      货物体积
     * @param weight      货物重量
     */
    void assignOrderToContainer(Long orderId, Long containerId, BigDecimal volume, BigDecimal weight);

    /**
     * 装柜确认 - 确认订单装入柜子
     */
    void loadOrderToContainer(Long orderId, Long containerId);

    /**
     * 完成柜子装柜
     */
    void finishContainerLoading(Long containerId);

    /**
     * 查询柜子内的订单 ID
     */
    List<Long> getOrdersInContainer(Long containerId);

    // ==================== 航次操作 ====================

    /**
     * 创建航次
     */
    Long createVoyage(Voyage voyage);

    /**
     * 查询航次详情
     */
    Voyage getVoyageDetail(Long voyageId);

    /**
     * 根据编号查询航次
     */
    Voyage getVoyageByNo(String voyageNo);

    /**
     * 将柜子分配到航次
     */
    void assignContainerToVoyage(Long containerId, Long voyageId);

    /**
     * 开船 - 批量更新航次、柜子、订单状态
     *
     * @param voyageId        航次 ID
     * @param actualDeparture 实际开船时间
     * @return 受影响的订单数量
     */
    int departVoyage(Long voyageId, LocalDateTime actualDeparture);

    /**
     * 到港 - 批量更新航次、柜子、订单状态
     *
     * @param voyageId      航次 ID
     * @param actualArrival 实际到港时间
     * @return 受影响的订单数量
     */
    int arrivePort(Long voyageId, LocalDateTime actualArrival);

    /**
     * 提柜 - 批量更新柜子和订单状态
     *
     * @param containerId 柜子 ID
     * @return 受影响的订单数量
     */
    int pickContainer(Long containerId);

    // ==================== 查询统计 ====================

    /**
     * 查询即将开船的航次
     */
    List<Voyage> getUpcomingVoyages(int days);

    /**
     * 查询指定状态的航次
     */
    List<Voyage> getVoyagesByStatus(String status);

    /**
     * 获取航次统计信息
     */
    VoyageStatistics getVoyageStatistics(Long voyageId);

    /**
     * 航次统计信息
     */
    record VoyageStatistics(
            Long voyageId,
            String voyageNo,
            int containerCount,
            int orderCount,
            BigDecimal totalWeight,
            BigDecimal totalVolume
    ) {
    }
}
