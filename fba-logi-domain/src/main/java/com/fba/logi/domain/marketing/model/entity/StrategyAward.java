package com.fba.logi.domain.marketing.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 策略奖品实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyAward {

    /**
     * 策略 ID
     */
    private Long strategyId;

    /**
     * 奖品 ID
     */
    private Integer awardId;

    /**
     * 奖品标题
     */
    private String awardTitle;

    /**
     * 奖品副标题
     */
    private String awardSubtitle;

    /**
     * 奖品总数量
     */
    private Integer awardCount;

    /**
     * 奖品剩余数量
     */
    private Integer awardCountSurplus;

    /**
     * 中奖概率
     */
    private BigDecimal awardRate;

    /**
     * 规则模型（逗号分隔）
     */
    private String ruleModels;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 扣减奖品库存
     */
    public boolean deductStock() {
        if (awardCountSurplus <= 0) {
            return false;
        }
        this.awardCountSurplus--;
        return true;
    }

}
