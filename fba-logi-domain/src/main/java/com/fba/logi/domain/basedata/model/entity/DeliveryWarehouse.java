package com.fba.logi.domain.basedata.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交货仓库实体（国内仓库）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryWarehouse {

    /**
     * 仓库 ID
     */
    private Long warehouseId;

    /**
     * 仓库代码
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 收货时间（如：周一至周六 9:00-18:00）
     */
    private String workingHours;

    /**
     * 支持的渠道（如：matson, zim, yantian）
     */
    private List<String> supportedChannels;

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
     * 判断仓库是否可用
     */
    public boolean isAvailable() {
        return Boolean.TRUE.equals(enabled);
    }

    /**
     * 判断是否支持指定渠道
     */
    public boolean supportsChannel(String channelCode) {
        return supportedChannels != null && supportedChannels.contains(channelCode);
    }

    /**
     * 获取完整地址字符串
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (province != null) {
            sb.append(province);
        }
        if (city != null) {
            sb.append(city);
        }
        if (district != null) {
            sb.append(district);
        }
        if (address != null) {
            sb.append(address);
        }
        return sb.toString();
    }

    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return warehouseCode + " - " + warehouseName + " (" + city + ")";
    }
}
