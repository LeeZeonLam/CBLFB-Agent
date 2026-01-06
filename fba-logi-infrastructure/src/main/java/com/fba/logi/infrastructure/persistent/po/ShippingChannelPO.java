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
 * 渠道持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("shipping_channel")
public class ShippingChannelPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long channelId;
    private String channelCode;
    private String channelName;
    private String carrier;
    private String transportType;
    private String departurePort;
    private String arrivalPorts;
    private Integer transitDays;
    private BigDecimal pricePerCbm;
    private BigDecimal pricePerKg;
    private BigDecimal minWeight;
    private BigDecimal maxWeight;
    private String supportedContainerTypes;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
