package com.fba.logi.domain.marketing.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抽奖策略实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Strategy {

    /**
     * 策略 ID
     */
    private Long strategyId;

    /**
     * 策略描述
     */
    private String strategyDesc;

    /**
     * 策略奖品列表
     */
    private List<StrategyAward> strategyAwards;

    /**
     * 策略规则列表
     */
    private List<StrategyRule> strategyRules;

}
