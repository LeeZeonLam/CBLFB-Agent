package com.fba.logi.infrastructure.adapter.llm;

import com.fba.logi.common.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM 客户端工厂
 * 根据配置或请求动态选择 LLM 提供商
 */
@Slf4j
@Component
public class LlmClientFactory {

    @Value("${llm.provider:deepseek}")
    private String defaultProvider;

    @Resource
    private List<ILlmClient> llmClients;

    private Map<String, ILlmClient> clientMap;

    /**
     * 初始化客户端映射
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        clientMap = new HashMap<>();
        for (ILlmClient client : llmClients) {
            clientMap.put(client.getProvider(), client);
            log.info("注册 LLM 客户端: {}", client.getProvider());
        }
    }

    /**
     * 获取默认 LLM 客户端
     *
     * @return LLM 客户端
     */
    public ILlmClient getClient() {
        return getClient(defaultProvider);
    }

    /**
     * 获取指定提供商的 LLM 客户端
     *
     * @param provider 提供商名称
     * @return LLM 客户端
     */
    public ILlmClient getClient(String provider) {
        ILlmClient client = clientMap.get(provider);
        if (client == null) {
            log.warn("未找到 LLM 客户端: {}，使用默认客户端: {}", provider, defaultProvider);
            client = clientMap.get(defaultProvider);
        }
        if (client == null) {
            throw new IllegalStateException("无可用的 LLM 客户端，请检查配置");
        }
        return client;
    }

    /**
     * 获取聊天客户端（优先 DeepSeek）
     */
    public ILlmClient getChatClient() {
        // 优先使用 DeepSeek 作为聊天模型
        ILlmClient client = clientMap.get(Constants.LlmProvider.DEEPSEEK);
        if (client != null) {
            return client;
        }
        // 降级到智谱
        client = clientMap.get(Constants.LlmProvider.ZHIPU);
        if (client != null) {
            return client;
        }
        return getClient();
    }

    /**
     * 获取 Embedding 客户端（优先智谱）
     */
    public ILlmClient getEmbeddingClient() {
        // 优先使用智谱作为 Embedding 模型
        ILlmClient client = clientMap.get(Constants.LlmProvider.ZHIPU);
        if (client != null) {
            return client;
        }
        return getClient();
    }

    /**
     * 检查提供商是否可用
     */
    public boolean isProviderAvailable(String provider) {
        return clientMap.containsKey(provider);
    }

}
