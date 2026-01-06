package com.fba.logi.domain.shipping.repository;

import com.fba.logi.domain.shipping.model.entity.Container;

import java.util.List;

/**
 * 集装箱仓储接口
 */
public interface IContainerRepository {

    /**
     * 根据 ID 查询柜子
     */
    Container queryById(Long containerId);

    /**
     * 根据编号查询柜子
     */
    Container queryByContainerNo(String containerNo);

    /**
     * 根据航次查询柜子列表
     */
    List<Container> queryByVoyageId(Long voyageId);

    /**
     * 查询可用于排柜的柜子（按柜型和状态）
     */
    List<Container> queryAvailableContainers(String containerType, String status);

    /**
     * 查询指定状态的柜子
     */
    List<Container> queryByStatus(String status);

    /**
     * 保存柜子
     */
    void save(Container container);

    /**
     * 更新柜子状态
     */
    void updateStatus(Long containerId, String status);

    /**
     * 批量更新柜子状态（用于开船、到港等批量操作）
     */
    void batchUpdateStatus(List<Long> containerIds, String status);

    /**
     * 添加订单到柜子
     */
    void addOrderToContainer(Long containerId, Long orderId);

    /**
     * 从柜子移除订单
     */
    void removeOrderFromContainer(Long containerId, Long orderId);

    /**
     * 查询柜子包含的订单 ID
     */
    List<Long> queryOrderIdsByContainerId(Long containerId);

    /**
     * 生成柜子编号
     */
    String generateContainerNo();

    /**
     * 删除柜子
     */
    void delete(Long containerId);
}
