package com.fba.logi.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka 生产者服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 发送消息（异步）
     *
     * @param topic   主题
     * @param message 消息内容
     */
    public void send(String topic, Object message) {
        String key = UUID.randomUUID().toString();
        send(topic, key, message);
    }

    /**
     * 发送消息（指定 Key）
     *
     * @param topic   主题
     * @param key     消息 Key
     * @param message 消息内容
     */
    public void send(String topic, String key, Object message) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("发送 Kafka 消息失败，Topic: {}, Key: {}, Error: {}",
                        topic, key, ex.getMessage());
            } else {
                log.debug("发送 Kafka 消息成功，Topic: {}, Key: {}, Partition: {}, Offset: {}",
                        topic, key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    /**
     * 发送消息（同步等待）
     *
     * @param topic   主题
     * @param message 消息内容
     * @return 发送结果
     */
    public SendResult<String, Object> sendSync(String topic, Object message) {
        String key = UUID.randomUUID().toString();
        return sendSync(topic, key, message);
    }

    /**
     * 发送消息（同步等待，指定 Key）
     */
    public SendResult<String, Object> sendSync(String topic, String key, Object message) {
        try {
            SendResult<String, Object> result = kafkaTemplate.send(topic, key, message).get();
            log.debug("同步发送 Kafka 消息成功，Topic: {}, Key: {}, Partition: {}, Offset: {}",
                    topic, key,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            return result;
        } catch (Exception e) {
            log.error("同步发送 Kafka 消息失败，Topic: {}, Key: {}, Error: {}",
                    topic, key, e.getMessage());
            throw new RuntimeException("Kafka 消息发送失败", e);
        }
    }

    // ==================== 订单事件 ====================

    /**
     * 发送订单创建事件
     */
    public void sendOrderCreated(Long orderId, String orderNo, String userId) {
        Map<String, Object> event = Map.of(
                "eventType", "ORDER_CREATED",
                "orderId", orderId,
                "orderNo", orderNo,
                "userId", userId,
                "timestamp", System.currentTimeMillis()
        );
        send(KafkaTopics.ORDER_CREATED, orderNo, event);
        log.info("发送订单创建事件，订单号: {}", orderNo);
    }

    /**
     * 发送订单状态变更事件
     */
    public void sendOrderStatusChanged(Long orderId, String orderNo, String fromState, String toState) {
        Map<String, Object> event = Map.of(
                "eventType", "ORDER_STATUS_CHANGED",
                "orderId", orderId,
                "orderNo", orderNo,
                "fromState", fromState,
                "toState", toState,
                "timestamp", System.currentTimeMillis()
        );
        send(KafkaTopics.ORDER_STATUS_CHANGED, orderNo, event);
        log.info("发送订单状态变更事件，订单号: {}, {} -> {}", orderNo, fromState, toState);
    }

    // ==================== 仓储事件 ====================

    /**
     * 发送仓储事件
     */
    public void sendWarehouseEvent(String eventType, Long entityId, String entityNo, Map<String, Object> data) {
        Map<String, Object> event = Map.of(
                "eventType", eventType,
                "entityId", entityId,
                "entityNo", entityNo,
                "data", data,
                "timestamp", System.currentTimeMillis()
        );
        send(KafkaTopics.WAREHOUSE_EVENT, entityNo, event);
        log.info("发送仓储事件，类型: {}, 实体: {}", eventType, entityNo);
    }

    // ==================== Agent 事件 ====================

    /**
     * 发送 Agent 任务
     */
    public void sendAgentTask(String taskId, String agentType, String sessionId, String message) {
        Map<String, Object> task = Map.of(
                "taskId", taskId,
                "agentType", agentType,
                "sessionId", sessionId,
                "message", message,
                "timestamp", System.currentTimeMillis()
        );
        send(KafkaTopics.AGENT_TASK, taskId, task);
        log.info("发送 Agent 任务，TaskId: {}, AgentType: {}", taskId, agentType);
    }

    /**
     * 发送 Agent 结果
     */
    public void sendAgentResult(String taskId, String sessionId, String result, boolean success) {
        Map<String, Object> resultEvent = Map.of(
                "taskId", taskId,
                "sessionId", sessionId,
                "result", result,
                "success", success,
                "timestamp", System.currentTimeMillis()
        );
        send(KafkaTopics.AGENT_RESULT, taskId, resultEvent);
        log.info("发送 Agent 结果，TaskId: {}, Success: {}", taskId, success);
    }

    // ==================== 通知事件 ====================

    /**
     * 发送通知
     */
    public void sendNotification(String type, String userId, String title, String content) {
        Map<String, Object> notification = Map.of(
                "type", type,
                "userId", userId,
                "title", title,
                "content", content,
                "timestamp", System.currentTimeMillis()
        );
        send(KafkaTopics.NOTIFICATION, userId, notification);
        log.info("发送通知，类型: {}, 用户: {}", type, userId);
    }

}
