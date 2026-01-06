package com.fba.logi.domain.marketing.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 策略规则实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyRule {

    /**
     * 策略 ID
     */
    private Long strategyId;

    /**
     * 奖品 ID（可为空，表示全局规则）
     */
    private Integer awardId;

    /**
     * 规则类型：rule_weight / rule_blacklist / rule_lock
     */
    private String ruleType;

    /**
     * 规则模型：rule_weight, rule_blacklist, rule_lock 等
     */
    private String ruleModel;

    /**
     * 规则值
     */
    private String ruleValue;

    /**
     * 规则描述
     */
    private String ruleDesc;

}
