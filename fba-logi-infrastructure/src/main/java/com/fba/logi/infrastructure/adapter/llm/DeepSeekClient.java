package com.fba.logi.infrastructure.adapter.llm;

import com.fba.logi.common.constants.Constants;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * DeepSeek LLM 客户端
 * 使用 OpenAI 兼容接口调用 DeepSeek API
 */
@Slf4j
@Component
public class DeepSeekClient implements ILlmClient {

    @Value("${llm.deepseek.api-key:}")
    private String apiKey;

    @Value("${llm.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${llm.deepseek.model:deepseek-chat}")
    private String model;

    private OpenAiChatModel chatModel;
    private OpenAiStreamingChatModel streamingChatModel;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("DeepSeek API Key 未配置，DeepSeek 客户端将不可用");
            return;
        }

        // 构建同步聊天模型
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(0.7)
                .maxTokens(4096)
                .build();

        // 构建流式聊天模型
        this.streamingChatModel = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(0.7)
                .maxTokens(4096)
                .build();

        log.info("DeepSeek 客户端初始化完成，模型: {}", model);
    }

    @Override
    public String getProvider() {
        return Constants.LlmProvider.DEEPSEEK;
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        if (chatModel == null) {
            throw new IllegalStateException("DeepSeek 客户端未初始化，请检查 API Key 配置");
        }
        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .build();
        return chatModel.chat(request);
    }

    @Override
    public String chat(String message) {
        if (chatModel == null) {
            throw new IllegalStateException("DeepSeek 客户端未初始化，请检查 API Key 配置");
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
            throw new IllegalStateException("DeepSeek 客户端未初始化，请检查 API Key 配置");
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
        // DeepSeek 目前不提供 Embedding API，使用智谱作为备选
        throw new UnsupportedOperationException("DeepSeek 暂不支持 Embedding，请使用智谱 GLM");
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        throw new UnsupportedOperationException("DeepSeek 暂不支持 Embedding，请使用智谱 GLM");
    }

}
