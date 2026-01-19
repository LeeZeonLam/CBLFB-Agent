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
 * 订单产品关联持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_product")
public class OrderProductPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long productId;
    private String sku;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String currency;

    // 冗余尺寸重量
    private BigDecimal unitWeight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;

    // 敏感货标识
    private Boolean hasBattery;
    private Boolean isLiquid;
    private Boolean isPowder;
    private Boolean isMagnetic;

    // 其他
    private String hsCode;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
