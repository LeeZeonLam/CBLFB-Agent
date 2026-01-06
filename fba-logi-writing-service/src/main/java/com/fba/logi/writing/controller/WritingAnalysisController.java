package com.fba.logi.writing.controller;

import com.fba.logi.common.response.Response;
import com.fba.logi.writing.model.domain.AnalysisResult;
import com.fba.logi.writing.model.request.WritingAnalysisRequest;
import com.fba.logi.writing.model.response.WritingAnalysisResponse;
import com.fba.logi.writing.service.IWritingAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 多模态写作分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/writing")
@RequiredArgsConstructor
@Validated
@Tag(name = "写作分析服务", description = "多模态写作分析接口 - 支持文本、图片、PDF")
public class WritingAnalysisController {

    private final IWritingAnalysisService analysisService;

    /**
     * 分析纯文本
     */
    @PostMapping("/analyze/text")
    @Operation(summary = "分析文本", description = "分析纯文本内容的写作质量")
    public Response<WritingAnalysisResponse> analyzeText(
            @RequestBody @Valid TextAnalysisRequest request) {

        log.info("收到文本分析请求，长度: {} 字符", request.getText().length());
        long startTime = System.currentTimeMillis();

        AnalysisResult result = analysisService.analyzeText(request.getText());
        WritingAnalysisResponse response = convertToResponse(result, startTime);

        return Response.success(response);
    }

    /**
     * 分析图片
     */
    @PostMapping(value = "/analyze/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "分析图片", description = "OCR 识别图片中的文字并分析写作质量")
    public Response<WritingAnalysisResponse> analyzeImage(
            @Parameter(description = "图片文件（支持 JPG、PNG、GIF）")
            @RequestParam("file") MultipartFile file) {

        log.info("收到图片分析请求，文件名: {}", file.getOriginalFilename());
        long startTime = System.currentTimeMillis();

        AnalysisResult result = analysisService.analyzeImage(file);
        WritingAnalysisResponse response = convertToResponse(result, startTime);

        return Response.success(response);
    }

    /**
     * 分析图片（Base64 格式）
     */
    @PostMapping("/analyze/image/base64")
    @Operation(summary = "分析图片(Base64)", description = "分析 Base64 编码的图片内容")
    public Response<WritingAnalysisResponse> analyzeImageBase64(
            @RequestBody @Valid ImageBase64Request request) {

        log.info("收到 Base64 图片分析请求");
        long startTime = System.currentTimeMillis();

        AnalysisResult result = analysisService.analyzeImageBase64(request.getImageBase64());
        WritingAnalysisResponse response = convertToResponse(result, startTime);

        return Response.success(response);
    }

    /**
     * 分析 PDF
     */
    @PostMapping(value = "/analyze/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "分析PDF", description = "解析 PDF 文档并分析写作质量")
    public Response<WritingAnalysisResponse> analyzePdf(
            @Parameter(description = "PDF 文件")
            @RequestParam("file") MultipartFile file) {

        log.info("收到 PDF 分析请求，文件名: {}", file.getOriginalFilename());
        long startTime = System.currentTimeMillis();

        AnalysisResult result = analysisService.analyzePdf(file);
        WritingAnalysisResponse response = convertToResponse(result, startTime);

        return Response.success(response);
    }

    /**
     * 多模态分析
     */
    @PostMapping("/analyze/multimodal")
    @Operation(summary = "多模态分析", description = "支持文本、图片混合输入的综合分析")
    public Response<WritingAnalysisResponse> analyzeMultimodal(
            @RequestBody @Valid WritingAnalysisRequest request) {

        log.info("收到多模态分析请求");
        long startTime = System.currentTimeMillis();

        AnalysisResult result = analysisService.analyzeMultimodal(request);
        WritingAnalysisResponse response = convertToResponse(result, startTime);

        return Response.success(response);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务状态")
    public Response<Map<String, Object>> health() {
        return Response.success(Map.of(
                "status", "UP",
                "service", "writing-analysis-service",
                "version", "1.0.0"
        ));
    }

    /**
     * 转换为响应对象
     */
    private WritingAnalysisResponse convertToResponse(AnalysisResult result, long startTime) {
        return WritingAnalysisResponse.builder()
                .analysisId(result.getAnalysisId())
                .overallScore(result.getOverallScore())
                .grade(result.getGrade())
                .contentType(result.getContentType())
                .dimensionScores(result.getDimensionScores())
                .suggestions(result.getSuggestions())
                .strengths(result.getStrengths())
                .detailedReport(result.getDetailedReport())
                .processingTime(System.currentTimeMillis() - startTime)
                .build();
    }

    /**
     * 文本分析请求
     */
    @lombok.Data
    public static class TextAnalysisRequest {
        @NotBlank(message = "文本内容不能为空")
        private String text;
    }

    /**
     * 图片 Base64 分析请求
     */
    @lombok.Data
    public static class ImageBase64Request {
        @NotBlank(message = "图片 Base64 数据不能为空")
        private String imageBase64;
    }

}
