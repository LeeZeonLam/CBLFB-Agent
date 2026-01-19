package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.StrategyRulePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 策略规则 Mapper
 */
@Mapper
public interface IStrategyRuleMapper extends BaseMapper<StrategyRulePO> {

    /**
     * 根据策略 ID 查询规则列表
     *
     * @param strategyId 策略 ID
     * @return 规则列表
     */
    @Select("SELECT * FROM strategy_rule WHERE strategy_id = #{strategyId}")
    List<StrategyRulePO> selectByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 根据策略 ID 和规则模型查询规则
     *
     * @param strategyId 策略 ID
     * @param ruleModel  规则模型
     * @return 规则
     */
    @Select("SELECT * FROM strategy_rule WHERE strategy_id = #{strategyId} AND rule_model = #{ruleModel}")
    StrategyRulePO selectByStrategyIdAndRuleModel(@Param("strategyId") Long strategyId,
                                                   @Param("ruleModel") String ruleModel);

}
