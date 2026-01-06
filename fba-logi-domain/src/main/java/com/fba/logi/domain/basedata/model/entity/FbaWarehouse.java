package com.fba.logi.domain.basedata.model.entity;

import com.fba.logi.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FBA/AWD 仓库实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbaWarehouse {

    /**
     * 仓库 ID
     */
    private Long warehouseId;

    /**
     * 仓库代码（如 ONT8、PHX6）
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 仓库类型：FBA/AWD
     */
    private String warehouseType;

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
     * 街道地址行1
     */
    private String addressLine1;

    /**
     * 街道地址行2
     */
    private String addressLine2;

    /**
     * 邮编
     */
    private String zipCode;

    /**
     * 纬度（可选）
     */
    private BigDecimal latitude;

    /**
     * 经度（可选）
     */
    private BigDecimal longitude;

    /**
     * 是否接受货物
     */
    private Boolean acceptingShipments;

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
     * 判断是否为 FBA 仓库
     */
    public boolean isFbaWarehouse() {
        return Constants.WarehouseType.FBA.equals(warehouseType);
    }

    /**
     * 判断是否为 AWD 仓库
     */
    public boolean isAwdWarehouse() {
        return Constants.WarehouseType.AWD.equals(warehouseType);
    }

    /**
     * 判断仓库是否可用
     */
    public boolean isAvailable() {
        return Boolean.TRUE.equals(enabled) && Boolean.TRUE.equals(acceptingShipments);
    }

    /**
     * 获取完整地址字符串
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) {
            sb.append(addressLine1);
        }
        if (addressLine2 != null && !addressLine2.isEmpty()) {
            sb.append(", ").append(addressLine2);
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
        String typeName = isFbaWarehouse() ? "FBA" : "AWD";
        return warehouseCode + " - " + warehouseName + " (" + typeName + ")";
    }
}
