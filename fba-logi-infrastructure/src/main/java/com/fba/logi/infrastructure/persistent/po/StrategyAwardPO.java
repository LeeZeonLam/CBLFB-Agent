package com.fba.logi.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 策略奖品持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("strategy_award")
public class StrategyAwardPO {

    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 奖品数量
     */
    private Integer awardCount;

    /**
     * 剩余数量
     */
    private Integer awardCountSurplus;

    /**
     * 中奖概率
     */
    private BigDecimal awardRate;

    /**
     * 规则模型
     */
    private String ruleModels;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
