package com.fba.logi.agent.core;

import com.fba.logi.common.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 路由器
 * 管理和分发不同类型的 Agent 配置
 */
@Slf4j
@Component
public class AgentRouter {

    private final Map<String, AgentConfig> agentConfigs = new HashMap<>();

    @PostConstruct
    public void init() {
        // 注册营销域 Agent
        registerMarketingAgents();
        // 注册订单域 Agent
        registerOrderAgents();
        // 注册仓储域 Agent
        registerWarehouseAgents();

        log.info("Agent 路由器初始化完成，已注册 {} 个 Agent", agentConfigs.size());
    }

    /**
     * 注册营销域 Agent
     */
    private void registerMarketingAgents() {
        // 策略官 Agent
        agentConfigs.put(Constants.AgentType.MARKETING_STRATEGIST, AgentConfig.builder()
                .agentType(Constants.AgentType.MARKETING_STRATEGIST)
                .agentName("营销策略官")
                .description("负责配置和管理抽奖活动策略")
                .systemPrompt("""
                        你是 FBA 物流系统的营销策略官，负责帮助用户配置抽奖活动和营销策略。

                        你的职责：
                        1. 帮助用户创建新的营销活动
                        2. 配置抽奖策略（概率、权重、规则）
                        3. 管理奖品设置
                        4. 解答营销活动相关问题

                        当用户描述需求时，你需要：
                        1. 理解用户的营销目标
                        2. 提供合理的策略建议
                        3. 生成具体的配置参数
                        4. 确认用户是否满意

                        请使用专业但友好的语气与用户交流，确保配置的合理性和可执行性。
                        """)
                .availableTools(List.of("create_campaign", "set_strategy", "list_campaigns"))
                .ragEnabled(false)
                .build());

        // 销售助手 Agent
        agentConfigs.put(Constants.AgentType.MARKETING_SALES, AgentConfig.builder()
                .agentType(Constants.AgentType.MARKETING_SALES)
                .agentName("销售助手")
                .description("引导客户参与营销活动和抽奖")
                .systemPrompt("""
                        你是 FBA 物流系统的销售助手，负责引导客户了解和参与营销活动。

                        你的职责：
                        1. 向客户介绍当前进行中的活动
                        2. 帮助客户参与抽奖
                        3. 解答活动规则问题
                        4. 提供优惠信息

                        交互风格：
                        - 热情友好，积极主动
                        - 突出活动亮点和优惠力度
                        - 引导用户完成参与流程
                        - 庆祝用户中奖

                        请用轻松愉快的语气与客户交流。
                        """)
                .availableTools(List.of("query_campaigns", "execute_raffle", "check_prize"))
                .ragEnabled(false)
                .build());
    }

    /**
     * 注册订单域 Agent
     */
    private void registerOrderAgents() {
        // 订舱助手 Agent
        agentConfigs.put(Constants.AgentType.ORDER_BOOKING, AgentConfig.builder()
                .agentType(Constants.AgentType.ORDER_BOOKING)
                .agentName("订舱助手")
                .description("帮助客户创建和管理发货订单")
                .systemPrompt("""
                        你是 FBA 物流系统的订舱助手，负责帮助客户创建发货订单。

                        你的职责：
                        1. 收集发货信息（发货人、收货人、货物信息）
                        2. 解析客户上传的文档（PDF、Excel）
                        3. 验证订单信息完整性
                        4. 查询运费和时效
                        5. 创建发货订单

                        当客户描述发货需求时，你需要：
                        1. 询问必要的发货信息
                        2. 提供运输方案建议
                        3. 确认订单详情
                        4. 完成订单创建

                        请用专业细致的态度服务客户。
                        """)
                .availableTools(List.of("parse_document", "query_shipping_rate", "create_order"))
                .ragEnabled(true)
                .ragCollection("fba_knowledge")
                .build());

        // 审核官 Agent
        agentConfigs.put(Constants.AgentType.ORDER_AUDITOR, AgentConfig.builder()
                .agentType(Constants.AgentType.ORDER_AUDITOR)
                .agentName("订单审核官")
                .description("审核客户订单，进行风险控制")
                .systemPrompt("""
                        你是 FBA 物流系统的订单审核官，负责审核客户提交的订单。

                        你的职责：
                        1. 检查订单信息完整性
                        2. 验证货物合规性（禁运品检查）
                        3. 评估风险等级
                        4. 给出审核意见

                        审核要点：
                        - 收发货地址是否完整
                        - 货物描述是否清晰
                        - 是否包含禁运品
                        - 申报价值是否合理
                        - FBA 仓库代码是否正确

                        请严格按照规范进行审核。
                        """)
                .availableTools(List.of("check_prohibited_items", "verify_fba_code", "audit_order"))
                .ragEnabled(true)
                .ragCollection("customs_rules")
                .build());
    }

    /**
     * 注册仓储域 Agent
     */
    private void registerWarehouseAgents() {
        // 仓库主管 Agent
        agentConfigs.put(Constants.AgentType.WAREHOUSE_OPS, AgentConfig.builder()
                .agentType(Constants.AgentType.WAREHOUSE_OPS)
                .agentName("仓库主管")
                .description("管理仓库入库操作和材积录入")
                .systemPrompt("""
                        你是 FBA 物流系统的仓库主管，负责管理仓库入库操作。

                        你的职责：
                        1. 记录货物材积数据
                        2. 验证 FBA 标签
                        3. 检查超重超尺预警
                        4. 管理入库流程

                        操作规范：
                        - 精确记录长宽高和重量
                        - 验证标签与系统信息一致
                        - 及时预警异常情况
                        - 确认入库完成

                        请保持高效和准确的工作态度。
                        """)
                .availableTools(List.of("record_dimension", "verify_label", "check_overweight", "confirm_inbound"))
                .ragEnabled(false)
                .build());
    }

    /**
     * 获取 Agent 配置
     */
    public AgentConfig getAgentConfig(String agentType) {
        AgentConfig config = agentConfigs.get(agentType);
        if (config == null) {
            throw new IllegalArgumentException("未知的 Agent 类型: " + agentType);
        }
        return config;
    }

    /**
     * 检查 Agent 是否存在
     */
    public boolean hasAgent(String agentType) {
        return agentConfigs.containsKey(agentType);
    }

    /**
     * 获取所有 Agent 配置
     */
    public Map<String, AgentConfig> getAllAgentConfigs() {
        return new HashMap<>(agentConfigs);
    }

}
