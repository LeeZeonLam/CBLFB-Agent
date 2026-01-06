package com.fba.logi.infrastructure.adapter.llm;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视觉 LLM 客户端工厂
 * 管理和获取视觉模型客户端
 */
@Slf4j
@Component
public class VisionLlmClientFactory {

    @Value("${llm.vision.provider:zhipu_vision}")
    private String defaultProvider;

    @Resource
    private List<IVisionLlmClient> visionClients;

    private Map<String, IVisionLlmClient> clientMap;

    @PostConstruct
    public void init() {
        clientMap = new HashMap<>();
        for (IVisionLlmClient client : visionClients) {
            clientMap.put(client.getProvider(), client);
            log.info("注册视觉 LLM 客户端: {}", client.getProvider());
        }

        if (clientMap.isEmpty()) {
            log.warn("没有可用的视觉 LLM 客户端");
        }
    }

    /**
     * 获取默认视觉客户端
     *
     * @return 视觉 LLM 客户端
     */
    public IVisionLlmClient getClient() {
        IVisionLlmClient client = clientMap.get(defaultProvider);
        if (client != null) {
            return client;
        }

        // 如果默认提供商不可用，尝试获取任意可用客户端
        if (!clientMap.isEmpty()) {
            return clientMap.values().iterator().next();
        }

        throw new IllegalStateException("没有可用的视觉 LLM 客户端");
    }

    /**
     * 获取指定提供商的视觉客户端
     *
     * @param provider 提供商名称
     * @return 视觉 LLM 客户端
     */
    public IVisionLlmClient getClient(String provider) {
        IVisionLlmClient client = clientMap.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("不支持的视觉 LLM 提供商: " + provider);
        }
        return client;
    }

    /**
     * 检查是否有可用的视觉客户端
     *
     * @return 是否有可用客户端
     */
    public boolean hasAvailableClient() {
        return !clientMap.isEmpty();
    }

    /**
     * 获取所有已注册的提供商
     *
     * @return 提供商名称列表
     */
    public List<String> getAvailableProviders() {
        return List.copyOf(clientMap.keySet());
    }

}
