package com.fba.logi.domain.warehouse.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 托盘实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pallet {

    /**
     * 托盘 ID
     */
    private Long palletId;

    /**
     * 托盘编号
     */
    private String palletNo;

    /**
     * 关联订单 ID
     */
    private Long orderId;

    /**
     * 托盘类型：标准/重型
     */
    private String palletType;

    /**
     * 托盘规格（英寸）
     */
    private String palletSize;

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
     * 总重量（KG）
     */
    private BigDecimal totalWeight;

    /**
     * 包含的纸箱列表
     */
    @Builder.Default
    private List<Carton> cartons = new ArrayList<>();

    /**
     * 仓库位置
     */
    private WarehouseLocation location;

    /**
     * 状态：待打托/已打托/已入库/已出库
     */
    private String status;

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
     * 计算托盘体积（CBM）
     */
    public BigDecimal calculateVolume() {
        if (length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        return length.multiply(width).multiply(height)
                .divide(BigDecimal.valueOf(1000000), 6, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 添加纸箱
     */
    public void addCarton(Carton carton) {
        if (cartons == null) {
            cartons = new ArrayList<>();
        }
        cartons.add(carton);
        recalculateWeight();
    }

    /**
     * 移除纸箱
     */
    public void removeCarton(String cartonNo) {
        if (cartons != null) {
            cartons.removeIf(c -> c.getCartonNo().equals(cartonNo));
            recalculateWeight();
        }
    }

    /**
     * 重新计算总重量
     */
    public void recalculateWeight() {
        if (cartons == null || cartons.isEmpty()) {
            this.totalWeight = BigDecimal.ZERO;
            return;
        }
        this.totalWeight = cartons.stream()
                .map(Carton::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取纸箱数量
     */
    public int getCartonCount() {
        return cartons != null ? cartons.size() : 0;
    }

    /**
     * 判断是否超重（标准托盘限重 1000KG）
     */
    public boolean isOverweight() {
        BigDecimal limit = "heavy".equals(palletType)
                ? new BigDecimal("2000")
                : new BigDecimal("1000");
        return totalWeight != null && totalWeight.compareTo(limit) > 0;
    }

    /**
     * 判断是否已入库
     */
    public boolean isInWarehouse() {
        return "stored".equals(status);
    }

}
