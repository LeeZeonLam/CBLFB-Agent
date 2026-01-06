package com.fba.logi.agent.workflow;

import com.fba.logi.agent.core.AgentExecutor;
import com.fba.logi.agent.core.AgentState;
import com.fba.logi.agent.skill.SkillContext;
import com.fba.logi.agent.skill.SkillExecutor;
import com.fba.logi.agent.skill.SkillResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 工作流执行器
 * 负责按照工作流定义执行各个节点
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowExecutor {

    private final AgentExecutor agentExecutor;
    private final SkillExecutor skillExecutor;

    /**
     * 最大执行步数（防止无限循环）
     */
    private static final int MAX_STEPS = 50;

    /**
     * 执行工作流
     */
    public WorkflowState execute(AgentWorkflow workflow, String sessionId, String userId, String input) {
        WorkflowState state = WorkflowState.create(sessionId, userId, input);
        return execute(workflow, state);
    }

    /**
     * 执行工作流（使用已有状态）
     */
    public WorkflowState execute(AgentWorkflow workflow, WorkflowState state) {
        log.info("开始执行工作流: {}, 会话: {}", workflow.getWorkflowName(), state.getSessionId());

        state.setStatus(WorkflowState.Status.RUNNING);
        state.setCurrentNodeId(workflow.getStartNodeId());

        int stepCount = 0;

        while (stepCount < MAX_STEPS) {
            stepCount++;
            String currentNodeId = state.getCurrentNodeId();

            // 检查是否到达结束节点
            if (currentNodeId == null || workflow.isEndNode(currentNodeId)) {
                log.info("工作流执行完成，共 {} 步", stepCount);
                state.setStatus(WorkflowState.Status.COMPLETED);
                state.setEndTime(System.currentTimeMillis());
                state.setFinalOutput(state.getAggregatedOutput());
                break;
            }

            // 获取当前节点
            AgentNode node = workflow.getNode(currentNodeId).orElse(null);
            if (node == null) {
                log.error("节点不存在: {}", currentNodeId);
                state.setStatus(WorkflowState.Status.FAILED);
                state.setErrorMessage("节点不存在: " + currentNodeId);
                break;
            }

            log.debug("执行节点: {} ({})", node.getNodeId(), node.getType());

            // 执行节点
            long nodeStartTime = System.currentTimeMillis();
            boolean success = false;
            String output = null;
            String error = null;

            try {
                switch (node.getType()) {
                    case AGENT:
                        output = executeAgentNode(node, state);
                        success = true;
                        break;

                    case TOOL:
                        output = executeToolNode(node, state);
                        success = true;
                        break;

                    case CONDITION:
                        // 条件节点直接在获取下一节点时处理
                        success = true;
                        break;

                    case CUSTOM:
                        state = executeCustomNode(node, state);
                        success = true;
                        break;

                    case START:
                    case END:
                        success = true;
                        break;

                    default:
                        error = "未知节点类型: " + node.getType();
                }

            } catch (Exception e) {
                log.error("节点执行失败: {}", node.getNodeId(), e);
                error = e.getMessage();

                if (!node.isSkippable()) {
                    state.setStatus(WorkflowState.Status.FAILED);
                    state.setErrorMessage(error);
                    state.setEndTime(System.currentTimeMillis());
                    return state;
                }
            }

            // 记录执行
            long nodeEndTime = System.currentTimeMillis();
            state.recordNodeExecution(node, nodeStartTime, nodeEndTime, success, output, error);

            // 获取下一个节点
            String nextNodeId = workflow.getNextNodeId(currentNodeId, state);
            state.setCurrentNodeId(nextNodeId);
        }

        if (stepCount >= MAX_STEPS) {
            log.warn("工作流执行超过最大步数限制: {}", MAX_STEPS);
            state.setStatus(WorkflowState.Status.FAILED);
            state.setErrorMessage("执行步数超过限制");
        }

        return state;
    }

    /**
     * 执行 Agent 节点
     */
    private String executeAgentNode(AgentNode node, WorkflowState state) {
        AgentState agentState = AgentState.builder()
                .sessionId(state.getSessionId())
                .userId(state.getUserId())
                .agentType(node.getAgentType())
                .userMessage(state.getCurrentMessage())
                .context(state.getContext())
                .build();

        AgentState resultState = agentExecutor.execute(agentState);

        // 保存结果
        state.saveAgentResult(node.getAgentType(), resultState);

        // 更新当前消息为 Agent 的输出（供后续节点使用）
        state.setCurrentMessage(resultState.getFinalResponse());

        return resultState.getFinalResponse();
    }

    /**
     * 执行工具节点
     */
    private String executeToolNode(AgentNode node, WorkflowState state) {
        SkillContext context = SkillContext.builder()
                .sessionId(state.getSessionId())
                .userId(state.getUserId())
                .variables(state.getContext())
                .userMessage(state.getCurrentMessage())
                .build();

        // 从上下文获取工具参数
        @SuppressWarnings("unchecked")
        Map<String, Object> params = state.hasContextValue("toolParams_" + node.getToolId())
                ? state.getContextValue("toolParams_" + node.getToolId())
                : Map.of();

        SkillResult result = skillExecutor.execute(node.getToolId(), context, params);

        // 将结果存入上下文
        state.setContextValue("toolResult_" + node.getToolId(), result);

        return result.getMessage();
    }

    /**
     * 执行自定义节点
     */
    private WorkflowState executeCustomNode(AgentNode node, WorkflowState state) {
        if (node.getHandler() != null) {
            return node.getHandler().apply(state);
        }
        return state;
    }

    /**
     * 暂停工作流（等待用户输入）
     */
    public void pause(WorkflowState state) {
        state.setStatus(WorkflowState.Status.WAITING);
        log.info("工作流暂停，等待用户输入");
    }

    /**
     * 恢复工作流
     */
    public WorkflowState resume(AgentWorkflow workflow, WorkflowState state, String userInput) {
        if (state.getStatus() != WorkflowState.Status.WAITING) {
            log.warn("工作流状态不是等待中，无法恢复");
            return state;
        }

        state.setCurrentMessage(userInput);
        state.setStatus(WorkflowState.Status.RUNNING);

        return execute(workflow, state);
    }
}
