package com.fba.logi.writing.service;

import com.fba.logi.writing.model.domain.AnalysisResult;
import com.fba.logi.writing.model.request.WritingAnalysisRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * 写作分析服务接口
 */
public interface IWritingAnalysisService {

    /**
     * 分析纯文本内容
     *
     * @param text 文本内容
     * @return 分析结果
     */
    AnalysisResult analyzeText(String text);

    /**
     * 分析图片内容（OCR + 写作分析）
     *
     * @param imageFile 图片文件
     * @return 分析结果
     */
    AnalysisResult analyzeImage(MultipartFile imageFile);

    /**
     * 分析图片内容（Base64 格式）
     *
     * @param imageBase64 图片 Base64 编码
     * @return 分析结果
     */
    AnalysisResult analyzeImageBase64(String imageBase64);

    /**
     * 分析 PDF 文档
     *
     * @param pdfFile PDF 文件
     * @return 分析结果
     */
    AnalysisResult analyzePdf(MultipartFile pdfFile);

    /**
     * 多模态分析（支持混合输入）
     *
     * @param request 分析请求（包含文本、图片等）
     * @return 分析结果
     */
    AnalysisResult analyzeMultimodal(WritingAnalysisRequest request);

}
