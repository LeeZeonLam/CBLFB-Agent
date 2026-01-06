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
 * 交货仓库持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("delivery_warehouse")
public class DeliveryWarehousePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String province;
    private String city;
    private String district;
    private String address;
    private String contactName;
    private String contactPhone;
    private String workingHours;
    private String supportedChannels;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
