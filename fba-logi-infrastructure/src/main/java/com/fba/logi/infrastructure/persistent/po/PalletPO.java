package com.fba.logi.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 托盘持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pallet")
public class PalletPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 托盘 ID
     */
    private String palletId;

    /**
     * 订单 ID
     */
    private String orderId;

    /**
     * 长度 (cm)
     */
    private BigDecimal length;

    /**
     * 宽度 (cm)
     */
    private BigDecimal width;

    /**
     * 高度 (cm)
     */
    private BigDecimal height;

    /**
     * 重量 (kg)
     */
    private BigDecimal weight;

    /**
     * 标签是否已验证
     */
    private Boolean labelVerified;

    /**
     * 状态：pending/verified/loaded
     */
    private String state;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
