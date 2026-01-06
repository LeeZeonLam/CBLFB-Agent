package com.fba.logi.domain.shipping.repository;

import com.fba.logi.domain.shipping.model.entity.Voyage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 航次仓储接口
 */
public interface IVoyageRepository {

    /**
     * 根据 ID 查询航次
     */
    Voyage queryById(Long voyageId);

    /**
     * 根据航次编号查询
     */
    Voyage queryByVoyageNo(String voyageNo);

    /**
     * 查询指定状态的航次
     */
    List<Voyage> queryByStatus(String status);

    /**
     * 查询指定时间范围内开船的航次
     */
    List<Voyage> queryByDepartureDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * 查询指定渠道的航次
     */
    List<Voyage> queryByChannel(String channelName);

    /**
     * 查询即将开船的航次（指定天数内）
     */
    List<Voyage> queryUpcomingVoyages(int days);

    /**
     * 保存航次
     */
    void save(Voyage voyage);

    /**
     * 更新航次状态
     */
    void updateStatus(Long voyageId, String status);

    /**
     * 更新开船时间
     */
    void updateDepartureTime(Long voyageId, LocalDateTime actualDeparture);

    /**
     * 更新到港时间
     */
    void updateArrivalTime(Long voyageId, LocalDateTime actualArrival);

    /**
     * 添加柜子到航次
     */
    void addContainerToVoyage(Long voyageId, Long containerId);

    /**
     * 从航次移除柜子
     */
    void removeContainerFromVoyage(Long voyageId, Long containerId);

    /**
     * 查询航次包含的柜子 ID
     */
    List<Long> queryContainerIdsByVoyageId(Long voyageId);

    /**
     * 生成航次编号
     */
    String generateVoyageNo();

    /**
     * 删除航次
     */
    void delete(Long voyageId);
}
