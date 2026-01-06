package com.fba.logi.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Kafka 消费者服务
 * 各业务模块可以继承或组合此服务来处理消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    /**
     * 消费订单创建事件
     */
    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = "${spring.kafka.consumer.group-id:fba-logi-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(
            @Payload Map<String, Object> message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        try {
            log.info("收到订单创建事件，Topic: {}, Key: {}, Partition: {}, Offset: {}",
                    topic, key, partition, offset);
            log.debug("消息内容: {}", message);

            // 处理订单创建事件
            processOrderCreated(message);

            // 手动确认消息
            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理订单创建事件失败，Key: {}, Error: {}", key, e.getMessage(), e);
            // 可以选择不确认，让消息重试
            // 或者发送到死信队列
        }
    }

    /**
     * 消费订单状态变更事件
     */
    @KafkaListener(
            topics = KafkaTopics.ORDER_STATUS_CHANGED,
            groupId = "${spring.kafka.consumer.group-id:fba-logi-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderStatusChanged(
            @Payload Map<String, Object> message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        try {
            log.info("收到订单状态变更事件，Key: {}", key);
            log.debug("消息内容: {}", message);

            // 处理订单状态变更
            processOrderStatusChanged(message);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理订单状态变更事件失败，Key: {}, Error: {}", key, e.getMessage(), e);
        }
    }

    /**
     * 消费仓储事件
     */
    @KafkaListener(
            topics = KafkaTopics.WAREHOUSE_EVENT,
            groupId = "${spring.kafka.consumer.group-id:fba-logi-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleWarehouseEvent(
            @Payload Map<String, Object> message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        try {
            log.info("收到仓储事件，Key: {}", key);
            log.debug("消息内容: {}", message);

            // 处理仓储事件
            processWarehouseEvent(message);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理仓储事件失败，Key: {}, Error: {}", key, e.getMessage(), e);
        }
    }

    /**
     * 消费 Agent 任务
     */
    @KafkaListener(
            topics = KafkaTopics.AGENT_TASK,
            groupId = "${spring.kafka.consumer.group-id:fba-logi-agent-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAgentTask(
            @Payload Map<String, Object> message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        try {
            log.info("收到 Agent 任务，TaskId: {}", key);
            log.debug("消息内容: {}", message);

            // 处理 Agent 任务
            processAgentTask(message);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理 Agent 任务失败，TaskId: {}, Error: {}", key, e.getMessage(), e);
        }
    }

    /**
     * 消费通知事件
     */
    @KafkaListener(
            topics = KafkaTopics.NOTIFICATION,
            groupId = "${spring.kafka.consumer.group-id:fba-logi-notification-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleNotification(
            @Payload Map<String, Object> message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        try {
            log.info("收到通知事件，UserId: {}", key);
            log.debug("消息内容: {}", message);

            // 处理通知
            processNotification(message);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理通知事件失败，UserId: {}, Error: {}", key, e.getMessage(), e);
        }
    }

    // ==================== 业务处理方法（可由子类覆盖） ====================

    /**
     * 处理订单创建事件
     */
    protected void processOrderCreated(Map<String, Object> message) {
        String orderNo = (String) message.get("orderNo");
        String userId = (String) message.get("userId");
        log.info("处理订单创建事件，订单号: {}, 用户: {}", orderNo, userId);
        // 具体业务逻辑由各服务实现
    }

    /**
     * 处理订单状态变更事件
     */
    protected void processOrderStatusChanged(Map<String, Object> message) {
        String orderNo = (String) message.get("orderNo");
        String fromState = (String) message.get("fromState");
        String toState = (String) message.get("toState");
        log.info("处理订单状态变更事件，订单号: {}, {} -> {}", orderNo, fromState, toState);
        // 具体业务逻辑由各服务实现
    }

    /**
     * 处理仓储事件
     */
    protected void processWarehouseEvent(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");
        String entityNo = (String) message.get("entityNo");
        log.info("处理仓储事件，类型: {}, 实体: {}", eventType, entityNo);
        // 具体业务逻辑由各服务实现
    }

    /**
     * 处理 Agent 任务
     */
    protected void processAgentTask(Map<String, Object> message) {
        String taskId = (String) message.get("taskId");
        String agentType = (String) message.get("agentType");
        log.info("处理 Agent 任务，TaskId: {}, AgentType: {}", taskId, agentType);
        // 具体业务逻辑由各服务实现
    }

    /**
     * 处理通知事件
     */
    protected void processNotification(Map<String, Object> message) {
        String type = (String) message.get("type");
        String userId = (String) message.get("userId");
        String title = (String) message.get("title");
        log.info("处理通知事件，类型: {}, 用户: {}, 标题: {}", type, userId, title);
        // 具体业务逻辑由各服务实现
    }

}
