package com.fba.logi.domain.basedata.model.entity;

import com.fba.logi.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 渠道实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingChannel {

    /**
     * 渠道 ID
     */
    private Long channelId;

    /**
     * 渠道代码（如：matson, zim, yantian）
     */
    private String channelCode;

    /**
     * 渠道名称（如：美森快船、以星快船、盐田船）
     */
    private String channelName;

    /**
     * 船公司
     */
    private String carrier;

    /**
     * 运输类型：SEA_EXPRESS(海运快船)/SEA_STANDARD(海运普船)
     */
    private String transportType;

    /**
     * 起运港
     */
    private String departurePort;

    /**
     * 可达港口列表
     */
    private List<String> arrivalPorts;

    /**
     * 预计时效（天）
     */
    private Integer transitDays;

    /**
     * 每立方价格
     */
    private BigDecimal pricePerCbm;

    /**
     * 每公斤价格
     */
    private BigDecimal pricePerKg;

    /**
     * 最小重量限制（KG）
     */
    private BigDecimal minWeight;

    /**
     * 最大重量限制（KG）
     */
    private BigDecimal maxWeight;

    /**
     * 支持的柜型列表
     */
    private List<String> supportedContainerTypes;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    // ==================== 业务方法 ====================

    /**
     * 判断渠道是否可用
     */
    public boolean isAvailable() {
        return Boolean.TRUE.equals(enabled);
    }

    /**
     * 判断是否为快船
     */
    public boolean isExpressShipping() {
        return "SEA_EXPRESS".equals(transportType);
    }

    /**
     * 判断是否支持指定港口
     */
    public boolean supportsPort(String port) {
        return arrivalPorts != null && arrivalPorts.contains(port);
    }

    /**
     * 判断是否支持指定柜型
     */
    public boolean supportsContainerType(String containerType) {
        return supportedContainerTypes != null && supportedContainerTypes.contains(containerType);
    }

    /**
     * 判断重量是否在限制范围内
     */
    public boolean isWeightInRange(BigDecimal weight) {
        if (weight == null) {
            return false;
        }
        boolean minCheck = minWeight == null || weight.compareTo(minWeight) >= 0;
        boolean maxCheck = maxWeight == null || weight.compareTo(maxWeight) <= 0;
        return minCheck && maxCheck;
    }

    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        String typeDesc = isExpressShipping() ? "快船" : "普船";
        return channelName + " (" + typeDesc + ", 约" + transitDays + "天)";
    }

    /**
     * 获取渠道类型的中文名称
     */
    public String getChannelDisplayName() {
        if (channelCode == null) {
            return channelName;
        }
        return switch (channelCode) {
            case Constants.ChannelType.MATSON -> "美森快船";
            case Constants.ChannelType.ZIM -> "以星快船";
            case Constants.ChannelType.YANTIAN -> "盐田船";
            default -> channelName;
        };
    }
}
