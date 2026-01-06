package com.fba.logi.infrastructure.adapter.llm;

import com.fba.logi.common.constants.Constants;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 智谱 GLM-4V 视觉模型客户端
 * 支持图文多模态理解和分析
 */
@Slf4j
@Component
public class ZhipuVisionClient implements IVisionLlmClient {

    @Value("${llm.zhipu.api-key:}")
    private String apiKey;

    @Value("${llm.zhipu.vision-model:glm-4v}")
    private String visionModel;

    private ZhipuAiChatModel visionChatModel;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("智谱 AI API Key 未配置，GLM-4V 视觉客户端将不可用");
            return;
        }

        // 构建视觉模型客户端
        this.visionChatModel = ZhipuAiChatModel.builder()
                .apiKey(apiKey)
                .model(visionModel)
                .temperature(0.7)
                .maxToken(4096)
                .build();

        log.info("智谱 GLM-4V 视觉客户端初始化完成，模型: {}", visionModel);
    }

    @Override
    public String getProvider() {
        return Constants.LlmProvider.ZHIPU + "_vision";
    }

    @Override
    public String chatWithImages(String text, List<String> imagesBase64) {
        if (visionChatModel == null) {
            throw new IllegalStateException("GLM-4V 客户端未初始化，请检查 API Key 配置");
        }

        // 构建多模态消息内容
        List<Content> contents = new ArrayList<>();

        // 添加图片内容（Base64 格式）
        for (String imageBase64 : imagesBase64) {
            // 判断是否已包含 data:image 前缀
            String base64Data = imageBase64;
            if (!imageBase64.startsWith("data:image")) {
                // 默认添加 JPEG 格式前缀
                base64Data = "data:image/jpeg;base64," + imageBase64;
            }
            contents.add(ImageContent.from(base64Data));
        }

        // 添加文本内容
        contents.add(TextContent.from(text));

        // 构建用户消息
        UserMessage userMessage = UserMessage.from(contents);

        // 发送请求
        ChatRequest request = ChatRequest.builder()
                .messages(userMessage)
                .build();

        try {
            ChatResponse response = visionChatModel.chat(request);
            String result = response.aiMessage().text();
            log.debug("GLM-4V 图文对话完成，图片数量: {}", imagesBase64.size());
            return result;
        } catch (Exception e) {
            log.error("GLM-4V 调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("GLM-4V 调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String chatWithImageUrl(String text, String imageUrl) {
        return chatWithImageUrls(text, List.of(imageUrl));
    }

    @Override
    public String chatWithImageUrls(String text, List<String> imageUrls) {
        if (visionChatModel == null) {
            throw new IllegalStateException("GLM-4V 客户端未初始化，请检查 API Key 配置");
        }

        // 构建多模态消息内容
        List<Content> contents = new ArrayList<>();

        // 添加图片内容（URL 格式）
        for (String imageUrl : imageUrls) {
            contents.add(ImageContent.from(imageUrl));
        }

        // 添加文本内容
        contents.add(TextContent.from(text));

        // 构建用户消息
        UserMessage userMessage = UserMessage.from(contents);

        // 发送请求
        ChatRequest request = ChatRequest.builder()
                .messages(userMessage)
                .build();

        try {
            ChatResponse response = visionChatModel.chat(request);
            String result = response.aiMessage().text();
            log.debug("GLM-4V URL 图文对话完成，图片数量: {}", imageUrls.size());
            return result;
        } catch (Exception e) {
            log.error("GLM-4V 调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("GLM-4V 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查客户端是否可用
     */
    public boolean isAvailable() {
        return visionChatModel != null;
    }

}
