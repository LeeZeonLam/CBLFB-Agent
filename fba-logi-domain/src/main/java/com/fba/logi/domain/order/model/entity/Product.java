package com.fba.logi.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 商品实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * 商品 ID
     */
    private Long productId;

    /**
     * SKU
     */
    private String sku;

    /**
     * ASIN（亚马逊标准识别号）
     */
    private String asin;

    /**
     * FNSKU（亚马逊配送网络库存单位）
     */
    private String fnsku;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品名称（英文）
     */
    private String productNameEn;

    /**
     * 商品描述
     */
    private String description;

    /**
     * HS 编码
     */
    private String hsCode;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 币种
     */
    private String currency;

    /**
     * 单件重量（KG）
     */
    private BigDecimal unitWeight;

    /**
     * 单件长度（CM）
     */
    private BigDecimal length;

    /**
     * 单件宽度（CM）
     */
    private BigDecimal width;

    /**
     * 单件高度（CM）
     */
    private BigDecimal height;

    /**
     * 是否含电池
     */
    private Boolean hasBattery;

    /**
     * 电池类型
     */
    private String batteryType;

    /**
     * 是否液体
     */
    private Boolean isLiquid;

    /**
     * 是否粉末
     */
    private Boolean isPowder;

    /**
     * 是否磁性
     */
    private Boolean isMagnetic;

    /**
     * 是否危险品
     */
    private Boolean isDangerous;

    /**
     * 原产国
     */
    private String originCountry;

    /**
     * 计算单件体积（CBM）
     */
    public BigDecimal calculateVolume() {
        if (length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        // CM 转 M：除以 100
        return length.multiply(width).multiply(height).divide(BigDecimal.valueOf(1000000), 6, RoundingMode.HALF_UP);
    }

    /**
     * 计算总重量
     */
    public BigDecimal calculateTotalWeight() {
        if (unitWeight == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitWeight.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * 计算总体积
     */
    public BigDecimal calculateTotalVolume() {
        if (quantity == null) {
            return BigDecimal.ZERO;
        }
        return calculateVolume().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * 计算总价值
     */
    public BigDecimal calculateTotalValue() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * 判断是否为敏感货
     */
    public boolean isSensitive() {
        return Boolean.TRUE.equals(hasBattery)
                || Boolean.TRUE.equals(isLiquid)
                || Boolean.TRUE.equals(isPowder)
                || Boolean.TRUE.equals(isMagnetic);
    }

}
