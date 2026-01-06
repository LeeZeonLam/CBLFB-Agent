package com.fba.logi.rag.service;

import com.fba.logi.infrastructure.adapter.llm.ILlmClient;
import com.fba.logi.infrastructure.adapter.llm.LlmClientFactory;
import com.fba.logi.infrastructure.adapter.parser.DocumentParser;
import com.fba.logi.infrastructure.milvus.EmbeddingService;
import com.fba.logi.infrastructure.milvus.MilvusService;
import com.fba.logi.infrastructure.milvus.MilvusService.DocumentVector;
import com.fba.logi.infrastructure.milvus.MilvusService.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RAG 检索增强生成服务
 * 提供文档索引、向量检索和问答生成功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final MilvusService milvusService;
    private final EmbeddingService embeddingService;
    private final DocumentParser documentParser;
    private final LlmClientFactory llmClientFactory;

    /**
     * 知识库集合名称
     */
    public static final class Collections {
        public static final String CUSTOMS_TARIFF = "customs_tariff";       // 海关税则
        public static final String FBA_WAREHOUSE = "fba_warehouse";         // FBA 仓库代码
        public static final String PROHIBITED_ITEMS = "prohibited_items";   // 禁运品清单
        public static final String SHIPPING_GUIDE = "shipping_guide";       // 物流指南
        public static final String GENERAL_KB = "general_kb";               // 通用知识库
    }

    /**
     * 初始化知识库集合
     */
    public void initCollections() {
        milvusService.createCollection(Collections.CUSTOMS_TARIFF);
        milvusService.createCollection(Collections.FBA_WAREHOUSE);
        milvusService.createCollection(Collections.PROHIBITED_ITEMS);
        milvusService.createCollection(Collections.SHIPPING_GUIDE);
        milvusService.createCollection(Collections.GENERAL_KB);
        log.info("知识库集合初始化完成");
    }

    /**
     * 索引文档到指定集合
     *
     * @param collectionName 集合名称
     * @param file           文档文件
     * @return 索引的文档数量
     */
    public int indexDocument(String collectionName, File file) throws Exception {
        // 解析文档
        List<String> chunks = documentParser.parseAndSplit(file);
        if (chunks.isEmpty()) {
            log.warn("文档内容为空: {}", file.getName());
            return 0;
        }

        // 批量生成向量
        List<float[]> embeddings = embeddingService.embedBatch(chunks);

        // 构建文档向量
        List<DocumentVector> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String id = UUID.randomUUID().toString();
            String metadata = String.format("{\"source\":\"%s\",\"chunk\":%d}", file.getName(), i);
            documents.add(new DocumentVector(id, chunks.get(i), metadata, embeddings.get(i)));
        }

        // 插入到 Milvus
        milvusService.insertDocuments(collectionName, documents);
        log.info("成功索引文档 {} 到集合 {}，共 {} 个块", file.getName(), collectionName, documents.size());
        return documents.size();
    }

    /**
     * 索引文本内容
     *
     * @param collectionName 集合名称
     * @param content        文本内容
     * @param source         来源标识
     * @return 索引的文档数量
     */
    public int indexText(String collectionName, String content, String source) {
        List<String> chunks = documentParser.splitIntoChunks(content);
        if (chunks.isEmpty()) {
            return 0;
        }

        List<float[]> embeddings = embeddingService.embedBatch(chunks);
        List<DocumentVector> documents = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String id = UUID.randomUUID().toString();
            String metadata = String.format("{\"source\":\"%s\",\"chunk\":%d}", source, i);
            documents.add(new DocumentVector(id, chunks.get(i), metadata, embeddings.get(i)));
        }

        milvusService.insertDocuments(collectionName, documents);
        return documents.size();
    }

    /**
     * 检索相关文档
     *
     * @param collectionName 集合名称
     * @param query          查询文本
     * @param topK           返回数量
     * @return 相关文档列表
     */
    public List<SearchResult> retrieve(String collectionName, String query, int topK) {
        float[] queryVector = embeddingService.embed(query);
        return milvusService.search(collectionName, queryVector, topK);
    }

    /**
     * 从多个集合检索
     */
    public List<SearchResult> retrieveFromMultiple(List<String> collectionNames, String query, int topK) {
        float[] queryVector = embeddingService.embed(query);
        List<SearchResult> allResults = new ArrayList<>();

        for (String collection : collectionNames) {
            try {
                List<SearchResult> results = milvusService.search(collection, queryVector, topK);
                allResults.addAll(results);
            } catch (Exception e) {
                log.warn("从集合 {} 检索失败: {}", collection, e.getMessage());
            }
        }

        // 按相似度排序，取 TopK
        return allResults.stream()
                .sorted((a, b) -> Float.compare(b.score(), a.score()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * RAG 问答
     * 检索相关文档后使用 LLM 生成回答
     *
     * @param collectionName 集合名称
     * @param query          用户问题
     * @return 生成的回答
     */
    public String query(String collectionName, String query) {
        return query(collectionName, query, 5);
    }

    /**
     * RAG 问答（指定 TopK）
     */
    public String query(String collectionName, String query, int topK) {
        // 检索相关文档
        List<SearchResult> results = retrieve(collectionName, query, topK);

        if (results.isEmpty()) {
            return "抱歉，没有找到相关信息。";
        }

        // 构建上下文
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            context.append(String.format("[%d] %s\n\n", i + 1, results.get(i).content()));
        }

        // 构建提示词
        String prompt = String.format("""
                你是一个专业的跨境电商物流助手。请根据以下参考资料回答用户的问题。
                如果参考资料中没有相关信息，请诚实说明。

                参考资料：
                %s

                用户问题：%s

                请用中文回答：""", context, query);

        // 调用 LLM 生成回答
        ILlmClient llmClient = llmClientFactory.getChatClient();
        return llmClient.chat(prompt);
    }

    /**
     * 多集合 RAG 问答
     */
    public String queryMultiple(List<String> collectionNames, String query, int topK) {
        List<SearchResult> results = retrieveFromMultiple(collectionNames, query, topK);

        if (results.isEmpty()) {
            return "抱歉，没有找到相关信息。";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            context.append(String.format("[%d] %s\n\n", i + 1, results.get(i).content()));
        }

        String prompt = String.format("""
                你是一个专业的跨境电商物流助手。请根据以下参考资料回答用户的问题。
                如果参考资料中没有相关信息，请诚实说明。

                参考资料：
                %s

                用户问题：%s

                请用中文回答：""", context, query);

        ILlmClient llmClient = llmClientFactory.getChatClient();
        return llmClient.chat(prompt);
    }

    /**
     * 删除集合
     */
    public void deleteCollection(String collectionName) {
        milvusService.dropCollection(collectionName);
    }

}
