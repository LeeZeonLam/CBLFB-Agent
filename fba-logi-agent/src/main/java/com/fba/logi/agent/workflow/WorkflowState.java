package com.fba.logi.agent.workflow;

import com.fba.logi.agent.core.AgentState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * 工作流状态
 * 在工作流执行过程中传递的状态对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowState {

    /**
     * 工作流 ID
     */
    private String workflowId;

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 原始用户输入
     */
    private String originalInput;

    /**
     * 当前处理的消息
     */
    private String currentMessage;

    /**
     * 当前节点 ID
     */
    private String currentNodeId;

    /**
     * 已执行的节点历史
     */
    @Builder.Default
    private List<NodeExecution> executionHistory = new ArrayList<>();

    /**
     * 每个 Agent 的执行结果
     */
    @Builder.Default
    private Map<String, AgentState> agentResults = new LinkedHashMap<>();

    /**
     * 上下文变量
     */
    @Builder.Default
    private Map<String, Object> context = new HashMap<>();

    /**
     * 最终输出
     */
    private String finalOutput;

    /**
     * 工作流状态
     */
    @Builder.Default
    private Status status = Status.PENDING;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 结束时间
     */
    private long endTime;

    /**
     * 工作流状态枚举
     */
    public enum Status {
        PENDING,      // 待执行
        RUNNING,      // 执行中
        WAITING,      // 等待用户输入
        COMPLETED,    // 已完成
        FAILED,       // 失败
        CANCELLED     // 已取消
    }

    /**
     * 节点执行记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeExecution {
        private String nodeId;
        private String nodeName;
        private AgentNode.NodeType nodeType;
        private long startTime;
        private long endTime;
        private boolean success;
        private String output;
        private String errorMessage;
    }

    /**
     * 添加节点执行记录
     */
    public void recordNodeExecution(AgentNode node, long startTime, long endTime,
                                     boolean success, String output, String error) {
        executionHistory.add(NodeExecution.builder()
                .nodeId(node.getNodeId())
                .nodeName(node.getNodeName())
                .nodeType(node.getType())
                .startTime(startTime)
                .endTime(endTime)
                .success(success)
                .output(output)
                .errorMessage(error)
                .build());
    }

    /**
     * 保存 Agent 执行结果
     */
    public void saveAgentResult(String agentType, AgentState state) {
        agentResults.put(agentType, state);
    }

    /**
     * 获取 Agent 执行结果
     */
    public Optional<AgentState> getAgentResult(String agentType) {
        return Optional.ofNullable(agentResults.get(agentType));
    }

    /**
     * 获取上下文变量
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextValue(String key) {
        return (T) context.get(key);
    }

    /**
     * 设置上下文变量
     */
    public void setContextValue(String key, Object value) {
        context.put(key, value);
    }

    /**
     * 检查上下文变量是否存在
     */
    public boolean hasContextValue(String key) {
        return context.containsKey(key);
    }

    /**
     * 获取执行总耗时
     */
    public long getDurationMs() {
        if (startTime == 0) return 0;
        long end = endTime > 0 ? endTime : System.currentTimeMillis();
        return end - startTime;
    }

    /**
     * 获取所有 Agent 的最终输出
     */
    public String getAggregatedOutput() {
        if (finalOutput != null) {
            return finalOutput;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, AgentState> entry : agentResults.entrySet()) {
            if (entry.getValue().getFinalResponse() != null) {
                sb.append(entry.getValue().getFinalResponse()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * 创建初始状态
     */
    public static WorkflowState create(String sessionId, String userId, String input) {
        return WorkflowState.builder()
                .workflowId(UUID.randomUUID().toString())
                .sessionId(sessionId)
                .userId(userId)
                .originalInput(input)
                .currentMessage(input)
                .status(Status.PENDING)
                .startTime(System.currentTimeMillis())
                .build();
    }
}
