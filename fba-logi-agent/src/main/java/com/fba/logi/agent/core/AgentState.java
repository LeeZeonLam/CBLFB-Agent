package com.fba.logi.agent.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 状态
 * 用于在 Agent 执行过程中传递和维护状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentState {

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 当前 Agent 类型
     */
    private String agentType;

    /**
     * 用户输入消息
     */
    private String userMessage;

    /**
     * 对话历史
     */
    @Builder.Default
    private List<ChatHistoryItem> chatHistory = new ArrayList<>();

    /**
     * 上下文变量
     */
    @Builder.Default
    private Map<String, Object> context = new HashMap<>();

    /**
     * 工具执行结果
     */
    @Builder.Default
    private List<ToolResult> toolResults = new ArrayList<>();

    /**
     * RAG 检索结果
     */
    @Builder.Default
    private List<String> ragResults = new ArrayList<>();

    /**
     * 最终响应
     */
    private String finalResponse;

    /**
     * 是否需要工具调用
     */
    private boolean requireToolCall;

    /**
     * 对话历史项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistoryItem {
        private String role;
        private String content;
        private Long timestamp;
    }

    /**
     * 工具执行结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolResult {
        private String toolName;
        private Object input;
        private Object output;
        private boolean success;
        private String errorMessage;
    }

    /**
     * 添加用户消息到历史
     */
    public void addUserMessage(String message) {
        chatHistory.add(ChatHistoryItem.builder()
                .role("user")
                .content(message)
                .timestamp(System.currentTimeMillis())
                .build());
    }

    /**
     * 添加助手消息到历史
     */
    public void addAssistantMessage(String message) {
        chatHistory.add(ChatHistoryItem.builder()
                .role("assistant")
                .content(message)
                .timestamp(System.currentTimeMillis())
                .build());
    }

    /**
     * 添加工具执行结果
     */
    public void addToolResult(String toolName, Object input, Object output, boolean success) {
        toolResults.add(ToolResult.builder()
                .toolName(toolName)
                .input(input)
                .output(output)
                .success(success)
                .build());
    }

    /**
     * 添加工具结果到对话历史
     */
    public void addToolResult(String resultText) {
        chatHistory.add(ChatHistoryItem.builder()
                .role("tool")
                .content(resultText)
                .timestamp(System.currentTimeMillis())
                .build());
    }

    /**
     * 获取上下文变量
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextValue(String key, Class<T> type) {
        return (T) context.get(key);
    }

    /**
     * 设置上下文变量
     */
    public void setContextValue(String key, Object value) {
        context.put(key, value);
    }

    /**
     * 获取所有上下文变量
     */
    public Map<String, Object> getContextVariables() {
        return context;
    }

    /**
     * 获取工具结果字符串列表
     */
    public List<String> getToolResultStrings() {
        List<String> results = new ArrayList<>();
        for (ToolResult tr : toolResults) {
            results.add(String.format("[%s] %s -> %s",
                    tr.isSuccess() ? "成功" : "失败",
                    tr.getToolName(),
                    tr.getOutput()));
        }
        return results;
    }

}
