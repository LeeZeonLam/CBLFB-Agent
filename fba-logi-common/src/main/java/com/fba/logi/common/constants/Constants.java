package com.fba.logi.common.constants;

/**
 * 系统常量定义
 */
public final class Constants {

    private Constants() {
        // 防止实例化
    }

    /**
     * 响应码
     */
    public static final class ResponseCode {
        public static final String SUCCESS = "0000";
        public static final String UN_ERROR = "0001";
        public static final String ILLEGAL_PARAMETER = "0002";
        public static final String INDEX_DUP = "0003";
        public static final String NO_UPDATE = "0004";
        public static final String NOT_FOUND = "0005";
        public static final String UNAUTHORIZED = "0401";
        public static final String FORBIDDEN = "0403";
    }

    /**
     * LLM 提供商
     */
    public static final class LlmProvider {
        public static final String DEEPSEEK = "deepseek";
        public static final String ZHIPU = "zhipu";
        public static final String GEMINI = "gemini";
    }

    /**
     * Agent 类型
     */
    public static final class AgentType {
        // 营销域 Agent
        public static final String MARKETING_STRATEGIST = "marketing_strategist";
        public static final String MARKETING_SALES = "marketing_sales";
        // 订单域 Agent
        public static final String ORDER_BOOKING = "order_booking";
        public static final String ORDER_AUDITOR = "order_auditor";
        // 仓储域 Agent
        public static final String WAREHOUSE_OPS = "warehouse_ops";
        // 物流域 Agent
        public static final String LOGISTICS_TRACKER = "logistics_tracker";
        // 写作分析域 Agent
        public static final String WRITING_ANALYST = "writing_analyst";
    }

    /**
     * 活动状态
     */
    public static final class ActivityState {
        public static final String CREATE = "create";
        public static final String OPEN = "open";
        public static final String CLOSE = "close";
    }

    /**
     * 订单状态（FBA 头程完整流程）
     */
    public static final class OrderState {
        // 基础状态
        /** 草稿/下单 */
        public static final String DRAFT = "draft";
        /** 待审核 */
        public static final String PENDING = "pending";
        /** 已审核 */
        public static final String APPROVED = "approved";
        /** 已驳回 */
        public static final String REJECTED = "rejected";

        // 入仓阶段
        /** 货物入仓 */
        public static final String RECEIVED = "received";
        /** 材积录入完成 */
        public static final String DIMENSION_RECORDED = "dimension_recorded";

        // 装柜阶段
        /** 已排柜 */
        public static final String CONTAINER_ASSIGNED = "container_assigned";
        /** 已装柜 */
        public static final String CONTAINER_LOADED = "container_loaded";

        // 运输阶段
        /** 已开船 */
        public static final String DEPARTED = "departed";
        /** 已到港 */
        public static final String ARRIVED = "arrived";
        /** 已提柜 */
        public static final String CONTAINER_PICKED = "container_picked";

        // 派送阶段
        /** 派送中 */
        public static final String DELIVERING = "delivering";
        /** 完成 */
        public static final String COMPLETED = "completed";

        // 兼容旧状态（将逐步废弃）
        @Deprecated
        public static final String SHIPPED = "shipped";
        @Deprecated
        public static final String DELIVERED = "delivered";
    }

    /**
     * 柜子状态
     */
    public static final class ContainerState {
        /** 空柜 */
        public static final String EMPTY = "empty";
        /** 排柜中 */
        public static final String ASSIGNING = "assigning";
        /** 装柜中 */
        public static final String LOADING = "loading";
        /** 已装柜 */
        public static final String LOADED = "loaded";
        /** 已开船 */
        public static final String DEPARTED = "departed";
        /** 已到港 */
        public static final String ARRIVED = "arrived";
        /** 已提柜 */
        public static final String PICKED = "picked";
        /** 已派送 */
        public static final String DELIVERED = "delivered";
    }

    /**
     * 航次状态
     */
    public static final class VoyageState {
        /** 已排期 */
        public static final String SCHEDULED = "scheduled";
        /** 已开船 */
        public static final String DEPARTED = "departed";
        /** 运输中 */
        public static final String IN_TRANSIT = "in_transit";
        /** 已到港 */
        public static final String ARRIVED = "arrived";
        /** 完成 */
        public static final String COMPLETED = "completed";
    }

    /**
     * 装柜包装类型
     */
    public static final class PackagingType {
        /** 散货 */
        public static final String LOOSE = "loose";
        /** 托盘 */
        public static final String PALLET = "pallet";
        /** 整柜 */
        public static final String FCL = "fcl";
    }

    /**
     * 派送地址类型
     */
    public static final class DeliveryAddressType {
        /** FBA 地址 */
        public static final String FBA = "fba";
        /** AWD 地址 */
        public static final String AWD = "awd";
        /** 第三方海外仓 */
        public static final String THIRD_PARTY = "third_party";
        /** 商业地址 */
        public static final String COMMERCIAL = "commercial";
        /** 住宅地址 */
        public static final String RESIDENTIAL = "residential";
    }

    /**
     * 客户地址类型
     */
    public static final class AddressType {
        /** 商业地址 */
        public static final String COMMERCIAL = "commercial";
        /** 住宅地址 */
        public static final String RESIDENTIAL = "residential";
    }

    /**
     * 柜型
     */
    public static final class ContainerType {
        /** 20英尺标准柜，容量约28CBM */
        public static final String GP20 = "20GP";
        /** 40英尺标准柜，容量约58CBM */
        public static final String GP40 = "40GP";
        /** 40英尺高柜，容量约68CBM */
        public static final String HQ40 = "40HQ";
        /** 45英尺高柜，容量约78CBM */
        public static final String HQ45 = "45HQ";
    }

    /**
     * 渠道类型
     */
    public static final class ChannelType {
        /** 美森快船 */
        public static final String MATSON = "matson";
        /** 以星快船 */
        public static final String ZIM = "zim";
        /** 盐田船 */
        public static final String YANTIAN = "yantian";
    }

    /**
     * 客户状态
     */
    public static final class CustomerStatus {
        /** 活跃 */
        public static final String ACTIVE = "active";
        /** 停用 */
        public static final String INACTIVE = "inactive";
    }

    /**
     * 仓库类型
     */
    public static final class WarehouseType {
        /** FBA 仓库 */
        public static final String FBA = "fba";
        /** AWD 仓库 */
        public static final String AWD = "awd";
    }

}
