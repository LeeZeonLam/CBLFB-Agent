package com.fba.logi.domain.marketing.repository;

import com.fba.logi.domain.marketing.model.entity.Activity;

import java.util.List;

/**
 * 活动仓储接口
 */
public interface IActivityRepository {

    /**
     * 根据活动 ID 查询活动
     *
     * @param activityId 活动 ID
     * @return 活动实体
     */
    Activity queryActivityById(Long activityId);

    /**
     * 查询所有有效活动
     *
     * @return 活动列表
     */
    List<Activity> queryValidActivities();

    /**
     * 扣减活动库存
     *
     * @param activityId 活动 ID
     * @return 是否成功
     */
    boolean deductActivityStock(Long activityId);

    /**
     * 保存活动
     *
     * @param activity 活动实体
     */
    void saveActivity(Activity activity);

    /**
     * 更新活动状态
     *
     * @param activityId 活动 ID
     * @param state      新状态
     */
    void updateActivityState(Long activityId, String state);

}
