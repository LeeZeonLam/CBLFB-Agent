package com.fba.logi.domain.customer.model.entity;

import com.fba.logi.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    /**
     * 客户 ID
     */
    private Long customerId;

    /**
     * 客户编号
     */
    private String customerNo;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态：ACTIVE/INACTIVE
     */
    private String status;

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
     * 判断客户是否活跃
     */
    public boolean isActive() {
        return Constants.CustomerStatus.ACTIVE.equals(status);
    }

    /**
     * 激活客户
     */
    public void activate() {
        this.status = Constants.CustomerStatus.ACTIVE;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 停用客户
     */
    public void deactivate() {
        this.status = Constants.CustomerStatus.INACTIVE;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 获取显示名称（公司名称 + 联系人）
     */
    public String getDisplayName() {
        if (companyName != null && contactName != null) {
            return companyName + " - " + contactName;
        } else if (companyName != null) {
            return companyName;
        } else if (contactName != null) {
            return contactName;
        }
        return customerNo;
    }
}
