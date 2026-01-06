package com.fba.logi.domain.marketing.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 抽奖活动实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    /**
     * 活动 ID
     */
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动描述
     */
    private String activityDesc;

    /**
     * 开始时间
     */
    private LocalDateTime beginDateTime;

    /**
     * 结束时间
     */
    private LocalDateTime endDateTime;

    /**
     * 库存总量
     */
    private Integer stockCount;

    /**
     * 剩余库存
     */
    private Integer stockCountSurplus;

    /**
     * 活动状态：create/open/close
     */
    private String state;

    /**
     * 关联的策略 ID
     */
    private Long strategyId;

    /**
     * 判断活动是否有效
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return "open".equals(state)
                && now.isAfter(beginDateTime)
                && now.isBefore(endDateTime)
                && stockCountSurplus > 0;
    }

    /**
     * 扣减库存
     */
    public boolean deductStock() {
        if (stockCountSurplus <= 0) {
            return false;
        }
        this.stockCountSurplus--;
        return true;
    }

}
