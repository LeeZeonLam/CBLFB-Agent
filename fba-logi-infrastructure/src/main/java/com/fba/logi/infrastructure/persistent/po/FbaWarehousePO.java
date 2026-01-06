package com.fba.logi.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FBA/AWD 仓库持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fba_warehouse")
public class FbaWarehousePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String warehouseType;
    private String country;
    private String state;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String zipCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean acceptingShipments;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
