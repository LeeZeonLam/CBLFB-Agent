package com.fba.logi.agent.workflow;

import com.fba.logi.common.constants.Constants;
import org.springframework.stereotype.Component;

/**
 * 工作流工厂
 * 提供预定义的业务工作流
 */
@Component
public class WorkflowFactory {

    /**
     * 创建 FBA 发货工作流
     * 订舱助手 -> 订单审核 -> 仓库入库
     */
    public AgentWorkflow createFbaShipmentWorkflow() {
        return AgentWorkflow.builder()
                .id("fba_shipment")
                .name("FBA 发货流程")
                .description("完整的 FBA 发货流程，从订单创建到仓库入库")

                // 添加节点
                .addAgentNode("booking", Constants.AgentType.ORDER_BOOKING)
                .addAgentNode("audit", Constants.AgentType.ORDER_AUDITOR)
                .addAgentNode("warehouse", Constants.AgentType.WAREHOUSE_OPS)

                // 添加条件节点：审核是否通过
                .addNode(AgentNode.condition("check_audit",
                        state -> {
                            // 检查审核结果
                            return state.getAgentResult(Constants.AgentType.ORDER_AUDITOR)
                                    .map(s -> !s.getFinalResponse().contains("拒绝"))
                                    .orElse(false);
                        },
                        "warehouse",  // 审核通过 -> 仓库
                        "__END__"     // 审核不通过 -> 结束
                ))

                // 设置流转
                .addEdge("booking", "audit")
                .addEdge("audit", "check_audit")
                .setStartNode("booking")
                .addEndNode("warehouse")

                .build();
    }

    /**
     * 创建营销活动工作流
     * 策略官配置 -> 销售助手推广
     */
    public AgentWorkflow createMarketingCampaignWorkflow() {
        return AgentWorkflow.builder()
                .id("marketing_campaign")
                .name("营销活动流程")
                .description("营销活动的配置和推广流程")

                .addAgentNode("strategy", Constants.AgentType.MARKETING_STRATEGIST)
                .addAgentNode("sales", Constants.AgentType.MARKETING_SALES)

                .addEdge("strategy", "sales")
                .setStartNode("strategy")
                .addEndNode("sales")

                .build();
    }

    /**
     * 创建订单处理工作流
     * 订舱 -> 运费查询 -> 确认 -> 审核
     */
    public AgentWorkflow createOrderProcessingWorkflow() {
        return AgentWorkflow.builder()
                .id("order_processing")
                .name("订单处理流程")
                .description("从接收订单到审核完成的流程")

                .addAgentNode("booking", Constants.AgentType.ORDER_BOOKING)

                // 添加运费查询工具节点
                .addNode(AgentNode.tool("query_rate", "query_shipping_rate"))

                // 添加用户确认节点
                .addNode(AgentNode.custom("confirm", "等待用户确认", state -> {
                    // 这里可以设置工作流暂停等待用户确认
                    state.setContextValue("awaiting_confirmation", true);
                    return state;
                }))

                .addAgentNode("audit", Constants.AgentType.ORDER_AUDITOR)

                .addEdge("booking", "query_rate")
                .addEdge("query_rate", "confirm")
                .addEdge("confirm", "audit")
                .setStartNode("booking")
                .addEndNode("audit")

                .build();
    }

    /**
     * 创建入库工作流
     * 材积录入 -> 标签验证 -> 超重检查 -> 入库确认
     */
    public AgentWorkflow createInboundWorkflow() {
        return AgentWorkflow.builder()
                .id("inbound")
                .name("仓库入库流程")
                .description("货物入库的完整流程")

                .addAgentNode("ops", Constants.AgentType.WAREHOUSE_OPS)

                // 材积录入
                .addNode(AgentNode.tool("dimension", "record_dimension"))

                // 超重检查条件
                .addNode(AgentNode.condition("check_overweight",
                        state -> {
                            // 检查是否超重
                            var result = state.getContextValue("toolResult_record_dimension");
                            if (result instanceof com.fba.logi.agent.skill.SkillResult sr) {
                                return !sr.getData().containsKey("hasWarning")
                                        || !(Boolean) sr.getData().get("hasWarning");
                            }
                            return true;
                        },
                        "confirm",    // 不超重 -> 确认入库
                        "notify"      // 超重 -> 通知
                ))

                // 超重通知
                .addNode(AgentNode.custom("notify", "超重通知", state -> {
                    state.setContextValue("overweight_warning", true);
                    return state;
                }))

                // 入库确认
                .addNode(AgentNode.tool("confirm", "confirm_inbound"))

                .addEdge("ops", "dimension")
                .addEdge("dimension", "check_overweight")
                .addEdge("notify", "confirm")
                .setStartNode("ops")
                .addEndNode("confirm")

                .build();
    }

    /**
     * 创建简单的单 Agent 工作流
     */
    public AgentWorkflow createSimpleAgentWorkflow(String agentType) {
        return AgentWorkflow.builder()
                .id("simple_" + agentType)
                .name("单Agent执行")
                .description("单个Agent的简单执行流程")
                .addAgentNode("main", agentType)
                .setStartNode("main")
                .addEndNode("main")
                .build();
    }
}
