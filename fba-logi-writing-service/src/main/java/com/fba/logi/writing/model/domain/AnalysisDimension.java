package com.fba.logi.writing.model.domain;

import lombok.Getter;

/**
 * 写作分析维度枚举
 */
@Getter
public enum AnalysisDimension {

    /**
     * 语法拼写
     */
    GRAMMAR_SPELLING("语法拼写", "检查语法错误、拼写错误和标点使用", 0.25),

    /**
     * 风格可读性
     */
    STYLE_READABILITY("风格可读性", "评估文章风格、可读性和句式多样性", 0.20),

    /**
     * 内容逻辑
     */
    CONTENT_LOGIC("内容逻辑", "检查论证逻辑、结构完整性和内容连贯性", 0.25),

    /**
     * 词汇丰富度
     */
    VOCABULARY("词汇丰富度", "评估词汇使用的准确性和多样性", 0.15),

    /**
     * 文章结构
     */
    STRUCTURE("文章结构", "评估开头、正文、结尾的组织结构", 0.15);

    /**
     * 显示名称
     */
    private final String displayName;

    /**
     * 描述
     */
    private final String description;

    /**
     * 默认权重
     */
    private final double defaultWeight;

    AnalysisDimension(String displayName, String description, double defaultWeight) {
        this.displayName = displayName;
        this.description = description;
        this.defaultWeight = defaultWeight;
    }

    /**
     * 根据名称获取枚举值
     */
    public static AnalysisDimension fromName(String name) {
        for (AnalysisDimension dimension : values()) {
            if (dimension.name().equalsIgnoreCase(name) ||
                dimension.displayName.equals(name)) {
                return dimension;
            }
        }
        return null;
    }

}
