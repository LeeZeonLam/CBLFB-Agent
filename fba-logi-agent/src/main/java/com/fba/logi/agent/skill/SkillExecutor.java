package com.fba.logi.agent.skill;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * 技能执行器
 * 负责执行 Skill 并处理执行过程中的异常
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillExecutor {

    private final SkillRegistry skillRegistry;

    /**
     * 执行指定的 Skill
     *
     * @param skillId    技能ID
     * @param context    执行上下文
     * @param parameters 参数
     * @return 执行结果
     */
    public SkillResult execute(String skillId, SkillContext context, Map<String, Object> parameters) {
        log.info("开始执行 Skill: {}，会话: {}", skillId, context.getSessionId());

        // 1. 查找 Skill
        Optional<ISkill> skillOpt = skillRegistry.getSkill(skillId);
        if (skillOpt.isEmpty()) {
            log.error("Skill 不存在: {}", skillId);
            return SkillResult.failure("未找到技能: " + skillId, "SKILL_NOT_FOUND");
        }

        ISkill skill = skillOpt.get();

        // 2. 验证 Agent 权限
        if (context.getAgentType() != null && !skillRegistry.canAgentUseSkill(context.getAgentType(), skillId)) {
            log.warn("Agent '{}' 无权使用 Skill '{}'", context.getAgentType(), skillId);
            return SkillResult.failure("当前助手无权执行此操作", "PERMISSION_DENIED");
        }

        // 3. 验证参数
        SkillValidationResult validation = skill.validateParameters(parameters);
        if (!validation.isValid()) {
            log.warn("Skill '{}' 参数验证失败: {}", skillId, validation.getErrorMessage());
            return SkillResult.failure("参数验证失败: " + validation.getErrorMessage(), "INVALID_PARAMETERS");
        }

        // 4. 执行 Skill
        try {
            long startTime = System.currentTimeMillis();
            SkillResult result = skill.execute(context, parameters);
            long duration = System.currentTimeMillis() - startTime;

            log.info("Skill '{}' 执行完成，耗时: {}ms，成功: {}", skillId, duration, result.isSuccess());
            return result;

        } catch (Exception e) {
            log.error("Skill '{}' 执行异常", skillId, e);
            return SkillResult.failure("执行出错: " + e.getMessage(), "EXECUTION_ERROR");
        }
    }

    /**
     * 执行 Skill（带 Agent 权限检查）
     */
    public SkillResult executeForAgent(String agentType, String skillId, SkillContext context, Map<String, Object> parameters) {
        // 设置 Agent 类型
        context.setAgentType(agentType);
        return execute(skillId, context, parameters);
    }

    /**
     * 批量执行多个 Skill（顺序执行）
     */
    public Map<String, SkillResult> executeBatch(SkillContext context, Map<String, Map<String, Object>> skillCalls) {
        Map<String, SkillResult> results = new java.util.LinkedHashMap<>();

        for (Map.Entry<String, Map<String, Object>> entry : skillCalls.entrySet()) {
            String skillId = entry.getKey();
            Map<String, Object> parameters = entry.getValue();

            SkillResult result = execute(skillId, context, parameters);
            results.put(skillId, result);

            // 如果某个 Skill 失败，可以选择继续或中断
            if (!result.isSuccess()) {
                log.warn("批量执行中 Skill '{}' 失败，继续执行后续 Skill", skillId);
            }
        }

        return results;
    }

    /**
     * 获取 Skill 信息
     */
    public Optional<SkillInfo> getSkillInfo(String skillId) {
        return skillRegistry.getSkill(skillId)
                .map(skill -> SkillInfo.builder()
                        .skillId(skill.getSkillId())
                        .skillName(skill.getSkillName())
                        .description(skill.getDescription())
                        .domain(skill.getDomain())
                        .requiresConfirmation(skill.requiresConfirmation())
                        .parameterSchema(skill.getParameterSchema())
                        .build());
    }

    /**
     * Skill 信息 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class SkillInfo {
        private String skillId;
        private String skillName;
        private String description;
        private String domain;
        private boolean requiresConfirmation;
        private SkillParameterSchema parameterSchema;
    }
}
