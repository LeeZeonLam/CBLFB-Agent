package com.fba.logi.domain.warehouse.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 纸箱实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Carton {

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
     * 状态：待入库/已入库/已上托/已出库
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

    /**
     * 计算体积（CBM）
     */
    public BigDecimal calculateVolume() {
        if (length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        return length.multiply(width).multiply(height)
                .divide(BigDecimal.valueOf(1000000), 6, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算体积重（KG）
     * 体积重 = 长 x 宽 x 高 / 5000
     */
    public BigDecimal calculateVolumeWeight() {
        if (length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        return length.multiply(width).multiply(height)
                .divide(BigDecimal.valueOf(5000), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取计费重量
     * 取实重和体积重的较大值
     */
    public BigDecimal getChargeableWeight() {
        BigDecimal volumeWeight = calculateVolumeWeight();
        if (weight == null) {
            return volumeWeight;
        }
        return weight.max(volumeWeight);
    }

    /**
     * 判断是否超尺寸（亚马逊标准：最大边不超过 63.5cm）
     */
    public boolean isOversized() {
        BigDecimal maxSide = BigDecimal.valueOf(63.5);
        return (length != null && length.compareTo(maxSide) > 0)
                || (width != null && width.compareTo(maxSide) > 0)
                || (height != null && height.compareTo(maxSide) > 0);
    }

    /**
     * 判断是否超重（亚马逊标准：单箱不超过 22.7kg）
     */
    public boolean isOverweight() {
        BigDecimal maxWeight = new BigDecimal("22.7");
        return weight != null && weight.compareTo(maxWeight) > 0;
    }

    /**
     * 判断是否已分配到托盘
     */
    public boolean isAssignedToPallet() {
        return palletId != null;
    }

    /**
     * 生成 FBA 标签号
     */
    public String generateFbaLabel(String shipmentId, int sequence) {
        return String.format("FBA%s%04d", shipmentId, sequence);
    }

}
