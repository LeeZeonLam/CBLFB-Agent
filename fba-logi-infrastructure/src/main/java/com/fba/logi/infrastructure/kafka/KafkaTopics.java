package com.fba.logi.infrastructure.kafka;

/**
 * Kafka Topic 常量定义
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // 防止实例化
    }

    /**
     * 订单相关 Topic
     */
    public static final String ORDER_CREATED = "fba.order.created";
    public static final String ORDER_STATUS_CHANGED = "fba.order.status-changed";
    public static final String ORDER_SUBMITTED = "fba.order.submitted";
    public static final String ORDER_APPROVED = "fba.order.approved";
    public static final String ORDER_REJECTED = "fba.order.rejected";
    public static final String ORDER_SHIPPED = "fba.order.shipped";
    public static final String ORDER_DELIVERED = "fba.order.delivered";

    /**
     * 仓储相关 Topic
     */
    public static final String WAREHOUSE_EVENT = "fba.warehouse.event";
    public static final String CARTON_RECEIVED = "fba.warehouse.carton-received";
    public static final String PALLET_STORED = "fba.warehouse.pallet-stored";
    public static final String PALLET_RELEASED = "fba.warehouse.pallet-released";

    /**
     * Agent 相关 Topic
     */
    public static final String AGENT_TASK = "fba.agent.task";
    public static final String AGENT_RESULT = "fba.agent.result";
    public static final String AGENT_ERROR = "fba.agent.error";

    /**
     * 营销相关 Topic
     */
    public static final String RAFFLE_EXECUTED = "fba.marketing.raffle-executed";
    public static final String ACTIVITY_STARTED = "fba.marketing.activity-started";
    public static final String ACTIVITY_ENDED = "fba.marketing.activity-ended";

    /**
     * 通知相关 Topic
     */
    public static final String NOTIFICATION = "fba.notification";
    public static final String NOTIFICATION_EMAIL = "fba.notification.email";
    public static final String NOTIFICATION_SMS = "fba.notification.sms";
    public static final String NOTIFICATION_PUSH = "fba.notification.push";

    /**
     * 审计日志 Topic
     */
    public static final String AUDIT_LOG = "fba.audit.log";

}
