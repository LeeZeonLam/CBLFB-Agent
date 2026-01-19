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
 * 产品持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("product")
public class ProductPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private String sku;
    private String asin;
    private String fnsku;
    private String productName;
    private String productNameEn;
    private String description;
    private String hsCode;

    // 尺寸重量
    private BigDecimal unitWeight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;

    // 价格
    private BigDecimal unitPrice;
    private String currency;

    // 敏感货标识
    private Boolean hasBattery;
    private String batteryType;
    private Boolean isLiquid;
    private Boolean isPowder;
    private Boolean isMagnetic;
    private Boolean isDangerous;

    // 其他
    private String originCountry;
    private String imageUrl;
    private String status;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
