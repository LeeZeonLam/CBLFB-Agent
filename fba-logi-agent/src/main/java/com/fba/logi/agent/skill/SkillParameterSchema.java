package com.fba.logi.agent.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 技能参数 Schema 定义
 * 用于描述 Skill 接受的参数结构，将被转换为 LLM Function Calling 的参数定义
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillParameterSchema {

    /**
     * 参数列表
     */
    @Builder.Default
    private List<ParameterDefinition> parameters = new ArrayList<>();

    /**
     * 必填参数名列表
     */
    @Builder.Default
    private List<String> required = new ArrayList<>();

    /**
     * 添加参数定义
     */
    public SkillParameterSchema addParameter(ParameterDefinition param) {
        this.parameters.add(param);
        return this;
    }

    /**
     * 添加必填参数
     */
    public SkillParameterSchema addRequired(String paramName) {
        this.required.add(paramName);
        return this;
    }

    /**
     * 转换为 JSON Schema 格式（用于 Function Calling）
     */
    public Map<String, Object> toJsonSchema() {
        Map<String, Object> properties = new java.util.LinkedHashMap<>();
        for (ParameterDefinition param : parameters) {
            properties.put(param.getName(), param.toSchemaMap());
        }

        return Map.of(
                "type", "object",
                "properties", properties,
                "required", required
        );
    }

    /**
     * 创建空 Schema
     */
    public static SkillParameterSchema empty() {
        return new SkillParameterSchema();
    }

    /**
     * 参数定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterDefinition {
        /**
         * 参数名
         */
        private String name;

        /**
         * 参数类型 (string, number, integer, boolean, array, object)
         */
        private String type;

        /**
         * 参数描述
         */
        private String description;

        /**
         * 枚举值（可选）
         */
        private List<String> enumValues;

        /**
         * 默认值（可选）
         */
        private Object defaultValue;

        /**
         * 数组元素类型（当 type=array 时使用）
         */
        private String itemType;

        /**
         * 转换为 Schema Map
         */
        public Map<String, Object> toSchemaMap() {
            Map<String, Object> schema = new java.util.LinkedHashMap<>();
            schema.put("type", type);
            schema.put("description", description);

            if (enumValues != null && !enumValues.isEmpty()) {
                schema.put("enum", enumValues);
            }
            if (defaultValue != null) {
                schema.put("default", defaultValue);
            }
            if ("array".equals(type) && itemType != null) {
                schema.put("items", Map.of("type", itemType));
            }

            return schema;
        }

        /**
         * 快速创建字符串参数
         */
        public static ParameterDefinition stringParam(String name, String description) {
            return ParameterDefinition.builder()
                    .name(name)
                    .type("string")
                    .description(description)
                    .build();
        }

        /**
         * 快速创建数字参数
         */
        public static ParameterDefinition numberParam(String name, String description) {
            return ParameterDefinition.builder()
                    .name(name)
                    .type("number")
                    .description(description)
                    .build();
        }

        /**
         * 快速创建整数参数
         */
        public static ParameterDefinition integerParam(String name, String description) {
            return ParameterDefinition.builder()
                    .name(name)
                    .type("integer")
                    .description(description)
                    .build();
        }

        /**
         * 快速创建布尔参数
         */
        public static ParameterDefinition booleanParam(String name, String description) {
            return ParameterDefinition.builder()
                    .name(name)
                    .type("boolean")
                    .description(description)
                    .build();
        }

        /**
         * 快速创建枚举参数
         */
        public static ParameterDefinition enumParam(String name, String description, List<String> values) {
            return ParameterDefinition.builder()
                    .name(name)
                    .type("string")
                    .description(description)
                    .enumValues(values)
                    .build();
        }
    }
}
