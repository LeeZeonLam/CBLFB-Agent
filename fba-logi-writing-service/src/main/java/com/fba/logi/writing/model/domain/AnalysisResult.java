package com.fba.logi.writing.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 写作分析结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    /**
     * 分析ID
     */
    private String analysisId;

    /**
     * 原始内容（提取的文本）
     */
    private String originalContent;

    /**
     * 内容类型（TEXT/IMAGE/PDF/MIXED）
     */
    private String contentType;

    /**
     * 综合评分（0-100）
     */
    private Integer overallScore;

    /**
     * 各维度评分
     */
    private List<DimensionScore> dimensionScores;

    /**
     * 改进建议列表
     */
    private List<ImprovementSuggestion> suggestions;

    /**
     * 优点总结
     */
    private List<String> strengths;

    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;

    /**
     * 详细分析报告（Markdown格式）
     */
    private String detailedReport;

    /**
     * 获取评分等级
     */
    public String getGrade() {
        if (overallScore == null) return "N/A";
        if (overallScore >= 90) return "A";
        if (overallScore >= 80) return "B";
        if (overallScore >= 70) return "C";
        if (overallScore >= 60) return "D";
        return "E";
    }

    /**
     * 获取高优先级建议数量
     */
    public int getHighPrioritySuggestionCount() {
        if (suggestions == null) return 0;
        return (int) suggestions.stream()
                .filter(ImprovementSuggestion::isHighPriority)
                .count();
    }

}
