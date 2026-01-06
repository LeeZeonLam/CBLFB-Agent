package com.fba.logi.agent.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 技能执行上下文
 * 包含执行 Skill 所需的环境信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillContext {

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
     * 扩展上下文变量
     */
    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();

    /**
     * RAG 检索结果（如果有）
     */
    private String ragContext;

    /**
     * 原始用户消息
     */
    private String userMessage;

    /**
     * 获取变量
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key) {
        return (T) variables.get(key);
    }

    /**
     * 设置变量
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * 检查变量是否存在
     */
    public boolean hasVariable(String key) {
        return variables.containsKey(key);
    }

    /**
     * 从 AgentState 创建上下文
     */
    public static SkillContext fromAgentState(com.fba.logi.agent.core.AgentState state) {
        return SkillContext.builder()
                .sessionId(state.getSessionId())
                .userId(state.getUserId())
                .agentType(state.getAgentType())
                .variables(new HashMap<>(state.getContextVariables()))
                .ragContext(String.join("\n", state.getRagResults()))
                .userMessage(state.getUserMessage())
                .build();
    }
}
