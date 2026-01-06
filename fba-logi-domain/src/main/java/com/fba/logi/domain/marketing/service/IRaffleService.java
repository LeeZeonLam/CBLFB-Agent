package com.fba.logi.domain.marketing.service;

import com.fba.logi.domain.marketing.model.entity.StrategyAward;

/**
 * 抽奖服务接口
 */
public interface IRaffleService {

    /**
     * 执行抽奖
     *
     * @param strategyId 策略 ID
     * @param userId     用户 ID
     * @return 中奖奖品
     */
    StrategyAward performRaffle(Long strategyId, String userId);

    /**
     * 执行抽奖（带活动校验）
     *
     * @param activityId 活动 ID
     * @param userId     用户 ID
     * @return 中奖奖品
     */
    StrategyAward performRaffleWithActivity(Long activityId, String userId);

}
