package com.fba.logi.domain.order.model.vo;

import com.fba.logi.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 派送地址值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {

    /**
     * 地址类型：FBA/AWD/THIRD_PARTY/COMMERCIAL/RESIDENTIAL
     */
    private String addressType;

    /**
     * 收件人名称
     */
    private String recipientName;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 街道地址行1
     */
    private String addressLine1;

    /**
     * 街道地址行2
     */
    private String addressLine2;

    /**
     * 城市
     */
    private String city;

    /**
     * 州/省
     */
    private String state;

    /**
     * 邮编
     */
    private String zipCode;

    /**
     * 国家代码
     */
    private String countryCode;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    // ========== FBA/AWD 特有字段 ==========

    /**
     * FBA 仓库代码
     */
    private String fbaWarehouseCode;

    /**
     * AWD 仓库代码
     */
    private String awdWarehouseCode;

    /**
     * FBA Shipment ID
     */
    private String fbaShipmentId;

    // ========== 第三方海外仓特有字段 ==========

    /**
     * 第三方仓名称
     */
    private String thirdPartyWarehouseName;

    /**
     * 第三方仓代码
     */
    private String thirdPartyWarehouseCode;

    /**
     * 第三方仓服务商
     */
    private String thirdPartyProvider;

    // ========== 住宅/商业地址特有字段 ==========

    /**
     * 是否需要预约
     */
    private Boolean requiresAppointment;

    /**
     * 送货备注
     */
    private String deliveryInstructions;

    /**
     * 客户地址 ID（关联 CustomerAddress）
     */
    private Long customerAddressId;

    // ==================== 业务方法 ====================

    /**
     * 判断是否为 FBA 地址
     */
    public boolean isFbaAddress() {
        return Constants.DeliveryAddressType.FBA.equals(addressType);
    }

    /**
     * 判断是否为 AWD 地址
     */
    public boolean isAwdAddress() {
        return Constants.DeliveryAddressType.AWD.equals(addressType);
    }

    /**
     * 判断是否为第三方海外仓地址
     */
    public boolean isThirdPartyWarehouse() {
        return Constants.DeliveryAddressType.THIRD_PARTY.equals(addressType);
    }

    /**
     * 判断是否为商业地址
     */
    public boolean isCommercialAddress() {
        return Constants.DeliveryAddressType.COMMERCIAL.equals(addressType);
    }

    /**
     * 判断是否为住宅地址
     */
    public boolean isResidentialAddress() {
        return Constants.DeliveryAddressType.RESIDENTIAL.equals(addressType);
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
        if (countryCode != null) {
            sb.append(", ").append(countryCode);
        }
        return sb.toString();
    }

    /**
     * 获取地址类型的中文描述
     */
    public String getAddressTypeDescription() {
        if (addressType == null) {
            return "未知";
        }
        return switch (addressType) {
            case "fba" -> "FBA 仓库地址";
            case "awd" -> "AWD 仓库地址";
            case "third_party" -> "第三方海外仓";
            case "commercial" -> "商业地址";
            case "residential" -> "住宅地址";
            default -> addressType;
        };
    }

    /**
     * 验证地址是否完整
     */
    public boolean isValid() {
        // 基础字段验证
        if (addressType == null) {
            return false;
        }

        // 根据地址类型验证必填字段
        return switch (addressType) {
            case "fba", "awd" -> fbaWarehouseCode != null || awdWarehouseCode != null;
            case "third_party" -> thirdPartyWarehouseCode != null;
            case "commercial", "residential" -> addressLine1 != null && city != null
                    && state != null && zipCode != null && countryCode != null;
            default -> false;
        };
    }
}
