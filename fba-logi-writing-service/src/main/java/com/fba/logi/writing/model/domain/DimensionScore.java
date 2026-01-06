package com.fba.logi.writing.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 维度评分
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionScore {

    /**
     * 分析维度
     */
    private AnalysisDimension dimension;

    /**
     * 评分（0-100）
     */
    private Integer score;

    /**
     * 评语
     */
    private String comment;

    /**
     * 发现的问题列表
     */
    private List<String> issues;

    /**
     * 获取维度显示名称
     */
    public String getDimensionName() {
        return dimension != null ? dimension.getDisplayName() : null;
    }

    /**
     * 获取评分等级
     */
    public String getGrade() {
        if (score == null) return "N/A";
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "E";
    }

}
