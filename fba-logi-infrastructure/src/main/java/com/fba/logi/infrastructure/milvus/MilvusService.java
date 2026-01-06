package com.fba.logi.infrastructure.milvus;

import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Milvus 向量存储服务
 * 提供向量的 CRUD 和相似度搜索功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MilvusService {

    private final MilvusServiceClient milvusClient;

    /**
     * 向量维度（智谱 embedding-2 模型输出 1024 维）
     */
    private static final int VECTOR_DIM = 1024;

    /**
     * 默认 Top K
     */
    private static final int DEFAULT_TOP_K = 5;

    /**
     * 创建集合
     *
     * @param collectionName 集合名称
     */
    public void createCollection(String collectionName) {
        if (milvusClient == null) {
            log.warn("Milvus 客户端未初始化，跳过创建集合");
            return;
        }

        // 检查集合是否存在
        R<Boolean> hasResult = milvusClient.hasCollection(
                HasCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );

        if (hasResult.getData()) {
            log.info("集合已存在: {}", collectionName);
            return;
        }

        // 定义字段
        List<FieldType> fieldTypes = Arrays.asList(
                FieldType.newBuilder()
                        .withName("id")
                        .withDataType(DataType.VarChar)
                        .withMaxLength(128)
                        .withPrimaryKey(true)
                        .withAutoID(false)
                        .build(),
                FieldType.newBuilder()
                        .withName("content")
                        .withDataType(DataType.VarChar)
                        .withMaxLength(65535)
                        .build(),
                FieldType.newBuilder()
                        .withName("metadata")
                        .withDataType(DataType.VarChar)
                        .withMaxLength(4096)
                        .build(),
                FieldType.newBuilder()
                        .withName("embedding")
                        .withDataType(DataType.FloatVector)
                        .withDimension(VECTOR_DIM)
                        .build()
        );

        // 创建集合
        CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("FBA LogiAI 知识库 - " + collectionName)
                .withFieldTypes(fieldTypes)
                .build();

        R<RpcStatus> createResult = milvusClient.createCollection(createParam);
        if (createResult.getStatus() != R.Status.Success.getCode()) {
            log.error("创建集合失败: {}", createResult.getMessage());
            throw new RuntimeException("创建集合失败: " + createResult.getMessage());
        }

        // 创建索引
        createIndex(collectionName);
        log.info("集合创建成功: {}", collectionName);
    }

    /**
     * 创建向量索引
     */
    private void createIndex(String collectionName) {
        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName("embedding")
                .withIndexType(IndexType.HNSW)
                .withMetricType(MetricType.COSINE)
                .withExtraParam("{\"M\": 16, \"efConstruction\": 256}")
                .build();

        R<RpcStatus> indexResult = milvusClient.createIndex(indexParam);
        if (indexResult.getStatus() != R.Status.Success.getCode()) {
            log.error("创建索引失败: {}", indexResult.getMessage());
            throw new RuntimeException("创建索引失败: " + indexResult.getMessage());
        }
        log.info("索引创建成功: {}", collectionName);
    }

    /**
     * 加载集合到内存
     */
    public void loadCollection(String collectionName) {
        if (milvusClient == null) {
            return;
        }

        R<RpcStatus> loadResult = milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );

        if (loadResult.getStatus() != R.Status.Success.getCode()) {
            log.error("加载集合失败: {}", loadResult.getMessage());
            throw new RuntimeException("加载集合失败: " + loadResult.getMessage());
        }
        log.info("集合加载成功: {}", collectionName);
    }

    /**
     * 插入向量数据
     *
     * @param collectionName 集合名称
     * @param documents      文档列表
     */
    public void insertDocuments(String collectionName, List<DocumentVector> documents) {
        if (milvusClient == null || documents.isEmpty()) {
            return;
        }

        List<String> ids = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        List<String> metadataList = new ArrayList<>();
        List<List<Float>> embeddings = new ArrayList<>();

        for (DocumentVector doc : documents) {
            ids.add(doc.getId());
            contents.add(doc.getContent());
            metadataList.add(doc.getMetadata());
            embeddings.add(toFloatList(doc.getEmbedding()));
        }

        List<InsertParam.Field> fields = Arrays.asList(
                new InsertParam.Field("id", ids),
                new InsertParam.Field("content", contents),
                new InsertParam.Field("metadata", metadataList),
                new InsertParam.Field("embedding", embeddings)
        );

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();

        R<MutationResult> insertResult = milvusClient.insert(insertParam);
        if (insertResult.getStatus() != R.Status.Success.getCode()) {
            log.error("插入数据失败: {}", insertResult.getMessage());
            throw new RuntimeException("插入数据失败: " + insertResult.getMessage());
        }
        log.info("成功插入 {} 条文档到集合 {}", documents.size(), collectionName);
    }

    /**
     * 相似度搜索
     *
     * @param collectionName 集合名称
     * @param queryVector    查询向量
     * @param topK           返回数量
     * @return 相似文档列表
     */
    public List<SearchResult> search(String collectionName, float[] queryVector, int topK) {
        if (milvusClient == null) {
            return Collections.emptyList();
        }

        // 确保集合已加载
        loadCollection(collectionName);

        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .withMetricType(MetricType.COSINE)
                .withOutFields(Arrays.asList("id", "content", "metadata"))
                .withTopK(topK)
                .withVectors(Collections.singletonList(toFloatList(queryVector)))
                .withVectorFieldName("embedding")
                .withParams("{\"ef\": 128}")
                .build();

        R<SearchResults> searchResult = milvusClient.search(searchParam);
        if (searchResult.getStatus() != R.Status.Success.getCode()) {
            log.error("搜索失败: {}", searchResult.getMessage());
            throw new RuntimeException("搜索失败: " + searchResult.getMessage());
        }

        SearchResultsWrapper wrapper = new SearchResultsWrapper(searchResult.getData().getResults());
        List<SearchResult> results = new ArrayList<>();

        for (int i = 0; i < wrapper.getRowRecords(0).size(); i++) {
            SearchResultsWrapper.IDScore idScore = wrapper.getIDScore(0).get(i);
            String id = idScore.getStrID();
            float score = idScore.getScore();

            Object contentObj = wrapper.getRowRecords(0).get(i).get("content");
            Object metadataObj = wrapper.getRowRecords(0).get(i).get("metadata");

            String content = contentObj != null ? contentObj.toString() : "";
            String metadata = metadataObj != null ? metadataObj.toString() : "";

            results.add(new SearchResult(id, content, metadata, score));
        }

        return results;
    }

    /**
     * 使用默认 Top K 搜索
     */
    public List<SearchResult> search(String collectionName, float[] queryVector) {
        return search(collectionName, queryVector, DEFAULT_TOP_K);
    }

    /**
     * 删除集合
     */
    public void dropCollection(String collectionName) {
        if (milvusClient == null) {
            return;
        }

        R<RpcStatus> dropResult = milvusClient.dropCollection(
                DropCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );

        if (dropResult.getStatus() != R.Status.Success.getCode()) {
            log.error("删除集合失败: {}", dropResult.getMessage());
            throw new RuntimeException("删除集合失败: " + dropResult.getMessage());
        }
        log.info("集合删除成功: {}", collectionName);
    }

    /**
     * float[] 转 List<Float>
     */
    private List<Float> toFloatList(float[] array) {
        List<Float> list = new ArrayList<>(array.length);
        for (float v : array) {
            list.add(v);
        }
        return list;
    }

    /**
     * 文档向量数据
     */
    public record DocumentVector(
            String id,
            String content,
            String metadata,
            float[] embedding
    ) {
        public String getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public String getMetadata() {
            return metadata;
        }

        public float[] getEmbedding() {
            return embedding;
        }
    }

    /**
     * 搜索结果
     */
    public record SearchResult(
            String id,
            String content,
            String metadata,
            float score
    ) {}

}
