package com.fba.logi.agent.skill;

import com.fba.logi.agent.core.AgentExecutor;
import com.fba.logi.agent.core.AgentState;
import com.fba.logi.agent.subagent.SubAgentRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 委托给其他 Agent 的 Skill
 * 允许一个 Agent 将任务委托给另一个 Agent 处理
 */
@Slf4j
@Component
public class DelegateToAgentSkill extends AbstractSkill {

    private final SubAgentRegistry subAgentRegistry;
    private final AgentExecutor agentExecutor;

    // 使用 @Lazy 避免循环依赖
    public DelegateToAgentSkill(SubAgentRegistry subAgentRegistry, @Lazy AgentExecutor agentExecutor) {
        this.subAgentRegistry = subAgentRegistry;
        this.agentExecutor = agentExecutor;
    }

    @Override
    public String getSkillId() {
        return "delegate_to_agent";
    }

    @Override
    public String getSkillName() {
        return "委托给专家助手";
    }

    @Override
    public String getDescription() {
        return "将任务委托给指定的专家助手处理，适用于需要跨领域协作的场景";
    }

    @Override
    public String getDomain() {
        return "orchestration";
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "targetAgent", "目标Agent类型标识"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "task", "要委托的任务描述"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "context", "额外的上下文信息（可选）")
                ))
                .required(List.of("targetAgent", "task"))
                .build();
    }

    @Override
    public SkillValidationResult validateParameters(Map<String, Object> parameters) {
        SkillValidationResult result = SkillValidationResult.success();

        String targetAgent = (String) parameters.get("targetAgent");
        if (targetAgent == null || targetAgent.isEmpty()) {
            result.addError("目标Agent不能为空");
        } else if (!subAgentRegistry.isDelegatable(targetAgent)) {
            result.addError("目标Agent不可用或不可委托: " + targetAgent);
        }

        if (!parameters.containsKey("task") || parameters.get("task") == null) {
            result.addError("任务描述不能为空");
        }

        return result;
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String targetAgent = getRequiredString(parameters, "targetAgent");
        String task = getRequiredString(parameters, "task");
        String extraContext = getOptionalString(parameters, "context", "");

        log.info("委托任务到 Agent: {}, 任务: {}", targetAgent, task);

        // 检查是否会形成循环调用
        if (targetAgent.equals(context.getAgentType())) {
            return SkillResult.failure("不能委托给自己", "SELF_DELEGATION");
        }

        // 构建子Agent状态
        AgentState subAgentState = AgentState.builder()
                .sessionId(context.getSessionId())
                .userId(context.getUserId())
                .agentType(targetAgent)
                .userMessage(task)
                .build();

        // 传递上下文
        if (!extraContext.isEmpty()) {
            subAgentState.setContextValue("delegationContext", extraContext);
        }
        subAgentState.setContextValue("delegatedFrom", context.getAgentType());

        try {
            // 执行子Agent
            AgentState resultState = agentExecutor.execute(subAgentState);

            return SkillResult.success(
                    String.format("【%s 的回复】\n%s",
                            subAgentRegistry.getSubAgent(targetAgent)
                                    .map(c -> c.getAgentName())
                                    .orElse(targetAgent),
                            resultState.getFinalResponse()),
                    Map.of(
                            "delegatedTo", targetAgent,
                            "response", resultState.getFinalResponse(),
                            "toolResults", resultState.getToolResultStrings()
                    )
            );

        } catch (Exception e) {
            log.error("委托执行失败", e);
            return SkillResult.failure("委托执行失败: " + e.getMessage(), "DELEGATION_ERROR");
        }
    }
}
