package com.fba.logi.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户地址持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("customer_address")
public class CustomerAddressPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long addressId;
    private Long customerId;
    private String addressType;
    private String addressName;
    private String recipientName;
    private String companyName;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String countryCode;
    private String phone;
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
