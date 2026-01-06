package com.fba.logi.writing.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 写作分析请求（用于多模态分析）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingAnalysisRequest {

    /**
     * 文本内容（可选）
     */
    @Size(max = 50000, message = "文本内容不能超过50000字符")
    private String text;

    /**
     * 图片 Base64 列表（可选）
     */
    private List<String> imagesBase64;

    /**
     * 分析维度（可选，默认全部）
     */
    private List<String> dimensions;

    /**
     * 是否返回详细报告
     */
    @Builder.Default
    private Boolean detailedReport = true;

    /**
     * 目标语言（用于分析标准判定）
     */
    @Builder.Default
    private String targetLanguage = "zh";

}
