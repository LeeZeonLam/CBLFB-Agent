package com.fba.logi.rag.controller;

import com.fba.logi.common.response.Response;
import com.fba.logi.infrastructure.milvus.MilvusService.SearchResult;
import com.fba.logi.rag.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * RAG 检索增强生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
@Tag(name = "RAG 服务", description = "检索增强生成相关接口")
public class RagController {

    private final RagService ragService;

    /**
     * 初始化知识库集合
     */
    @PostMapping("/init")
    @Operation(summary = "初始化知识库", description = "创建所有知识库集合")
    public Response<Void> initCollections() {
        ragService.initCollections();
        return Response.success();
    }

    /**
     * 上传并索引文档
     */
    @PostMapping("/index/upload")
    @Operation(summary = "上传文档", description = "上传文档并索引到指定集合")
    public Response<IndexResult> uploadAndIndex(
            @RequestParam("collection") String collectionName,
            @RequestParam("file") MultipartFile file) {

        try {
            // 保存临时文件
            File tempFile = File.createTempFile("rag_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile);

            // 索引文档
            int count = ragService.indexDocument(collectionName, tempFile);

            // 删除临时文件
            tempFile.delete();

            IndexResult result = new IndexResult();
            result.setCollection(collectionName);
            result.setFileName(file.getOriginalFilename());
            result.setChunkCount(count);

            return Response.success(result);
        } catch (Exception e) {
            log.error("文档索引失败: {}", e.getMessage(), e);
            return Response.fail("文档索引失败: " + e.getMessage());
        }
    }

    /**
     * 索引文本内容
     */
    @PostMapping("/index/text")
    @Operation(summary = "索引文本", description = "将文本内容索引到指定集合")
    public Response<IndexResult> indexText(@Valid @RequestBody IndexTextRequest request) {
        int count = ragService.indexText(
                request.getCollection(),
                request.getContent(),
                request.getSource()
        );

        IndexResult result = new IndexResult();
        result.setCollection(request.getCollection());
        result.setFileName(request.getSource());
        result.setChunkCount(count);

        return Response.success(result);
    }

    /**
     * 检索相关文档
     */
    @PostMapping("/retrieve")
    @Operation(summary = "检索文档", description = "根据查询检索相关文档")
    public Response<List<SearchResult>> retrieve(@Valid @RequestBody RetrieveRequest request) {
        int topK = request.getTopK() != null ? request.getTopK() : 5;
        List<SearchResult> results = ragService.retrieve(
                request.getCollection(),
                request.getQuery(),
                topK
        );
        return Response.success(results);
    }

    /**
     * 多集合检索
     */
    @PostMapping("/retrieve/multiple")
    @Operation(summary = "多集合检索", description = "从多个集合检索相关文档")
    public Response<List<SearchResult>> retrieveMultiple(@Valid @RequestBody RetrieveMultipleRequest request) {
        int topK = request.getTopK() != null ? request.getTopK() : 5;
        List<SearchResult> results = ragService.retrieveFromMultiple(
                request.getCollections(),
                request.getQuery(),
                topK
        );
        return Response.success(results);
    }

    /**
     * RAG 问答
     */
    @PostMapping("/query")
    @Operation(summary = "RAG 问答", description = "基于检索的增强生成问答")
    public Response<QueryResult> query(@Valid @RequestBody QueryRequest request) {
        int topK = request.getTopK() != null ? request.getTopK() : 5;
        String answer = ragService.query(
                request.getCollection(),
                request.getQuery(),
                topK
        );

        QueryResult result = new QueryResult();
        result.setQuery(request.getQuery());
        result.setAnswer(answer);

        return Response.success(result);
    }

    /**
     * 多集合 RAG 问答
     */
    @PostMapping("/query/multiple")
    @Operation(summary = "多集合问答", description = "从多个集合检索后生成回答")
    public Response<QueryResult> queryMultiple(@Valid @RequestBody QueryMultipleRequest request) {
        int topK = request.getTopK() != null ? request.getTopK() : 5;
        String answer = ragService.queryMultiple(
                request.getCollections(),
                request.getQuery(),
                topK
        );

        QueryResult result = new QueryResult();
        result.setQuery(request.getQuery());
        result.setAnswer(answer);

        return Response.success(result);
    }

    /**
     * 删除集合
     */
    @DeleteMapping("/collection/{name}")
    @Operation(summary = "删除集合", description = "删除指定的知识库集合")
    public Response<Void> deleteCollection(@PathVariable("name") String collectionName) {
        ragService.deleteCollection(collectionName);
        return Response.success();
    }

    // ==================== 请求/响应 DTO ====================

    @Data
    public static class IndexTextRequest {
        @NotBlank(message = "集合名称不能为空")
        private String collection;

        @NotBlank(message = "文本内容不能为空")
        private String content;

        private String source = "manual_input";
    }

    @Data
    public static class RetrieveRequest {
        @NotBlank(message = "集合名称不能为空")
        private String collection;

        @NotBlank(message = "查询内容不能为空")
        private String query;

        private Integer topK;
    }

    @Data
    public static class RetrieveMultipleRequest {
        private List<String> collections;

        @NotBlank(message = "查询内容不能为空")
        private String query;

        private Integer topK;
    }

    @Data
    public static class QueryRequest {
        @NotBlank(message = "集合名称不能为空")
        private String collection;

        @NotBlank(message = "问题不能为空")
        private String query;

        private Integer topK;
    }

    @Data
    public static class QueryMultipleRequest {
        private List<String> collections;

        @NotBlank(message = "问题不能为空")
        private String query;

        private Integer topK;
    }

    @Data
    public static class IndexResult {
        private String collection;
        private String fileName;
        private int chunkCount;
    }

    @Data
    public static class QueryResult {
        private String query;
        private String answer;
    }

}
