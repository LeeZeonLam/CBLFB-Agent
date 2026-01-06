package com.fba.logi.infrastructure.adapter.llm;

import java.util.List;

/**
 * 视觉 LLM 客户端接口
 * 支持图文多模态理解（如智谱 GLM-4V）
 */
public interface IVisionLlmClient {

    /**
     * 获取提供商名称
     *
     * @return 提供商名称
     */
    String getProvider();

    /**
     * 图文多模态对话（多张图片）
     *
     * @param text   文本提示
     * @param imagesBase64 图片 Base64 编码列表
     * @return 模型回复
     */
    String chatWithImages(String text, List<String> imagesBase64);

    /**
     * 图文多模态对话（单张图片）
     *
     * @param text        文本提示
     * @param imageBase64 图片 Base64 编码
     * @return 模型回复
     */
    default String chatWithImage(String text, String imageBase64) {
        return chatWithImages(text, List.of(imageBase64));
    }

    /**
     * 使用图片 URL 进行对话
     *
     * @param text     文本提示
     * @param imageUrl 图片 URL
     * @return 模型回复
     */
    String chatWithImageUrl(String text, String imageUrl);

    /**
     * 使用多个图片 URL 进行对话
     *
     * @param text      文本提示
     * @param imageUrls 图片 URL 列表
     * @return 模型回复
     */
    String chatWithImageUrls(String text, List<String> imageUrls);

}
