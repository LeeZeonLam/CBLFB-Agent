package com.fba.logi.agent.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具调用解析器
 * 从 LLM 响应中解析工具调用意图
 */
@Slf4j
@Component
public class ToolCallParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 工具调用结构（标准 Function Calling 格式）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCall {
        private String id;
        private String name;
        private Map<String, Object> arguments;
    }

    /**
     * 解析结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParseResult {
        /**
         * 是否包含工具调用
         */
        private boolean hasToolCall;

        /**
         * 解析到的工具调用列表
         */
        @Builder.Default
        private List<ToolCall> toolCalls = new ArrayList<>();

        /**
         * 纯文本内容（不包含工具调用部分）
         */
        private String textContent;

        /**
         * 原始响应
         */
        private String rawResponse;
    }

    // 支持多种工具调用格式的正则表达式
    // 格式1: <tool_call>{"name": "xxx", "arguments": {...}}</tool_call>
    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile(
            "<tool_call>\\s*(\\{.*?\\})\\s*</tool_call>",
            Pattern.DOTALL
    );

    // 格式2: ```json\n{"tool": "xxx", "parameters": {...}}\n```
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile(
            "```(?:json)?\\s*\\n?(\\{[^`]*\"(?:tool|function|name)\"[^`]*\\})\\s*\\n?```",
            Pattern.DOTALL
    );

    // 格式3: [CALL:skill_name(param1=value1, param2=value2)]
    private static final Pattern CALL_PATTERN = Pattern.compile(
            "\\[CALL:(\\w+)\\(([^)]*?)\\)\\]"
    );

    /**
     * 解析 LLM 响应
     */
    public ParseResult parse(String response) {
        if (response == null || response.isEmpty()) {
            return ParseResult.builder()
                    .hasToolCall(false)
                    .textContent("")
                    .rawResponse(response)
                    .build();
        }

        List<ToolCall> toolCalls = new ArrayList<>();
        String textContent = response;

        // 尝试解析各种格式
        toolCalls.addAll(parseToolCallFormat(response));
        toolCalls.addAll(parseJsonBlockFormat(response));
        toolCalls.addAll(parseCallFormat(response));

        // 清理文本内容（移除工具调用标记）
        if (!toolCalls.isEmpty()) {
            textContent = cleanTextContent(response);
        }

        return ParseResult.builder()
                .hasToolCall(!toolCalls.isEmpty())
                .toolCalls(toolCalls)
                .textContent(textContent.trim())
                .rawResponse(response)
                .build();
    }

    /**
     * 解析 <tool_call> 格式
     */
    private List<ToolCall> parseToolCallFormat(String response) {
        List<ToolCall> calls = new ArrayList<>();
        Matcher matcher = TOOL_CALL_PATTERN.matcher(response);

        while (matcher.find()) {
            try {
                String json = matcher.group(1);
                JsonNode node = objectMapper.readTree(json);

                String name = getStringField(node, "name", "function", "tool");
                JsonNode argsNode = node.has("arguments") ? node.get("arguments") :
                        node.has("parameters") ? node.get("parameters") : null;

                Map<String, Object> arguments = new HashMap<>();
                if (argsNode != null && argsNode.isObject()) {
                    arguments = objectMapper.convertValue(argsNode, Map.class);
                }

                calls.add(ToolCall.builder()
                        .id(UUID.randomUUID().toString())
                        .name(name)
                        .arguments(arguments)
                        .build());

            } catch (JsonProcessingException e) {
                log.warn("解析 tool_call JSON 失败: {}", e.getMessage());
            }
        }

        return calls;
    }

    /**
     * 解析 JSON 代码块格式
     */
    private List<ToolCall> parseJsonBlockFormat(String response) {
        List<ToolCall> calls = new ArrayList<>();
        Matcher matcher = JSON_BLOCK_PATTERN.matcher(response);

        while (matcher.find()) {
            try {
                String json = matcher.group(1);
                JsonNode node = objectMapper.readTree(json);

                String name = getStringField(node, "tool", "function", "name");
                if (name == null) continue;

                JsonNode argsNode = node.has("parameters") ? node.get("parameters") :
                        node.has("arguments") ? node.get("arguments") : null;

                Map<String, Object> arguments = new HashMap<>();
                if (argsNode != null && argsNode.isObject()) {
                    arguments = objectMapper.convertValue(argsNode, Map.class);
                }

                calls.add(ToolCall.builder()
                        .id(UUID.randomUUID().toString())
                        .name(name)
                        .arguments(arguments)
                        .build());

            } catch (JsonProcessingException e) {
                log.warn("解析 JSON 代码块失败: {}", e.getMessage());
            }
        }

        return calls;
    }

    /**
     * 解析 [CALL:...] 格式
     */
    private List<ToolCall> parseCallFormat(String response) {
        List<ToolCall> calls = new ArrayList<>();
        Matcher matcher = CALL_PATTERN.matcher(response);

        while (matcher.find()) {
            String name = matcher.group(1);
            String paramsStr = matcher.group(2);

            Map<String, Object> arguments = parseSimpleParams(paramsStr);

            calls.add(ToolCall.builder()
                    .id(UUID.randomUUID().toString())
                    .name(name)
                    .arguments(arguments)
                    .build());
        }

        return calls;
    }

    /**
     * 解析简单参数格式 (key1=value1, key2=value2)
     */
    private Map<String, Object> parseSimpleParams(String paramsStr) {
        Map<String, Object> params = new HashMap<>();
        if (paramsStr == null || paramsStr.trim().isEmpty()) {
            return params;
        }

        String[] pairs = paramsStr.split(",");
        for (String pair : pairs) {
            String[] kv = pair.trim().split("=", 2);
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();

                // 尝试解析数字
                try {
                    if (value.contains(".")) {
                        params.put(key, Double.parseDouble(value));
                    } else {
                        params.put(key, Integer.parseInt(value));
                    }
                } catch (NumberFormatException e) {
                    // 去除引号
                    value = value.replaceAll("^['\"]|['\"]$", "");
                    params.put(key, value);
                }
            }
        }

        return params;
    }

    /**
     * 获取字符串字段（尝试多个字段名）
     */
    private String getStringField(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && node.get(fieldName).isTextual()) {
                return node.get(fieldName).asText();
            }
        }
        return null;
    }

    /**
     * 清理文本内容（移除工具调用标记）
     */
    private String cleanTextContent(String response) {
        String cleaned = response;
        cleaned = TOOL_CALL_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = JSON_BLOCK_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = CALL_PATTERN.matcher(cleaned).replaceAll("");
        return cleaned;
    }
}
