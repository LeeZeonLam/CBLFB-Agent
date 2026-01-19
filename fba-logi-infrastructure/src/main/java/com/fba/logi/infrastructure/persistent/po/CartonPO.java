package com.fba.logi.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 纸箱持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("carton")
public class CartonPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 纸箱 ID
     */
    private Long cartonId;

    /**
     * 纸箱编号
     */
    private String cartonNo;

    /**
     * 关联订单 ID
     */
    private Long orderId;

    /**
     * 关联托盘 ID
     */
    private Long palletId;

    /**
     * FBA 标签号
     */
    private String fbaLabel;

    /**
     * 长度（CM）
     */
    private BigDecimal length;

    /**
     * 宽度（CM）
     */
    private BigDecimal width;

    /**
     * 高度（CM）
     */
    private BigDecimal height;

    /**
     * 重量（KG）
     */
    private BigDecimal weight;

    /**
     * 商品 SKU
     */
    private String sku;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 状态
     */
    private String status;

    /**
     * 是否含敏感货
     */
    private Boolean hasSensitive;

    /**
     * 是否需要特殊处理
     */
    private Boolean needSpecialHandle;

    /**
     * 特殊处理说明
     */
    private String specialHandleNote;

    /**
     * 备注
     */
    private String remark;

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
