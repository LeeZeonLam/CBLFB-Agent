package com.fba.logi.agent.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Agent 工作流定义
 * 类似 LangGraph 的状态图，定义 Agent 之间的执行顺序和条件转换
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentWorkflow {

    /**
     * 工作流 ID
     */
    private String workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 工作流描述
     */
    private String description;

    /**
     * 节点映射
     */
    @Builder.Default
    private Map<String, AgentNode> nodes = new LinkedHashMap<>();

    /**
     * 边（转换）定义
     */
    @Builder.Default
    private Map<String, List<Edge>> edges = new HashMap<>();

    /**
     * 起始节点 ID
     */
    private String startNodeId;

    /**
     * 结束节点 ID 列表
     */
    @Builder.Default
    private List<String> endNodeIds = new ArrayList<>();

    /**
     * 边（转换）定义
     */
    @Data
    @Builder
    public static class Edge {
        private String fromNode;
        private String toNode;
        private String condition;  // 条件描述（可选）
    }

    /**
     * 工作流构建器
     */
    public static class WorkflowBuilder {
        private final Map<String, AgentNode> nodes = new LinkedHashMap<>();
        private final Map<String, List<Edge>> edges = new HashMap<>();
        private String startNodeId;
        private final List<String> endNodeIds = new ArrayList<>();
        private String workflowId;
        private String workflowName;
        private String description;

        public WorkflowBuilder id(String id) {
            this.workflowId = id;
            return this;
        }

        public WorkflowBuilder name(String name) {
            this.workflowName = name;
            return this;
        }

        public WorkflowBuilder description(String desc) {
            this.description = desc;
            return this;
        }

        /**
         * 添加节点
         */
        public WorkflowBuilder addNode(AgentNode node) {
            nodes.put(node.getNodeId(), node);
            return this;
        }

        /**
         * 添加 Agent 节点
         */
        public WorkflowBuilder addAgentNode(String nodeId, String agentType) {
            return addNode(AgentNode.agent(nodeId, agentType));
        }

        /**
         * 添加边（转换）
         */
        public WorkflowBuilder addEdge(String from, String to) {
            edges.computeIfAbsent(from, k -> new ArrayList<>())
                    .add(Edge.builder().fromNode(from).toNode(to).build());

            // 如果 from 节点存在且没有设置 nextNode，设置它
            if (nodes.containsKey(from) && nodes.get(from).getNextNode() == null) {
                nodes.get(from).setNextNode(to);
            }

            return this;
        }

        /**
         * 添加条件边
         */
        public WorkflowBuilder addConditionalEdge(String from, String condition, String to) {
            edges.computeIfAbsent(from, k -> new ArrayList<>())
                    .add(Edge.builder().fromNode(from).toNode(to).condition(condition).build());
            return this;
        }

        /**
         * 设置起始节点
         */
        public WorkflowBuilder setStartNode(String nodeId) {
            this.startNodeId = nodeId;
            return this;
        }

        /**
         * 添加结束节点
         */
        public WorkflowBuilder addEndNode(String nodeId) {
            this.endNodeIds.add(nodeId);
            return this;
        }

        /**
         * 构建工作流
         */
        public AgentWorkflow build() {
            // 自动添加起始和结束节点（如果没有显式设置）
            if (startNodeId == null && !nodes.isEmpty()) {
                startNodeId = nodes.keySet().iterator().next();
            }

            AgentWorkflow workflow = new AgentWorkflow();
            workflow.setWorkflowId(workflowId != null ? workflowId : UUID.randomUUID().toString());
            workflow.setWorkflowName(workflowName);
            workflow.setDescription(description);
            workflow.setNodes(nodes);
            workflow.setEdges(edges);
            workflow.setStartNodeId(startNodeId);
            workflow.setEndNodeIds(endNodeIds);

            return workflow;
        }
    }

    /**
     * 创建新的构建器
     */
    public static WorkflowBuilder builder() {
        return new WorkflowBuilder();
    }

    /**
     * 获取节点
     */
    public Optional<AgentNode> getNode(String nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }

    /**
     * 获取起始节点
     */
    public AgentNode getStartNode() {
        return nodes.get(startNodeId);
    }

    /**
     * 检查节点是否为结束节点
     */
    public boolean isEndNode(String nodeId) {
        return endNodeIds.contains(nodeId) || "__END__".equals(nodeId);
    }

    /**
     * 获取节点的出边
     */
    public List<Edge> getOutgoingEdges(String nodeId) {
        return edges.getOrDefault(nodeId, Collections.emptyList());
    }

    /**
     * 获取下一个节点 ID
     */
    public String getNextNodeId(String currentNodeId, WorkflowState state) {
        AgentNode currentNode = nodes.get(currentNodeId);
        if (currentNode == null) {
            return null;
        }

        // 条件节点使用条件判断
        if (currentNode.getType() == AgentNode.NodeType.CONDITION) {
            boolean result = currentNode.getCondition().test(state);
            return result ? currentNode.getTrueNextNode() : currentNode.getFalseNextNode();
        }

        // 其他节点使用默认的 nextNode
        return currentNode.getNextNode();
    }

    /**
     * 可视化工作流（生成 ASCII 图）
     */
    public String visualize() {
        StringBuilder sb = new StringBuilder();
        sb.append("工作流: ").append(workflowName).append("\n");
        sb.append("================================\n\n");

        for (Map.Entry<String, AgentNode> entry : nodes.entrySet()) {
            AgentNode node = entry.getValue();
            sb.append("[").append(node.getNodeId()).append("]");
            sb.append(" (").append(node.getType()).append(")");

            if (node.getNextNode() != null) {
                sb.append(" --> ").append(node.getNextNode());
            }
            if (node.getTrueNextNode() != null) {
                sb.append(" --[true]--> ").append(node.getTrueNextNode());
            }
            if (node.getFalseNextNode() != null) {
                sb.append(" --[false]--> ").append(node.getFalseNextNode());
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
