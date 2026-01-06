package com.fba.logi.agent.core;

import com.fba.logi.agent.skill.SkillContext;
import com.fba.logi.agent.skill.SkillExecutor;
import com.fba.logi.agent.skill.SkillRegistry;
import com.fba.logi.agent.skill.SkillResult;
import com.fba.logi.agent.skill.ISkill;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.infrastructure.adapter.llm.ILlmClient;
import com.fba.logi.infrastructure.adapter.llm.LlmClientFactory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agent 执行器
 * 负责执行 Agent 的核心逻辑，支持 Tool Calling
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentExecutor {

    private final LlmClientFactory llmClientFactory;
    private final AgentRouter agentRouter;
    private final ToolCallParser toolCallParser;
    private final SkillExecutor skillExecutor;
    private final SkillRegistry skillRegistry;

    /**
     * 最大工具调用轮次（防止无限循环）
     */
    private static final int MAX_TOOL_CALL_ROUNDS = 5;

    /**
     * 执行 Agent
     *
     * @param state Agent 状态
     * @return 执行后的状态
     */
    public AgentState execute(AgentState state) {
        log.info("开始执行 Agent，类型: {}，会话: {}", state.getAgentType(), state.getSessionId());

        try {
            // 1. 获取 Agent 配置
            AgentConfig config = agentRouter.getAgentConfig(state.getAgentType());

            // 2. 构建带工具定义的系统提示词
            String systemPromptWithTools = buildSystemPromptWithTools(config);

            // 3. 执行对话循环（支持多轮工具调用）
            int round = 0;
            while (round < MAX_TOOL_CALL_ROUNDS) {
                round++;
                log.debug("执行第 {} 轮对话", round);

                // 构建消息列表
                List<ChatMessage> messages = buildMessages(state, config, systemPromptWithTools);

                // 调用 LLM（使用完整的消息列表）
                ILlmClient llmClient = llmClientFactory.getChatClient();
                dev.langchain4j.model.chat.response.ChatResponse llmResponse = llmClient.chat(messages);
                String response = llmResponse.aiMessage().text();

                // 解析工具调用
                ToolCallParser.ParseResult parseResult = toolCallParser.parse(response);

                if (parseResult.isHasToolCall()) {
                    // 执行工具调用
                    List<SkillResult> toolResults = executeToolCalls(state, parseResult.getToolCalls());

                    // 将工具执行结果添加到上下文
                    String toolResultsText = formatToolResults(toolResults);
                    state.addAssistantMessage(parseResult.getTextContent());
                    state.addToolResult(toolResultsText);

                    // 添加工具结果作为新的用户消息，让LLM继续处理
                    state.setUserMessage("[工具执行结果]\n" + toolResultsText);

                    log.info("第 {} 轮执行了 {} 个工具", round, parseResult.getToolCalls().size());
                } else {
                    // 没有工具调用，返回最终响应
                    state.setFinalResponse(response);
                    state.addAssistantMessage(response);
                    break;
                }
            }

            if (round >= MAX_TOOL_CALL_ROUNDS) {
                log.warn("达到最大工具调用轮次限制: {}", MAX_TOOL_CALL_ROUNDS);
                state.setFinalResponse("处理完成，但由于复杂度较高，部分操作可能需要进一步确认。");
            }

            log.info("Agent 执行完成，会话: {}，共 {} 轮", state.getSessionId(), round);
            return state;

        } catch (Exception e) {
            log.error("Agent 执行失败", e);
            state.setFinalResponse("抱歉，处理您的请求时出现了问题，请稍后重试。");
            return state;
        }
    }

    /**
     * 执行工具调用
     */
    private List<SkillResult> executeToolCalls(AgentState state, List<ToolCallParser.ToolCall> toolCalls) {
        SkillContext context = SkillContext.fromAgentState(state);
        List<SkillResult> results = new ArrayList<>();

        for (ToolCallParser.ToolCall call : toolCalls) {
            log.info("执行工具: {} 参数: {}", call.getName(), call.getArguments());
            SkillResult result = skillExecutor.execute(call.getName(), context, call.getArguments());
            results.add(result);

            // 将结果记录到状态
            state.addToolResult(call.getName(), call.getArguments(), result.getMessage(), result.isSuccess());
        }

        return results;
    }

    /**
     * 格式化工具执行结果
     */
    private String formatToolResults(List<SkillResult> results) {
        return results.stream()
                .map(SkillResult::toPromptString)
                .collect(Collectors.joining("\n---\n"));
    }

    /**
     * 构建带工具定义的系统提示词
     */
    private String buildSystemPromptWithTools(AgentConfig config) {
        StringBuilder prompt = new StringBuilder(config.getSystemPrompt());

        List<ISkill> skills = skillRegistry.getSkillsForAgent(config.getAgentType());
        if (!skills.isEmpty()) {
            prompt.append("\n\n## 可用工具\n");
            prompt.append("当你需要执行操作时，使用以下格式调用工具：\n");
            prompt.append("<tool_call>{\"name\": \"工具名\", \"arguments\": {参数}}</tool_call>\n\n");

            for (ISkill skill : skills) {
                prompt.append("### ").append(skill.getSkillName()).append("\n");
                prompt.append("- ID: `").append(skill.getSkillId()).append("`\n");
                prompt.append("- 描述: ").append(skill.getDescription()).append("\n");
                prompt.append("- 参数: ").append(formatParameters(skill)).append("\n\n");
            }
        }

        return prompt.toString();
    }

    /**
     * 格式化参数说明
     */
    private String formatParameters(ISkill skill) {
        var schema = skill.getParameterSchema();
        if (schema.getParameters().isEmpty()) {
            return "无";
        }
        return schema.getParameters().stream()
                .map(p -> p.getName() + " (" + p.getType() + "): " + p.getDescription())
                .collect(Collectors.joining(", "));
    }

    /**
     * 流式执行 Agent
     *
     * @param state   Agent 状态
     * @param handler 响应处理器
     */
    public void executeStream(AgentState state, ILlmClient.StreamResponseHandler handler) {
        log.info("开始流式执行 Agent，类型: {}，会话: {}", state.getAgentType(), state.getSessionId());

        try {
            // 1. 获取 Agent 配置
            AgentConfig config = agentRouter.getAgentConfig(state.getAgentType());

            // 2. 构建消息列表
            List<ChatMessage> messages = buildMessages(state, config);

            // 3. 流式调用 LLM
            ILlmClient llmClient = llmClientFactory.getChatClient();
            StringBuilder responseBuilder = new StringBuilder();

            llmClient.streamChat(messages, new ILlmClient.StreamResponseHandler() {
                @Override
                public void onToken(String token) {
                    responseBuilder.append(token);
                    handler.onToken(token);
                }

                @Override
                public void onComplete() {
                    state.setFinalResponse(responseBuilder.toString());
                    state.addAssistantMessage(responseBuilder.toString());
                    handler.onComplete();
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error("流式执行失败", throwable);
                    handler.onError(throwable);
                }
            });

        } catch (Exception e) {
            log.error("流式执行失败", e);
            handler.onError(e);
        }
    }

    /**
     * 构建消息列表
     */
    private List<ChatMessage> buildMessages(AgentState state, AgentConfig config) {
        return buildMessages(state, config, config.getSystemPrompt());
    }

    /**
     * 构建消息列表（使用指定的系统提示词）
     */
    private List<ChatMessage> buildMessages(AgentState state, AgentConfig config, String systemPrompt) {
        List<ChatMessage> messages = new ArrayList<>();

        // 添加系统提示词
        messages.add(SystemMessage.from(systemPrompt));

        // 添加历史消息
        for (AgentState.ChatHistoryItem item : state.getChatHistory()) {
            if ("user".equals(item.getRole())) {
                messages.add(UserMessage.from(item.getContent()));
            } else if ("assistant".equals(item.getRole())) {
                messages.add(AiMessage.from(item.getContent()));
            } else if ("tool".equals(item.getRole())) {
                // 工具结果作为用户消息添加
                messages.add(UserMessage.from("[工具结果] " + item.getContent()));
            }
        }

        // 添加当前用户消息
        if (state.getUserMessage() != null) {
            messages.add(UserMessage.from(state.getUserMessage()));
            state.addUserMessage(state.getUserMessage());
        }

        return messages;
    }

}
