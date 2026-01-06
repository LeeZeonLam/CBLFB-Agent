package com.fba.logi.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发货订单持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("shipment_order")
public class ShipmentOrderPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private String orderNo;
    private String userId;

    // 客户关联
    private Long customerId;
    private String customerNo;
    private String customerName;
    private Long customerAddressId;

    // 基础信息
    private String orderType;
    private String originAddress;
    private String destCountry;
    private String fbaWarehouseCode;
    private String shippingMethod;

    // 渠道和包装
    private String channelCode;
    private String channelName;
    private String packagingType;

    // 派送地址
    private String deliveryAddressType;
    private String deliveryAddressDetail;
    private String deliveryWarehouseCode;

    // 货物信息
    private BigDecimal totalWeight;
    private BigDecimal totalVolume;
    private Integer totalPieces;
    private BigDecimal estimatedCost;

    // 柜子关联
    private Long containerId;
    private String containerNo;

    // 状态
    private String state;
    private String rejectReason;

    // 时间节点
    private LocalDateTime receivedTime;
    private LocalDateTime dimensionRecordedTime;
    private LocalDateTime loadedTime;
    private LocalDateTime actualDepartureTime;
    private LocalDateTime actualArrivalPortTime;
    private LocalDateTime pickedTime;
    private LocalDateTime deliveryStartTime;
    private LocalDateTime completedTime;

    // 通用字段
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
