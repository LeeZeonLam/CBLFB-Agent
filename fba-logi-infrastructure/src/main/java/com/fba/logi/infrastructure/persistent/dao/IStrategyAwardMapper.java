package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.StrategyAwardPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 策略奖品 Mapper
 */
@Mapper
public interface IStrategyAwardMapper extends BaseMapper<StrategyAwardPO> {

    /**
     * 扣减奖品库存
     *
     * @param strategyId 策略 ID
     * @param awardId    奖品 ID
     * @return 影响行数
     */
    @Update("UPDATE strategy_award SET award_count_surplus = award_count_surplus - 1, " +
            "update_time = NOW() " +
            "WHERE strategy_id = #{strategyId} AND award_id = #{awardId} AND award_count_surplus > 0")
    int deductStock(@Param("strategyId") Long strategyId, @Param("awardId") Integer awardId);

}
