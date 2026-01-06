package com.fba.logi.writing.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fba.logi.infrastructure.adapter.llm.ILlmClient;
import com.fba.logi.infrastructure.adapter.llm.IVisionLlmClient;
import com.fba.logi.infrastructure.adapter.llm.LlmClientFactory;
import com.fba.logi.infrastructure.adapter.llm.VisionLlmClientFactory;
import com.fba.logi.infrastructure.adapter.parser.DocumentParser;
import com.fba.logi.writing.model.domain.*;
import com.fba.logi.writing.model.request.WritingAnalysisRequest;
import com.fba.logi.writing.service.IWritingAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 写作分析服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WritingAnalysisServiceImpl implements IWritingAnalysisService {

    private final LlmClientFactory llmClientFactory;
    private final VisionLlmClientFactory visionLlmClientFactory;
    private final DocumentParser documentParser;
    private final ObjectMapper objectMapper;

    /**
     * 写作分析提示词模板
     */
    private static final String ANALYSIS_PROMPT_TEMPLATE = """
            你是一位专业的写作评估专家。请对以下文本进行全面的写作分析。

            待分析文本：
            ---
            %s
            ---

            请从以下维度进行分析：
            1. 语法拼写（GRAMMAR_SPELLING）：检查语法错误、拼写错误和标点使用
            2. 风格可读性（STYLE_READABILITY）：评估文章风格、可读性和句式多样性
            3. 内容逻辑（CONTENT_LOGIC）：检查论证逻辑、结构完整性和内容连贯性
            4. 词汇丰富度（VOCABULARY）：评估词汇使用的准确性和多样性
            5. 文章结构（STRUCTURE）：评估开头、正文、结尾的组织结构

            请严格按照以下 JSON 格式返回分析结果（只返回 JSON，不要其他内容）：
            ```json
            {
                "overallScore": 85,
                "dimensions": [
                    {
                        "name": "GRAMMAR_SPELLING",
                        "score": 90,
                        "comment": "语法使用规范，标点正确",
                        "issues": ["第3段存在一处错别字"]
                    },
                    {
                        "name": "STYLE_READABILITY",
                        "score": 82,
                        "comment": "行文流畅，可读性好",
                        "issues": []
                    },
                    {
                        "name": "CONTENT_LOGIC",
                        "score": 88,
                        "comment": "论证有力，逻辑清晰",
                        "issues": []
                    },
                    {
                        "name": "VOCABULARY",
                        "score": 80,
                        "comment": "词汇使用恰当",
                        "issues": ["部分词汇重复使用"]
                    },
                    {
                        "name": "STRUCTURE",
                        "score": 85,
                        "comment": "结构完整",
                        "issues": []
                    }
                ],
                "strengths": ["观点明确", "论据充分"],
                "suggestions": [
                    {
                        "dimension": "GRAMMAR_SPELLING",
                        "priority": "MEDIUM",
                        "original": "原文片段",
                        "suggested": "建议修改",
                        "explanation": "修改理由"
                    }
                ],
                "summary": "总体评价文字"
            }
            ```
            """;

    /**
     * 图片 OCR + 分析提示词
     */
    private static final String IMAGE_ANALYSIS_PROMPT = """
            你是一位专业的写作评估专家。请完成以下任务：

            1. 首先，识别图片中的所有文字内容
            2. 然后，对识别出的文字进行全面的写作分析

            分析维度：
            - 语法拼写（GRAMMAR_SPELLING）：检查语法错误、拼写错误和标点使用
            - 风格可读性（STYLE_READABILITY）：评估文章风格、可读性和句式多样性
            - 内容逻辑（CONTENT_LOGIC）：检查论证逻辑、结构完整性和内容连贯性
            - 词汇丰富度（VOCABULARY）：评估词汇使用的准确性和多样性
            - 文章结构（STRUCTURE）：评估开头、正文、结尾的组织结构

            请严格按照以下 JSON 格式返回（只返回 JSON，不要其他内容）：
            ```json
            {
                "recognizedText": "识别出的完整文字内容",
                "overallScore": 85,
                "dimensions": [
                    {"name": "GRAMMAR_SPELLING", "score": 90, "comment": "评语", "issues": []}
                ],
                "strengths": ["优点1", "优点2"],
                "suggestions": [
                    {"dimension": "GRAMMAR_SPELLING", "priority": "MEDIUM", "original": "原文", "suggested": "建议", "explanation": "说明"}
                ],
                "summary": "总体评价"
            }
            ```
            """;

    @Override
    public AnalysisResult analyzeText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("文本内容不能为空");
        }

        if (text.length() > 50000) {
            throw new IllegalArgumentException("文本内容过长，最大支持50000字符");
        }

        log.info("开始分析文本，长度: {} 字符", text.length());

        // 构建分析提示词
        String prompt = String.format(ANALYSIS_PROMPT_TEMPLATE, text);

        // 调用 LLM 进行分析
        ILlmClient llmClient = llmClientFactory.getChatClient();
        String response = llmClient.chat(prompt);

        // 解析结果
        AnalysisResult result = parseAnalysisResult(response, text, "TEXT");

        log.info("文本分析完成，综合评分: {}", result.getOverallScore());
        return result;
    }

    @Override
    public AnalysisResult analyzeImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("图片文件不能为空");
        }

        try {
            // 转换为 Base64
            byte[] bytes = imageFile.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            log.info("开始分析图片，文件名: {}，大小: {} KB",
                    imageFile.getOriginalFilename(),
                    bytes.length / 1024);

            return analyzeImageBase64(base64);
        } catch (IOException e) {
            log.error("读取图片文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("读取图片文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public AnalysisResult analyzeImageBase64(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new IllegalArgumentException("图片数据不能为空");
        }

        log.info("开始分析 Base64 图片");

        // 调用视觉模型进行 OCR + 分析
        IVisionLlmClient visionClient = visionLlmClientFactory.getClient();
        String response = visionClient.chatWithImage(IMAGE_ANALYSIS_PROMPT, imageBase64);

        // 解析结果
        AnalysisResult result = parseImageAnalysisResult(response);

        log.info("图片分析完成，综合评分: {}", result.getOverallScore());
        return result;
    }

    @Override
    public AnalysisResult analyzePdf(MultipartFile pdfFile) {
        if (pdfFile == null || pdfFile.isEmpty()) {
            throw new IllegalArgumentException("PDF 文件不能为空");
        }

        try {
            // 保存临时文件
            File tempFile = File.createTempFile("writing_", "_" + pdfFile.getOriginalFilename());
            pdfFile.transferTo(tempFile);

            log.info("开始分析 PDF，文件名: {}", pdfFile.getOriginalFilename());

            // 解析 PDF 内容
            String content = documentParser.parsePdf(tempFile);

            // 删除临时文件
            tempFile.delete();

            if (content == null || content.isBlank()) {
                throw new RuntimeException("PDF 文件内容为空或无法解析");
            }

            // 使用文本分析
            AnalysisResult result = analyzeText(content);
            result.setContentType("PDF");
            result.setOriginalContent(content);

            return result;
        } catch (IOException e) {
            log.error("PDF 解析失败: {}", e.getMessage(), e);
            throw new RuntimeException("PDF 解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public AnalysisResult analyzeMultimodal(WritingAnalysisRequest request) {
        StringBuilder combinedContent = new StringBuilder();
        String contentType = "MIXED";

        // 处理文本
        if (request.getText() != null && !request.getText().isBlank()) {
            combinedContent.append(request.getText());
        }

        // 处理图片（OCR 提取文字）
        if (request.getImagesBase64() != null && !request.getImagesBase64().isEmpty()) {
            for (String imageBase64 : request.getImagesBase64()) {
                try {
                    AnalysisResult imageResult = analyzeImageBase64(imageBase64);
                    if (imageResult.getOriginalContent() != null) {
                        if (combinedContent.length() > 0) {
                            combinedContent.append("\n\n");
                        }
                        combinedContent.append(imageResult.getOriginalContent());
                    }
                } catch (Exception e) {
                    log.warn("图片分析失败，跳过: {}", e.getMessage());
                }
            }
        }

        if (combinedContent.length() == 0) {
            throw new IllegalArgumentException("没有有效的内容可供分析");
        }

        // 分析合并后的内容
        AnalysisResult result = analyzeText(combinedContent.toString());
        result.setContentType(contentType);

        return result;
    }

    /**
     * 解析 LLM 返回的分析结果
     */
    private AnalysisResult parseAnalysisResult(String response, String originalContent, String contentType) {
        try {
            // 提取 JSON 内容
            String jsonContent = extractJsonFromResponse(response);
            JsonNode root = objectMapper.readTree(jsonContent);

            // 构建分析结果
            AnalysisResult result = AnalysisResult.builder()
                    .analysisId(UUID.randomUUID().toString())
                    .originalContent(originalContent)
                    .contentType(contentType)
                    .overallScore(root.path("overallScore").asInt(0))
                    .analysisTime(LocalDateTime.now())
                    .build();

            // 解析维度评分
            List<DimensionScore> dimensionScores = new ArrayList<>();
            JsonNode dimensions = root.path("dimensions");
            if (dimensions.isArray()) {
                for (JsonNode dim : dimensions) {
                    AnalysisDimension dimension = AnalysisDimension.fromName(dim.path("name").asText());
                    if (dimension != null) {
                        List<String> issues = new ArrayList<>();
                        dim.path("issues").forEach(issue -> issues.add(issue.asText()));

                        dimensionScores.add(DimensionScore.builder()
                                .dimension(dimension)
                                .score(dim.path("score").asInt(0))
                                .comment(dim.path("comment").asText(""))
                                .issues(issues)
                                .build());
                    }
                }
            }
            result.setDimensionScores(dimensionScores);

            // 解析优点
            List<String> strengths = new ArrayList<>();
            root.path("strengths").forEach(s -> strengths.add(s.asText()));
            result.setStrengths(strengths);

            // 解析改进建议
            List<ImprovementSuggestion> suggestions = new ArrayList<>();
            JsonNode suggestionsNode = root.path("suggestions");
            if (suggestionsNode.isArray()) {
                for (JsonNode sug : suggestionsNode) {
                    AnalysisDimension dim = AnalysisDimension.fromName(sug.path("dimension").asText());
                    suggestions.add(ImprovementSuggestion.builder()
                            .dimension(dim)
                            .priority(sug.path("priority").asText("MEDIUM"))
                            .originalText(sug.path("original").asText(""))
                            .suggestedText(sug.path("suggested").asText(""))
                            .explanation(sug.path("explanation").asText(""))
                            .build());
                }
            }
            result.setSuggestions(suggestions);

            // 生成详细报告
            String summary = root.path("summary").asText("");
            result.setDetailedReport(generateDetailedReport(result, summary));

            return result;

        } catch (JsonProcessingException e) {
            log.error("解析分析结果失败: {}", e.getMessage(), e);
            // 返回默认结果
            return createDefaultResult(originalContent, contentType, response);
        }
    }

    /**
     * 解析图片分析结果（包含 OCR 文字）
     */
    private AnalysisResult parseImageAnalysisResult(String response) {
        try {
            String jsonContent = extractJsonFromResponse(response);
            JsonNode root = objectMapper.readTree(jsonContent);

            // 获取识别的文字
            String recognizedText = root.path("recognizedText").asText("");

            // 使用通用解析方法
            AnalysisResult result = parseAnalysisResult(response, recognizedText, "IMAGE");
            result.setOriginalContent(recognizedText);

            return result;

        } catch (Exception e) {
            log.error("解析图片分析结果失败: {}", e.getMessage(), e);
            return createDefaultResult("", "IMAGE", response);
        }
    }

    /**
     * 从响应中提取 JSON 内容
     */
    private String extractJsonFromResponse(String response) {
        // 尝试提取 ```json ... ``` 块
        int jsonStart = response.indexOf("```json");
        int jsonEnd = response.lastIndexOf("```");

        if (jsonStart != -1 && jsonEnd > jsonStart) {
            return response.substring(jsonStart + 7, jsonEnd).trim();
        }

        // 尝试提取 { ... } 块
        int braceStart = response.indexOf("{");
        int braceEnd = response.lastIndexOf("}");

        if (braceStart != -1 && braceEnd > braceStart) {
            return response.substring(braceStart, braceEnd + 1);
        }

        return response;
    }

    /**
     * 创建默认结果（解析失败时使用）
     */
    private AnalysisResult createDefaultResult(String content, String contentType, String rawResponse) {
        return AnalysisResult.builder()
                .analysisId(UUID.randomUUID().toString())
                .originalContent(content)
                .contentType(contentType)
                .overallScore(0)
                .dimensionScores(new ArrayList<>())
                .suggestions(new ArrayList<>())
                .strengths(new ArrayList<>())
                .analysisTime(LocalDateTime.now())
                .detailedReport("分析结果解析失败，原始响应：\n" + rawResponse)
                .build();
    }

    /**
     * 生成详细报告（Markdown 格式）
     */
    private String generateDetailedReport(AnalysisResult result, String summary) {
        StringBuilder report = new StringBuilder();

        report.append("# 写作分析报告\n\n");
        report.append("**分析时间**: ").append(result.getAnalysisTime()).append("\n\n");

        // 综合评分
        report.append("## 综合评分\n\n");
        report.append("**评分**: ").append(result.getOverallScore()).append("/100 (")
                .append(result.getGrade()).append(")\n\n");

        if (summary != null && !summary.isEmpty()) {
            report.append("**总评**: ").append(summary).append("\n\n");
        }

        // 各维度评分
        report.append("## 维度评分\n\n");
        report.append("| 维度 | 评分 | 等级 | 评语 |\n");
        report.append("|------|------|------|------|\n");

        if (result.getDimensionScores() != null) {
            for (DimensionScore score : result.getDimensionScores()) {
                report.append("| ").append(score.getDimensionName())
                        .append(" | ").append(score.getScore())
                        .append(" | ").append(score.getGrade())
                        .append(" | ").append(score.getComment())
                        .append(" |\n");
            }
        }
        report.append("\n");

        // 优点
        if (result.getStrengths() != null && !result.getStrengths().isEmpty()) {
            report.append("## 优点\n\n");
            for (String strength : result.getStrengths()) {
                report.append("- ").append(strength).append("\n");
            }
            report.append("\n");
        }

        // 改进建议
        if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
            report.append("## 改进建议\n\n");
            for (ImprovementSuggestion suggestion : result.getSuggestions()) {
                report.append("### [").append(suggestion.getPriority()).append("] ")
                        .append(suggestion.getDimensionName()).append("\n\n");

                if (suggestion.getOriginalText() != null && !suggestion.getOriginalText().isEmpty()) {
                    report.append("**原文**: ").append(suggestion.getOriginalText()).append("\n\n");
                }
                if (suggestion.getSuggestedText() != null && !suggestion.getSuggestedText().isEmpty()) {
                    report.append("**建议**: ").append(suggestion.getSuggestedText()).append("\n\n");
                }
                if (suggestion.getExplanation() != null && !suggestion.getExplanation().isEmpty()) {
                    report.append("**说明**: ").append(suggestion.getExplanation()).append("\n\n");
                }
            }
        }

        return report.toString();
    }

}
