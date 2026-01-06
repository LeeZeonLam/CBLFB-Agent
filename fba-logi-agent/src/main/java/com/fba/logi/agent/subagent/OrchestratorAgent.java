package com.fba.logi.agent.subagent;

import com.fba.logi.agent.core.AgentConfig;
import com.fba.logi.agent.core.AgentExecutor;
import com.fba.logi.agent.core.AgentState;
import com.fba.logi.infrastructure.adapter.llm.ILlmClient;
import com.fba.logi.infrastructure.adapter.llm.LlmClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编排者 Agent
 * 作为主Agent，负责理解用户意图并将任务委托给合适的子Agent
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestratorAgent {

    private final SubAgentRegistry subAgentRegistry;
    private final AgentExecutor agentExecutor;
    private final LlmClientFactory llmClientFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ORCHESTRATOR_SYSTEM_PROMPT = """
            你是 FBA 物流系统的智能调度中心，负责理解用户的需求并将任务分配给合适的专家助手。

            你的职责：
            1. 分析用户的意图和需求
            2. 判断应该由哪个专家助手来处理
            3. 将任务委托给对应的专家
            4. 汇总专家的处理结果并回复用户

            ## 工作流程
            1. 首先判断用户的问题属于哪个领域（营销、订单、仓储）
            2. 选择最合适的专家助手
            3. 使用 delegate_to_agent 将任务委托出去
            4. 根据返回结果给用户最终回复

            ## 委托格式
            当需要委托任务时，使用以下格式：
            <delegate>{"agent": "agent_type", "task": "任务描述"}</delegate>

            ## 直接回复情况
            如果用户只是简单的问候或者问题不需要专家处理，你可以直接回复。
            """;

    // 解析委托格式
    private static final Pattern DELEGATE_PATTERN = Pattern.compile(
            "<delegate>\\s*(\\{.*?\\})\\s*</delegate>",
            Pattern.DOTALL
    );

    /**
     * 处理用户请求
     */
    public OrchestratorResult process(String sessionId, String userId, String userMessage) {
        log.info("Orchestrator 处理请求, 会话: {}, 用户消息: {}", sessionId, userMessage);

        try {
            // 1. 构建完整的系统提示词
            String fullPrompt = ORCHESTRATOR_SYSTEM_PROMPT + "\n\n" + subAgentRegistry.generateAgentSelectionPrompt();

            // 2. 调用 LLM 判断意图
            ILlmClient llmClient = llmClientFactory.getChatClient();
            var chatResponse = llmClient.chat(List.of(
                    dev.langchain4j.data.message.SystemMessage.from(fullPrompt),
                    dev.langchain4j.data.message.UserMessage.from(userMessage)
            ));
            String llmResponse = chatResponse.aiMessage().text();

            // 3. 解析是否需要委托
            Matcher matcher = DELEGATE_PATTERN.matcher(llmResponse);
            if (matcher.find()) {
                // 需要委托给子Agent
                String delegateJson = matcher.group(1);
                JsonNode delegateNode = objectMapper.readTree(delegateJson);

                String targetAgent = delegateNode.get("agent").asText();
                String task = delegateNode.has("task") ? delegateNode.get("task").asText() : userMessage;

                log.info("委托任务给子Agent: {}, 任务: {}", targetAgent, task);

                // 4. 验证目标Agent是否可委托
                if (!subAgentRegistry.isDelegatable(targetAgent)) {
                    return OrchestratorResult.builder()
                            .success(false)
                            .message("抱歉，无法处理该请求，请尝试其他方式。")
                            .build();
                }

                // 5. 执行子Agent
                AgentState subAgentState = AgentState.builder()
                        .sessionId(sessionId)
                        .userId(userId)
                        .agentType(targetAgent)
                        .userMessage(task)
                        .build();

                AgentState resultState = agentExecutor.execute(subAgentState);

                // 6. 汇总结果
                return OrchestratorResult.builder()
                        .success(true)
                        .delegatedTo(targetAgent)
                        .subAgentResponse(resultState.getFinalResponse())
                        .message(resultState.getFinalResponse())
                        .toolResults(resultState.getToolResultStrings())
                        .build();

            } else {
                // 不需要委托，直接回复
                // 清理可能残留的委托标记
                String cleanResponse = llmResponse.replaceAll("<delegate>.*?</delegate>", "").trim();

                return OrchestratorResult.builder()
                        .success(true)
                        .message(cleanResponse.isEmpty() ? llmResponse : cleanResponse)
                        .build();
            }

        } catch (Exception e) {
            log.error("Orchestrator 处理失败", e);
            return OrchestratorResult.builder()
                    .success(false)
                    .message("抱歉，系统处理出现问题，请稍后重试。")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * 处理多轮对话
     */
    public OrchestratorResult processWithHistory(String sessionId, String userId, String userMessage,
                                                  List<AgentState.ChatHistoryItem> history) {
        log.info("Orchestrator 处理多轮对话, 会话: {}", sessionId);

        try {
            // 1. 构建完整的系统提示词
            String fullPrompt = ORCHESTRATOR_SYSTEM_PROMPT + "\n\n" + subAgentRegistry.generateAgentSelectionPrompt();

            // 2. 构建消息列表
            List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
            messages.add(dev.langchain4j.data.message.SystemMessage.from(fullPrompt));

            // 添加历史消息
            for (AgentState.ChatHistoryItem item : history) {
                if ("user".equals(item.getRole())) {
                    messages.add(dev.langchain4j.data.message.UserMessage.from(item.getContent()));
                } else if ("assistant".equals(item.getRole())) {
                    messages.add(dev.langchain4j.data.message.AiMessage.from(item.getContent()));
                }
            }

            // 添加当前消息
            messages.add(dev.langchain4j.data.message.UserMessage.from(userMessage));

            // 3. 调用 LLM
            ILlmClient llmClient = llmClientFactory.getChatClient();
            var chatResponse = llmClient.chat(messages);
            String llmResponse = chatResponse.aiMessage().text();

            // 4. 解析和处理（同单轮）
            Matcher matcher = DELEGATE_PATTERN.matcher(llmResponse);
            if (matcher.find()) {
                String delegateJson = matcher.group(1);
                JsonNode delegateNode = objectMapper.readTree(delegateJson);
                String targetAgent = delegateNode.get("agent").asText();
                String task = delegateNode.has("task") ? delegateNode.get("task").asText() : userMessage;

                if (!subAgentRegistry.isDelegatable(targetAgent)) {
                    return OrchestratorResult.builder()
                            .success(false)
                            .message("抱歉，无法处理该请求。")
                            .build();
                }

                AgentState subAgentState = AgentState.builder()
                        .sessionId(sessionId)
                        .userId(userId)
                        .agentType(targetAgent)
                        .userMessage(task)
                        .chatHistory(new ArrayList<>(history))
                        .build();

                AgentState resultState = agentExecutor.execute(subAgentState);

                return OrchestratorResult.builder()
                        .success(true)
                        .delegatedTo(targetAgent)
                        .subAgentResponse(resultState.getFinalResponse())
                        .message(resultState.getFinalResponse())
                        .toolResults(resultState.getToolResultStrings())
                        .build();
            } else {
                String cleanResponse = llmResponse.replaceAll("<delegate>.*?</delegate>", "").trim();
                return OrchestratorResult.builder()
                        .success(true)
                        .message(cleanResponse.isEmpty() ? llmResponse : cleanResponse)
                        .build();
            }

        } catch (Exception e) {
            log.error("Orchestrator 多轮处理失败", e);
            return OrchestratorResult.builder()
                    .success(false)
                    .message("抱歉，系统处理出现问题。")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * 编排结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrchestratorResult {
        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 最终消息
         */
        private String message;

        /**
         * 委托给的子Agent（如果有）
         */
        private String delegatedTo;

        /**
         * 子Agent的响应
         */
        private String subAgentResponse;

        /**
         * 工具执行结果
         */
        private List<String> toolResults;

        /**
         * 错误信息
         */
        private String error;
    }
}
