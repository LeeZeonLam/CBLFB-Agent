package com.fba.logi.agent.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 技能执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResult {

    /**
     * 是否执行成功
     */
    private boolean success;

    /**
     * 结果消息（供 LLM 理解的自然语言描述）
     */
    private String message;

    /**
     * 结构化数据（供程序处理）
     */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    /**
     * 错误代码（失败时）
     */
    private String errorCode;

    /**
     * 是否需要用户确认后续操作
     */
    private boolean requiresFollowUp;

    /**
     * 后续操作提示
     */
    private String followUpPrompt;

    /**
     * 建议调用的下一个 Skill（可选）
     */
    private String suggestedNextSkill;

    /**
     * 创建成功结果
     */
    public static SkillResult success(String message) {
        return SkillResult.builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * 创建成功结果（带数据）
     */
    public static SkillResult success(String message, Map<String, Object> data) {
        return SkillResult.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static SkillResult failure(String message, String errorCode) {
        return SkillResult.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }

    /**
     * 创建需要后续确认的结果
     */
    public static SkillResult needsConfirmation(String message, String followUpPrompt) {
        return SkillResult.builder()
                .success(true)
                .message(message)
                .requiresFollowUp(true)
                .followUpPrompt(followUpPrompt)
                .build();
    }

    /**
     * 添加数据项
     */
    public SkillResult withData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 设置建议的下一步 Skill
     */
    public SkillResult withNextSkill(String skillId) {
        this.suggestedNextSkill = skillId;
        return this;
    }

    /**
     * 转换为 LLM 可理解的字符串
     */
    public String toPromptString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Skill执行结果]\n");
        sb.append("状态: ").append(success ? "成功" : "失败").append("\n");
        sb.append("信息: ").append(message).append("\n");

        if (!data.isEmpty()) {
            sb.append("数据: ").append(data).append("\n");
        }
        if (!success && errorCode != null) {
            sb.append("错误代码: ").append(errorCode).append("\n");
        }
        if (requiresFollowUp) {
            sb.append("需要确认: ").append(followUpPrompt).append("\n");
        }

        return sb.toString();
    }
}
