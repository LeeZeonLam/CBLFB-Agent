package com.fba.logi.agent.skill.writing;

import com.fba.logi.agent.skill.*;
import com.fba.logi.infrastructure.adapter.llm.ILlmClient;
import com.fba.logi.infrastructure.adapter.llm.LlmClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 写作分析 Skill
 * 对用户提供的文本进行写作质量分析
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzeWritingSkill extends AbstractSkill {

    private final LlmClientFactory llmClientFactory;

    /**
     * 写作分析提示词模板
     */
    private static final String ANALYSIS_PROMPT = """
            你是一位专业的写作评估专家。请对以下文本进行全面的写作分析。

            待分析文本：
            ---
            %s
            ---

            请从以下维度进行分析：
            1. 语法拼写：检查语法错误、拼写错误和标点使用
            2. 风格可读性：评估文章风格、可读性和句式多样性
            3. 内容逻辑：检查论证逻辑、结构完整性和内容连贯性
            4. 词汇丰富度：评估词汇使用的准确性和多样性
            5. 文章结构：评估开头、正文、结尾的组织结构

            请返回以下格式的分析结果：

            ## 综合评分
            [0-100分，并给出等级 A/B/C/D/E]

            ## 各维度评分
            - 语法拼写：[分数] - [简短评语]
            - 风格可读性：[分数] - [简短评语]
            - 内容逻辑：[分数] - [简短评语]
            - 词汇丰富度：[分数] - [简短评语]
            - 文章结构：[分数] - [简短评语]

            ## 优点
            [列出 2-3 个主要优点]

            ## 改进建议
            [列出 3-5 条具体可操作的改进建议，按优先级排序]

            ## 总结
            [一段简短的总体评价]
            """;

    @Override
    public String getSkillId() {
        return "analyze_writing";
    }

    @Override
    public String getSkillName() {
        return "写作分析";
    }

    @Override
    public String getDescription() {
        return "分析文本的写作质量，包括语法拼写、风格可读性、内容逻辑、词汇丰富度、文章结构等多个维度，提供评分和改进建议";
    }

    @Override
    public String getDomain() {
        return "writing";
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "content", "待分析的文本内容")
                ))
                .required(List.of("content"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String content = getRequiredString(parameters, "content");

        // 内容验证
        if (content.length() > 50000) {
            return SkillResult.failure("文本内容过长，最大支持50000字符", "CONTENT_TOO_LONG");
        }

        if (content.length() < 10) {
            return SkillResult.failure("文本内容过短，至少需要10个字符", "CONTENT_TOO_SHORT");
        }

        try {
            log.info("开始写作分析，内容长度: {} 字符", content.length());

            // 构建分析提示词
            String prompt = String.format(ANALYSIS_PROMPT, content);

            // 调用 LLM 进行分析
            ILlmClient llmClient = llmClientFactory.getChatClient();
            String analysisResult = llmClient.chat(prompt);

            log.info("写作分析完成");

            return SkillResult.success(analysisResult, Map.of(
                    "contentLength", content.length(),
                    "analysisType", "full"
            ));

        } catch (Exception e) {
            log.error("写作分析失败", e);
            return SkillResult.failure("写作分析失败: " + e.getMessage(), "ANALYSIS_ERROR");
        }
    }

}
