package com.fba.logi.agent.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agent 配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfig {

    /**
     * Agent 类型
     */
    private String agentType;

    /**
     * Agent 名称
     */
    private String agentName;

    /**
     * Agent 描述
     */
    private String description;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 可用工具列表
     */
    private List<String> availableTools;

    /**
     * 是否启用 RAG
     */
    private boolean ragEnabled;

    /**
     * RAG 知识库集合名称
     */
    private String ragCollection;

    /**
     * 最大历史消息数
     */
    @Builder.Default
    private int maxHistoryMessages = 20;

    /**
     * 温度参数
     */
    @Builder.Default
    private double temperature = 0.7;

}
