package com.fba.logi.domain.basedata.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 第三方海外仓实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdPartyWarehouse {

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
     * 服务商名称
     */
    private String providerName;

    /**
     * 国家代码
     */
    private String country;

    /**
     * 州/省
     */
    private String state;

    /**
     * 城市
     */
    private String city;

    /**
     * 街道地址
     */
    private String address;

    /**
     * 邮编
     */
    private String zipCode;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 支持的服务类型（如：仓储、一件代发、退货处理等）
     */
    private List<String> serviceTypes;

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
     * 判断是否支持指定服务类型
     */
    public boolean supportsService(String serviceType) {
        return serviceTypes != null && serviceTypes.contains(serviceType);
    }

    /**
     * 获取完整地址字符串
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null) {
            sb.append(address);
        }
        if (city != null) {
            sb.append(", ").append(city);
        }
        if (state != null) {
            sb.append(", ").append(state);
        }
        if (zipCode != null) {
            sb.append(" ").append(zipCode);
        }
        if (country != null) {
            sb.append(", ").append(country);
        }
        return sb.toString();
    }

    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return warehouseCode + " - " + warehouseName + " (" + providerName + ")";
    }
}
