package com.fba.logi.agent.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能参数验证结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillValidationResult {

    /**
     * 是否验证通过
     */
    private boolean valid;

    /**
     * 错误信息列表
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /**
     * 创建成功结果
     */
    public static SkillValidationResult success() {
        return SkillValidationResult.builder()
                .valid(true)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static SkillValidationResult failure(String error) {
        return SkillValidationResult.builder()
                .valid(false)
                .errors(List.of(error))
                .build();
    }

    /**
     * 创建失败结果（多个错误）
     */
    public static SkillValidationResult failure(List<String> errors) {
        return SkillValidationResult.builder()
                .valid(false)
                .errors(errors)
                .build();
    }

    /**
     * 添加错误
     */
    public SkillValidationResult addError(String error) {
        this.errors.add(error);
        this.valid = false;
        return this;
    }

    /**
     * 获取所有错误的拼接字符串
     */
    public String getErrorMessage() {
        return String.join("; ", errors);
    }
}
