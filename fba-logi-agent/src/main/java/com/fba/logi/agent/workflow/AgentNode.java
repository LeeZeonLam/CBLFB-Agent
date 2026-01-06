package com.fba.logi.agent.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Agent 工作流节点
 * 代表状态图中的一个节点，可以是 Agent 调用、条件判断或自定义操作
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentNode {

    /**
     * 节点类型
     */
    public enum NodeType {
        /**
         * Agent 调用节点
         */
        AGENT,

        /**
         * 条件判断节点
         */
        CONDITION,

        /**
         * 工具调用节点
         */
        TOOL,

        /**
         * 自定义处理节点
         */
        CUSTOM,

        /**
         * 起始节点
         */
        START,

        /**
         * 结束节点
         */
        END
    }

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型
     */
    private NodeType type;

    /**
     * Agent 类型（当 type=AGENT 时使用）
     */
    private String agentType;

    /**
     * 工具ID（当 type=TOOL 时使用）
     */
    private String toolId;

    /**
     * 自定义处理器（当 type=CUSTOM 时使用）
     */
    private Function<WorkflowState, WorkflowState> handler;

    /**
     * 条件判断器（当 type=CONDITION 时使用）
     */
    private Predicate<WorkflowState> condition;

    /**
     * 条件成立时的下一个节点
     */
    private String trueNextNode;

    /**
     * 条件不成立时的下一个节点
     */
    private String falseNextNode;

    /**
     * 默认的下一个节点（非条件节点使用）
     */
    private String nextNode;

    /**
     * 是否可跳过
     */
    @Builder.Default
    private boolean skippable = false;

    /**
     * 执行超时（毫秒）
     */
    @Builder.Default
    private long timeoutMs = 30000;

    /**
     * 创建 Agent 调用节点
     */
    public static AgentNode agent(String nodeId, String agentType) {
        return AgentNode.builder()
                .nodeId(nodeId)
                .nodeName(agentType)
                .type(NodeType.AGENT)
                .agentType(agentType)
                .build();
    }

    /**
     * 创建条件节点
     */
    public static AgentNode condition(String nodeId, Predicate<WorkflowState> condition,
                                       String trueNext, String falseNext) {
        return AgentNode.builder()
                .nodeId(nodeId)
                .nodeName("条件判断")
                .type(NodeType.CONDITION)
                .condition(condition)
                .trueNextNode(trueNext)
                .falseNextNode(falseNext)
                .build();
    }

    /**
     * 创建工具调用节点
     */
    public static AgentNode tool(String nodeId, String toolId) {
        return AgentNode.builder()
                .nodeId(nodeId)
                .nodeName(toolId)
                .type(NodeType.TOOL)
                .toolId(toolId)
                .build();
    }

    /**
     * 创建自定义处理节点
     */
    public static AgentNode custom(String nodeId, String name, Function<WorkflowState, WorkflowState> handler) {
        return AgentNode.builder()
                .nodeId(nodeId)
                .nodeName(name)
                .type(NodeType.CUSTOM)
                .handler(handler)
                .build();
    }

    /**
     * 创建起始节点
     */
    public static AgentNode start() {
        return AgentNode.builder()
                .nodeId("__START__")
                .nodeName("开始")
                .type(NodeType.START)
                .build();
    }

    /**
     * 创建结束节点
     */
    public static AgentNode end() {
        return AgentNode.builder()
                .nodeId("__END__")
                .nodeName("结束")
                .type(NodeType.END)
                .build();
    }

    /**
     * 设置下一个节点并返回自身（链式调用）
     */
    public AgentNode then(String nextNodeId) {
        this.nextNode = nextNodeId;
        return this;
    }
}
