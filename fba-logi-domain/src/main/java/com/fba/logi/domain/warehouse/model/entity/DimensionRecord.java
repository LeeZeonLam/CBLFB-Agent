package com.fba.logi.domain.warehouse.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 材积记录实体（支持多次测量）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionRecord {

    /**
     * 记录 ID
     */
    private Long recordId;

    /**
     * 关联订单 ID
     */
    private Long orderId;

    /**
     * 关联纸箱 ID（可选）
     */
    private Long cartonId;

    /**
     * 纸箱编号
     */
    private String cartonNo;

    /**
     * 测量序号（第几次测量）
     */
    private Integer measureSequence;

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
     * 实际重量（KG）
     */
    private BigDecimal actualWeight;

    /**
     * 体积（CBM）
     */
    private BigDecimal volume;

    /**
     * 体积重（KG）
     */
    private BigDecimal volumetricWeight;

    /**
     * 计费重（KG）
     */
    private BigDecimal chargeableWeight;

    /**
     * 是否超尺寸
     */
    private Boolean oversized;

    /**
     * 是否超重
     */
    private Boolean overweight;

    /**
     * 测量人员
     */
    private String measuredBy;

    /**
     * 测量时间
     */
    private LocalDateTime measuredTime;

    /**
     * 是否为最终确认值
     */
    private Boolean isFinal;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    // ==================== FBA 标准常量 ====================

    /** FBA 标准尺寸最大重量 KG */
    public static final BigDecimal FBA_STANDARD_MAX_WEIGHT = BigDecimal.valueOf(22.68);
    /** FBA 标准尺寸最大长度 CM */
    public static final BigDecimal FBA_STANDARD_MAX_LENGTH = BigDecimal.valueOf(45.72);
    /** FBA 标准尺寸最大宽度 CM */
    public static final BigDecimal FBA_STANDARD_MAX_WIDTH = BigDecimal.valueOf(35.56);
    /** FBA 标准尺寸最大高度 CM */
    public static final BigDecimal FBA_STANDARD_MAX_HEIGHT = BigDecimal.valueOf(20.32);

    /** FBA 大件最大重量 KG */
    public static final BigDecimal FBA_OVERSIZE_MAX_WEIGHT = BigDecimal.valueOf(68.04);
    /** FBA 大件最大边长 CM */
    public static final BigDecimal FBA_OVERSIZE_MAX_LENGTH = BigDecimal.valueOf(152.4);
    /** FBA 大件最大 长+周长 CM */
    public static final BigDecimal FBA_OVERSIZE_MAX_GIRTH = BigDecimal.valueOf(330.2);

    // ==================== 业务方法 ====================

    /**
     * 计算体积（CBM）
     */
    public BigDecimal calculateVolume() {
        if (length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        // 体积 = 长 * 宽 * 高 / 1000000 (CM 转 CBM)
        return length.multiply(width).multiply(height)
                .divide(BigDecimal.valueOf(1000000), 6, RoundingMode.HALF_UP);
    }

    /**
     * 计算体积重（KG）- 海运按 1:1000
     */
    public BigDecimal calculateVolumetricWeightSea() {
        BigDecimal vol = calculateVolume();
        return vol.multiply(BigDecimal.valueOf(1000)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算体积重（KG）- 空运按 1:167
     */
    public BigDecimal calculateVolumetricWeightAir() {
        BigDecimal vol = calculateVolume();
        return vol.multiply(BigDecimal.valueOf(167)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算计费重（海运）
     */
    public BigDecimal calculateChargeableWeightSea() {
        BigDecimal volWeight = calculateVolumetricWeightSea();
        if (actualWeight == null) {
            return volWeight;
        }
        return actualWeight.max(volWeight);
    }

    /**
     * 计算计费重（空运）
     */
    public BigDecimal calculateChargeableWeightAir() {
        BigDecimal volWeight = calculateVolumetricWeightAir();
        if (actualWeight == null) {
            return volWeight;
        }
        return actualWeight.max(volWeight);
    }

    /**
     * 计算所有值并填充
     */
    public void calculateAndFill() {
        this.volume = calculateVolume();
        this.volumetricWeight = calculateVolumetricWeightSea();
        this.chargeableWeight = calculateChargeableWeightSea();
        this.oversized = checkOversized();
        this.overweight = checkOverweight();
    }

    /**
     * 检查是否超尺寸（FBA 标准）
     */
    public boolean checkOversized() {
        if (length == null || width == null || height == null) {
            return false;
        }
        // 找出最大边
        BigDecimal maxDimension = length.max(width).max(height);
        // 如果最大边超过标准尺寸限制
        return maxDimension.compareTo(FBA_OVERSIZE_MAX_LENGTH) > 0;
    }

    /**
     * 检查是否超重（FBA 标准）
     */
    public boolean checkOverweight() {
        if (actualWeight == null) {
            return false;
        }
        return actualWeight.compareTo(FBA_OVERSIZE_MAX_WEIGHT) > 0;
    }

    /**
     * 判断是否为 FBA 标准尺寸
     */
    public boolean isFbaStandardSize() {
        if (length == null || width == null || height == null || actualWeight == null) {
            return false;
        }
        // 重量检查
        if (actualWeight.compareTo(FBA_STANDARD_MAX_WEIGHT) > 0) {
            return false;
        }
        // 尺寸检查（需要按照长宽高排序）
        BigDecimal[] dims = {length, width, height};
        java.util.Arrays.sort(dims, java.util.Comparator.reverseOrder());
        return dims[0].compareTo(FBA_STANDARD_MAX_LENGTH) <= 0
                && dims[1].compareTo(FBA_STANDARD_MAX_WIDTH) <= 0
                && dims[2].compareTo(FBA_STANDARD_MAX_HEIGHT) <= 0;
    }

    /**
     * 获取货物分类
     */
    public String getSizeCategory() {
        if (isFbaStandardSize()) {
            return "标准尺寸";
        } else if (!checkOversized() && !checkOverweight()) {
            return "大件";
        } else {
            return "超大件";
        }
    }

    /**
     * 标记为最终确认值
     */
    public void markAsFinal() {
        this.isFinal = true;
    }
}
