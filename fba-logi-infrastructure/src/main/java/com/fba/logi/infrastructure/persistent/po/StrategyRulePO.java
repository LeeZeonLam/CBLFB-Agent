package com.fba.logi.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 策略规则持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("strategy_rule")
public class StrategyRulePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 策略 ID
     */
    private Long strategyId;

    /**
     * 奖品 ID（可选，针对特定奖品的规则）
     */
    private Integer awardId;

    /**
     * 规则类型
     */
    private String ruleType;

    /**
     * 规则模型
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
