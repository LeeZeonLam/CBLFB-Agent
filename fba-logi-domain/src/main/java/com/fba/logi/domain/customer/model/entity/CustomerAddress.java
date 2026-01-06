package com.fba.logi.domain.customer.model.entity;

import com.fba.logi.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户派送地址实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddress {

    /**
     * 地址 ID
     */
    private Long addressId;

    /**
     * 关联客户 ID
     */
    private Long customerId;

    /**
     * 地址类型：COMMERCIAL/RESIDENTIAL
     */
    private String addressType;

    /**
     * 地址名称/标签（如：公司总部、分仓A、家）
     */
    private String addressName;

    /**
     * 收件人
     */
    private String recipientName;

    /**
     * 公司名称（商业地址时使用）
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

    /**
     * 是否默认地址
     */
    private Boolean isDefault;

    /**
     * 是否需要预约送货
     */
    private Boolean requiresAppointment;

    /**
     * 送货备注
     */
    private String deliveryInstructions;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ==================== 业务方法 ====================

    /**
     * 判断是否为商业地址
     */
    public boolean isCommercial() {
        return Constants.DeliveryAddressType.COMMERCIAL.equals(addressType);
    }

    /**
     * 判断是否为住宅地址
     */
    public boolean isResidential() {
        return Constants.DeliveryAddressType.RESIDENTIAL.equals(addressType);
    }

    /**
     * 判断是否为默认地址
     */
    public boolean isDefaultAddress() {
        return Boolean.TRUE.equals(isDefault);
    }

    /**
     * 设置为默认地址
     */
    public void setAsDefault() {
        this.isDefault = true;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 取消默认地址
     */
    public void unsetDefault() {
        this.isDefault = false;
        this.updateTime = LocalDateTime.now();
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
     * 获取显示名称（地址名称 + 类型）
     */
    public String getDisplayName() {
        String typeName = isCommercial() ? "商业地址" : "住宅地址";
        if (addressName != null && !addressName.isEmpty()) {
            return addressName + " (" + typeName + ")";
        }
        return typeName + " - " + city;
    }

    /**
     * 验证地址是否完整
     */
    public boolean isValid() {
        return addressLine1 != null && !addressLine1.isEmpty()
                && city != null && !city.isEmpty()
                && state != null && !state.isEmpty()
                && zipCode != null && !zipCode.isEmpty()
                && countryCode != null && !countryCode.isEmpty();
    }
}
