package com.fba.logi.agent.skill;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Skill 抽象基类
 * 提供通用的参数验证和日志记录功能
 */
@Slf4j
public abstract class AbstractSkill implements ISkill {

    @Override
    public SkillResult execute(SkillContext context, Map<String, Object> parameters) {
        log.debug("执行 Skill: {} 参数: {}", getSkillId(), parameters);
        try {
            return doExecute(context, parameters);
        } catch (Exception e) {
            log.error("Skill {} 执行失败", getSkillId(), e);
            return SkillResult.failure("执行失败: " + e.getMessage(), "EXECUTION_ERROR");
        }
    }

    /**
     * 实际执行逻辑（由子类实现）
     */
    protected abstract SkillResult doExecute(SkillContext context, Map<String, Object> parameters);

    /**
     * 获取必填字符串参数
     */
    protected String getRequiredString(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value == null) {
            throw new IllegalArgumentException("缺少必填参数: " + key);
        }
        return value.toString();
    }

    /**
     * 获取可选字符串参数
     */
    protected String getOptionalString(Map<String, Object> parameters, String key, String defaultValue) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * 获取必填整数参数
     */
    protected int getRequiredInt(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value == null) {
            throw new IllegalArgumentException("缺少必填参数: " + key);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * 获取可选整数参数
     */
    protected int getOptionalInt(Map<String, Object> parameters, String key, int defaultValue) {
        Object value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * 获取必填浮点数参数
     */
    protected double getRequiredDouble(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value == null) {
            throw new IllegalArgumentException("缺少必填参数: " + key);
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    /**
     * 获取可选浮点数参数
     */
    protected double getOptionalDouble(Map<String, Object> parameters, String key, double defaultValue) {
        Object value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    /**
     * 获取布尔参数
     */
    protected boolean getBoolean(Map<String, Object> parameters, String key, boolean defaultValue) {
        Object value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
