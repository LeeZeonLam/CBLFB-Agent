package com.fba.logi.agent.config;

import com.fba.logi.agent.skill.SkillRegistry;
import com.fba.logi.common.constants.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Agent 和 Skill 绑定配置
 * 在应用启动后自动配置 Agent 可用的 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentSkillConfiguration {

    private final SkillRegistry skillRegistry;

    @EventListener(ApplicationReadyEvent.class)
    public void configureAgentSkills() {
        log.info("配置 Agent 和 Skill 绑定关系...");

        // 营销策略官的技能
        skillRegistry.bindSkillsToAgent(
                Constants.AgentType.MARKETING_STRATEGIST,
                "create_campaign",       // 创建活动
                "set_strategy",          // 设置抽奖策略
                "query_campaigns",       // 查询活动列表
                "delegate_to_agent"      // 可以委托给其他Agent
        );

        // 销售助手的技能
        skillRegistry.bindSkillsToAgent(
                Constants.AgentType.MARKETING_SALES,
                "query_campaigns",       // 查询进行中的活动
                "execute_raffle"         // 执行抽奖
        );

        // 订舱助手的技能
        skillRegistry.bindSkillsToAgent(
                Constants.AgentType.ORDER_BOOKING,
                "create_order",          // 创建订单
                "query_shipping_rate",   // 查询运费
                "verify_fba_code",       // 验证FBA仓库代码
                "delegate_to_agent"      // 可以委托给其他Agent
        );

        // 订单审核官的技能
        skillRegistry.bindSkillsToAgent(
                Constants.AgentType.ORDER_AUDITOR,
                "check_prohibited_items", // 禁运品检查
                "verify_fba_code",        // 验证FBA代码
                "audit_order"             // 审核订单
        );

        // 仓库主管的技能
        skillRegistry.bindSkillsToAgent(
                Constants.AgentType.WAREHOUSE_OPS,
                "record_dimension",       // 录入货物尺寸
                "verify_label",           // 验证FBA标签
                "check_overweight",       // 超重超尺检查
                "confirm_inbound"         // 确认入库
        );

        // 写作分析 Agent 的技能
        skillRegistry.bindSkillsToAgent(
                Constants.AgentType.WRITING_ANALYST,
                "analyze_writing"
        );

        log.info("Agent 和 Skill 绑定配置完成");
        log.info("SkillRegistry 统计: {}", skillRegistry.getStats());
    }
}
