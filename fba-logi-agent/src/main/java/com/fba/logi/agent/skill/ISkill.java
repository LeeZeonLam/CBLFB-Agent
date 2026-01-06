package com.fba.logi.agent.skill;

import java.util.Map;

/**
 * 技能接口 - 所有 Skill 的基础抽象
 *
 * Skill 是 Agent 可调用的原子能力单元，每个 Skill 封装一个具体的业务操作。
 * Agent 通过 LLM 的 Function Calling 机制选择并调用合适的 Skill。
 */
public interface ISkill {

    /**
     * 获取技能唯一标识
     * @return 技能ID（如 "create_campaign", "query_shipping_rate"）
     */
    String getSkillId();

    /**
     * 获取技能名称（用于展示）
     * @return 技能名称
     */
    String getSkillName();

    /**
     * 获取技能描述（供 LLM 理解技能用途）
     * @return 技能描述
     */
    String getDescription();

    /**
     * 获取技能参数定义（JSON Schema 格式）
     * 这将用于构建 LLM 的 Function Calling 参数
     * @return 参数 Schema 定义
     */
    SkillParameterSchema getParameterSchema();

    /**
     * 执行技能
     * @param context 执行上下文（包含会话信息、用户信息等）
     * @param parameters LLM 传入的参数
     * @return 执行结果
     */
    SkillResult execute(SkillContext context, Map<String, Object> parameters);

    /**
     * 验证参数是否有效
     * @param parameters 待验证的参数
     * @return 验证结果
     */
    default SkillValidationResult validateParameters(Map<String, Object> parameters) {
        return SkillValidationResult.success();
    }

    /**
     * 获取技能所属领域
     * @return 领域标识
     */
    default String getDomain() {
        return "general";
    }

    /**
     * 是否需要用户确认才能执行
     * @return true 表示执行前需要确认
     */
    default boolean requiresConfirmation() {
        return false;
    }
}
