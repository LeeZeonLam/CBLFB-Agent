package com.fba.logi.infrastructure.adapter.llm;

import com.fba.logi.common.constants.Constants;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.model.zhipu.ZhipuAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智谱 AI LLM 客户端
 * 支持 GLM-4 系列模型和 Embedding
 */
@Slf4j
@Component
public class ZhipuClient implements ILlmClient {

    @Value("${llm.zhipu.api-key:}")
    private String apiKey;

    @Value("${llm.zhipu.model:glm-4-flash}")
    private String model;

    @Value("${llm.zhipu.embedding-model:embedding-2}")
    private String embeddingModel;

    private ZhipuAiChatModel chatModel;
    private ZhipuAiStreamingChatModel streamingChatModel;
    private ZhipuAiEmbeddingModel embeddingModelClient;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("智谱 AI API Key 未配置，智谱客户端将不可用");
            return;
        }

        // 构建同步聊天模型
        this.chatModel = ZhipuAiChatModel.builder()
                .apiKey(apiKey)
                .model(model)
                .temperature(0.7)
                .maxToken(4096)
                .build();

        // 构建流式聊天模型
        this.streamingChatModel = ZhipuAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .model(model)
                .temperature(0.7)
                .maxToken(4096)
                .build();

        // 构建 Embedding 模型
        this.embeddingModelClient = ZhipuAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .model(embeddingModel)
                .build();

        log.info("智谱 AI 客户端初始化完成，聊天模型: {}，Embedding 模型: {}", model, embeddingModel);
    }

    @Override
    public String getProvider() {
        return Constants.LlmProvider.ZHIPU;
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        if (chatModel == null) {
            throw new IllegalStateException("智谱 AI 客户端未初始化，请检查 API Key 配置");
        }
        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .build();
        return chatModel.chat(request);
    }

    @Override
    public String chat(String message) {
        if (chatModel == null) {
            throw new IllegalStateException("智谱 AI 客户端未初始化，请检查 API Key 配置");
        }
        ChatRequest request = ChatRequest.builder()
                .messages(dev.langchain4j.data.message.UserMessage.from(message))
                .build();
        ChatResponse response = chatModel.chat(request);
        return response.aiMessage().text();
    }

    @Override
    public void streamChat(List<ChatMessage> messages, StreamResponseHandler handler) {
        if (streamingChatModel == null) {
            throw new IllegalStateException("智谱 AI 客户端未初始化，请检查 API Key 配置");
        }

        StringBuilder responseBuilder = new StringBuilder();
        streamingChatModel.generate(messages, new dev.langchain4j.model.StreamingResponseHandler<dev.langchain4j.data.message.AiMessage>() {
            @Override
            public void onNext(String token) {
                responseBuilder.append(token);
                handler.onToken(token);
            }

            @Override
            public void onComplete(dev.langchain4j.model.output.Response<dev.langchain4j.data.message.AiMessage> response) {
                handler.onComplete();
            }

            @Override
            public void onError(Throwable error) {
                handler.onError(error);
            }
        });
    }

    @Override
    public float[] embed(String text) {
        if (embeddingModelClient == null) {
            throw new IllegalStateException("智谱 AI 客户端未初始化，请检查 API Key 配置");
        }
        Embedding embedding = embeddingModelClient.embed(text).content();
        return embedding.vector();
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        if (embeddingModelClient == null) {
            throw new IllegalStateException("智谱 AI 客户端未初始化，请检查 API Key 配置");
        }
        return embeddingModelClient.embedAll(texts.stream()
                        .map(dev.langchain4j.data.segment.TextSegment::from)
                        .collect(Collectors.toList()))
                .content()
                .stream()
                .map(Embedding::vector)
                .collect(Collectors.toList());
    }

}
