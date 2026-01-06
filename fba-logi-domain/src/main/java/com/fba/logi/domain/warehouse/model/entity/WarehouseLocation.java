package com.fba.logi.domain.warehouse.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仓库库位实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseLocation {

    /**
     * 库位 ID
     */
    private Long locationId;

    /**
     * 库位编号
     */
    private String locationCode;

    /**
     * 仓库代码
     */
    private String warehouseCode;

    /**
     * 区域：A/B/C
     */
    private String zone;

    /**
     * 排号
     */
    private String row;

    /**
     * 列号
     */
    private String column;

    /**
     * 层号
     */
    private String level;

    /**
     * 库位类型：托盘位/散货位
     */
    private String locationType;

    /**
     * 最大承重（KG）
     */
    private Integer maxWeight;

    /**
     * 当前占用重量（KG）
     */
    private Integer currentWeight;

    /**
     * 状态：空闲/占用/锁定
     */
    private String status;

    /**
     * 是否可用于敏感货
     */
    private Boolean allowSensitive;

    /**
     * 温控要求
     */
    private String temperatureControl;

    /**
     * 备注
     */
    private String remark;

    /**
     * 获取完整库位编号
     * 格式：仓库-区域-排-列-层
     */
    public String getFullLocationCode() {
        return String.format("%s-%s-%s-%s-%s",
                warehouseCode, zone, row, column, level);
    }

    /**
     * 判断库位是否可用
     */
    public boolean isAvailable() {
        return "free".equals(status);
    }

    /**
     * 判断是否可以存放指定重量
     */
    public boolean canStore(int weight) {
        if (!isAvailable()) {
            return false;
        }
        if (maxWeight == null) {
            return true;
        }
        int available = maxWeight - (currentWeight != null ? currentWeight : 0);
        return weight <= available;
    }

    /**
     * 占用库位
     */
    public void occupy(int weight) {
        if (!canStore(weight)) {
            throw new IllegalStateException("库位容量不足");
        }
        this.currentWeight = (currentWeight != null ? currentWeight : 0) + weight;
        this.status = "occupied";
    }

    /**
     * 释放库位
     */
    public void release(int weight) {
        if (currentWeight != null) {
            this.currentWeight = currentWeight - weight;
            if (this.currentWeight <= 0) {
                this.currentWeight = 0;
                this.status = "free";
            }
        }
    }

    /**
     * 锁定库位
     */
    public void lock() {
        this.status = "locked";
    }

    /**
     * 解锁库位
     */
    public void unlock() {
        if (currentWeight != null && currentWeight > 0) {
            this.status = "occupied";
        } else {
            this.status = "free";
        }
    }

    /**
     * 获取剩余容量
     */
    public int getRemainingCapacity() {
        if (maxWeight == null) {
            return Integer.MAX_VALUE;
        }
        return maxWeight - (currentWeight != null ? currentWeight : 0);
    }

}
