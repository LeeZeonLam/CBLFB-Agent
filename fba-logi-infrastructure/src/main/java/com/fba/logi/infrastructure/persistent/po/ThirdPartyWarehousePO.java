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
 * 第三方海外仓持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("third_party_warehouse")
public class ThirdPartyWarehousePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String providerName;
    private String country;
    private String state;
    private String city;
    private String address;
    private String contactName;
    private String contactPhone;
    private String serviceTypes;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
