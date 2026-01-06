package com.fba.logi.agent.subagent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-Agent 配置
 * 定义子Agent的元信息，供主Agent路由使用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubAgentConfig {

    /**
     * Agent 类型标识
     */
    private String agentType;

    /**
     * Agent 名称（中文）
     */
    private String agentName;

    /**
     * Agent 职责描述（供主Agent理解）
     */
    private String responsibility;

    /**
     * 适用场景列表
     */
    @Builder.Default
    private List<String> applicableScenarios = new ArrayList<>();

    /**
     * 所属领域
     */
    private String domain;

    /**
     * 优先级（数字越小优先级越高）
     */
    @Builder.Default
    private int priority = 100;

    /**
     * 是否启用
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 是否可以被委托调用
     */
    @Builder.Default
    private boolean delegatable = true;
}
