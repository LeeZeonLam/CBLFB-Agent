package com.fba.logi.writing.model.response;

import com.fba.logi.writing.model.domain.DimensionScore;
import com.fba.logi.writing.model.domain.ImprovementSuggestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 写作分析响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingAnalysisResponse {

    /**
     * 分析ID
     */
    private String analysisId;

    /**
     * 综合评分（0-100）
     */
    private Integer overallScore;

    /**
     * 等级评定（A/B/C/D/E）
     */
    private String grade;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 各维度评分
     */
    private List<DimensionScore> dimensionScores;

    /**
     * 改进建议
     */
    private List<ImprovementSuggestion> suggestions;

    /**
     * 优点列表
     */
    private List<String> strengths;

    /**
     * 详细报告（Markdown格式）
     */
    private String detailedReport;

    /**
     * 处理时间（毫秒）
     */
    private Long processingTime;

}
