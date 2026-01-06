package com.fba.logi.writing.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 改进建议
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementSuggestion {

    /**
     * 所属维度
     */
    private AnalysisDimension dimension;

    /**
     * 优先级（HIGH/MEDIUM/LOW）
     */
    private String priority;

    /**
     * 原文片段
     */
    private String originalText;

    /**
     * 建议修改
     */
    private String suggestedText;

    /**
     * 修改说明
     */
    private String explanation;

    /**
     * 开始位置（可选）
     */
    private Integer startPosition;

    /**
     * 结束位置（可选）
     */
    private Integer endPosition;

    /**
     * 获取维度显示名称
     */
    public String getDimensionName() {
        return dimension != null ? dimension.getDisplayName() : null;
    }

    /**
     * 判断是否为高优先级
     */
    public boolean isHighPriority() {
        return "HIGH".equalsIgnoreCase(priority);
    }

}
