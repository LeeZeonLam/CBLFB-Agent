package com.fba.logi.infrastructure.adapter.llm;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.List;

/**
 * LLM 客户端接口
 * 统一抽象不同 LLM 提供商的调用
 */
public interface ILlmClient {

    /**
     * 获取提供商名称
     *
     * @return 提供商名称
     */
    String getProvider();

    /**
     * 发送聊天请求
     *
     * @param messages 消息列表
     * @return 响应
     */
    ChatResponse chat(List<ChatMessage> messages);

    /**
     * 发送聊天请求（单条消息）
     *
     * @param message 用户消息
     * @return 助手回复
     */
    String chat(String message);

    /**
     * 流式聊天（返回 token 流）
     *
     * @param messages 消息列表
     * @param handler  流式响应处理器
     */
    void streamChat(List<ChatMessage> messages, StreamResponseHandler handler);

    /**
     * 生成文本向量
     *
     * @param text 文本内容
     * @return 向量数组
     */
    float[] embed(String text);

    /**
     * 批量生成文本向量
     *
     * @param texts 文本列表
     * @return 向量数组列表
     */
    List<float[]> embedBatch(List<String> texts);

    /**
     * 流式响应处理器
     */
    interface StreamResponseHandler {
        /**
         * 接收 token
         */
        void onToken(String token);

        /**
         * 完成
         */
        void onComplete();

        /**
         * 错误
         */
        void onError(Throwable throwable);
    }

}
