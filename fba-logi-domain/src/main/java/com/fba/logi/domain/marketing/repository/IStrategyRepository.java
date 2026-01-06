package com.fba.logi.domain.marketing.repository;

import com.fba.logi.domain.marketing.model.entity.Strategy;
import com.fba.logi.domain.marketing.model.entity.StrategyAward;

import java.util.List;

/**
 * 策略仓储接口
 */
public interface IStrategyRepository {

    /**
     * 根据策略 ID 查询策略
     *
     * @param strategyId 策略 ID
     * @return 策略实体
     */
    Strategy queryStrategyById(Long strategyId);

    /**
     * 查询策略奖品列表
     *
     * @param strategyId 策略 ID
     * @return 策略奖品列表
     */
    List<StrategyAward> queryStrategyAwardList(Long strategyId);

    /**
     * 查询策略奖品
     *
     * @param strategyId 策略 ID
     * @param awardId    奖品 ID
     * @return 策略奖品
     */
    StrategyAward queryStrategyAward(Long strategyId, Integer awardId);

    /**
     * 扣减奖品库存
     *
     * @param strategyId 策略 ID
     * @param awardId    奖品 ID
     * @return 是否成功
     */
    boolean deductAwardStock(Long strategyId, Integer awardId);

    /**
     * 保存策略
     *
     * @param strategy 策略实体
     */
    void saveStrategy(Strategy strategy);

    /**
     * 保存策略奖品
     *
     * @param strategyAward 策略奖品实体
     */
    void saveStrategyAward(StrategyAward strategyAward);

}
