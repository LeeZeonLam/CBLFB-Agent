package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.ActivityPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 活动 Mapper
 */
@Mapper
public interface IActivityMapper extends BaseMapper<ActivityPO> {

    /**
     * 扣减活动库存
     *
     * @param activityId 活动 ID
     * @return 影响行数
     */
    @Update("UPDATE raffle_activity SET stock_count_surplus = stock_count_surplus - 1, " +
            "update_time = NOW() " +
            "WHERE activity_id = #{activityId} AND stock_count_surplus > 0")
    int deductStock(@Param("activityId") Long activityId);

    /**
     * 更新活动状态
     *
     * @param activityId 活动 ID
     * @param state      状态
     * @return 影响行数
     */
    @Update("UPDATE raffle_activity SET state = #{state}, update_time = NOW() " +
            "WHERE activity_id = #{activityId}")
    int updateState(@Param("activityId") Long activityId, @Param("state") String state);

}
