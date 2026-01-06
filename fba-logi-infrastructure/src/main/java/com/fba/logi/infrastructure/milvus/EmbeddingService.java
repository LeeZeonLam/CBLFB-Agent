package com.fba.logi.infrastructure.milvus;

import com.fba.logi.infrastructure.adapter.llm.ILlmClient;
import com.fba.logi.infrastructure.adapter.llm.LlmClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文本向量化服务
 * 使用智谱 AI 的 Embedding 模型将文本转换为向量
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final LlmClientFactory llmClientFactory;

    /**
     * 将单条文本转换为向量
     *
     * @param text 文本内容
     * @return 向量数组
     */
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("文本内容不能为空");
        }

        ILlmClient embeddingClient = llmClientFactory.getEmbeddingClient();
        return embeddingClient.embed(text);
    }

    /**
     * 批量将文本转换为向量
     *
     * @param texts 文本列表
     * @return 向量数组列表
     */
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("文本列表不能为空");
        }

        ILlmClient embeddingClient = llmClientFactory.getEmbeddingClient();
        return embeddingClient.embedBatch(texts);
    }

    /**
     * 计算两个向量的余弦相似度
     *
     * @param v1 向量1
     * @param v2 向量2
     * @return 相似度 (0-1)
     */
    public float cosineSimilarity(float[] v1, float[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("向量维度不匹配");
        }

        float dotProduct = 0;
        float norm1 = 0;
        float norm2 = 0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }

        return (float) (dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2)));
    }

}
