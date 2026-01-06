package com.fba.logi.domain.shipping.model.entity;

import com.fba.logi.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 集装箱/柜子实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Container {

    /**
     * 柜子 ID
     */
    private Long containerId;

    /**
     * 柜子编号
     */
    private String containerNo;

    /**
     * 柜型：20GP/40GP/40HQ/45HQ
     */
    private String containerType;

    /**
     * 封号
     */
    private String sealNo;

    /**
     * 最大容量（CBM）
     */
    private BigDecimal maxCapacity;

    /**
     * 最大载重（KG）
     */
    private BigDecimal maxWeight;

    /**
     * 当前已用容量（CBM）
     */
    private BigDecimal currentCapacity;

    /**
     * 当前已用重量（KG）
     */
    private BigDecimal currentWeight;

    /**
     * 关联航次 ID
     */
    private Long voyageId;

    /**
     * 航次编号（冗余字段）
     */
    private String voyageNo;

    /**
     * 状态：EMPTY/ASSIGNING/LOADING/LOADED/DEPARTED/ARRIVED/PICKED/DELIVERED
     */
    private String status;

    /**
     * 包含的订单 ID 列表
     */
    @Builder.Default
    private List<Long> orderIds = new ArrayList<>();

    /**
     * 装柜开始时间
     */
    private LocalDateTime loadStartTime;

    /**
     * 装柜完成时间
     */
    private LocalDateTime loadEndTime;

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

    // ==================== 柜型容量常量 ====================

    /** 20GP 最大容量 CBM */
    public static final BigDecimal CAPACITY_20GP = BigDecimal.valueOf(28);
    /** 40GP 最大容量 CBM */
    public static final BigDecimal CAPACITY_40GP = BigDecimal.valueOf(58);
    /** 40HQ 最大容量 CBM */
    public static final BigDecimal CAPACITY_40HQ = BigDecimal.valueOf(68);
    /** 45HQ 最大容量 CBM */
    public static final BigDecimal CAPACITY_45HQ = BigDecimal.valueOf(78);

    /** 20GP 最大载重 KG */
    public static final BigDecimal WEIGHT_20GP = BigDecimal.valueOf(17500);
    /** 40GP 最大载重 KG */
    public static final BigDecimal WEIGHT_40GP = BigDecimal.valueOf(22000);
    /** 40HQ 最大载重 KG */
    public static final BigDecimal WEIGHT_40HQ = BigDecimal.valueOf(22000);
    /** 45HQ 最大载重 KG */
    public static final BigDecimal WEIGHT_45HQ = BigDecimal.valueOf(26000);

    // ==================== 业务方法 ====================

    /**
     * 根据柜型初始化容量和载重
     */
    public void initByContainerType() {
        if (containerType == null) {
            return;
        }
        switch (containerType) {
            case Constants.ContainerType.GP20 -> {
                this.maxCapacity = CAPACITY_20GP;
                this.maxWeight = WEIGHT_20GP;
            }
            case Constants.ContainerType.GP40 -> {
                this.maxCapacity = CAPACITY_40GP;
                this.maxWeight = WEIGHT_40GP;
            }
            case Constants.ContainerType.HQ40 -> {
                this.maxCapacity = CAPACITY_40HQ;
                this.maxWeight = WEIGHT_40HQ;
            }
            case Constants.ContainerType.HQ45 -> {
                this.maxCapacity = CAPACITY_45HQ;
                this.maxWeight = WEIGHT_45HQ;
            }
        }
        this.currentCapacity = BigDecimal.ZERO;
        this.currentWeight = BigDecimal.ZERO;
        this.status = Constants.ContainerState.EMPTY;
    }

    /**
     * 计算剩余容量（CBM）
     */
    public BigDecimal getRemainingCapacity() {
        if (maxCapacity == null) {
            return BigDecimal.ZERO;
        }
        return maxCapacity.subtract(currentCapacity != null ? currentCapacity : BigDecimal.ZERO);
    }

    /**
     * 计算剩余载重（KG）
     */
    public BigDecimal getRemainingWeight() {
        if (maxWeight == null) {
            return BigDecimal.ZERO;
        }
        return maxWeight.subtract(currentWeight != null ? currentWeight : BigDecimal.ZERO);
    }

    /**
     * 判断是否可以装入指定货物
     */
    public boolean canLoad(BigDecimal volume, BigDecimal weight) {
        return getRemainingCapacity().compareTo(volume) >= 0
                && getRemainingWeight().compareTo(weight) >= 0;
    }

    /**
     * 添加货物到柜子
     */
    public void addCargo(BigDecimal volume, BigDecimal weight, Long orderId) {
        if (!canLoad(volume, weight)) {
            throw new IllegalStateException("柜子容量或载重不足");
        }
        this.currentCapacity = (this.currentCapacity != null ? this.currentCapacity : BigDecimal.ZERO).add(volume);
        this.currentWeight = (this.currentWeight != null ? this.currentWeight : BigDecimal.ZERO).add(weight);
        if (orderId != null && !this.orderIds.contains(orderId)) {
            this.orderIds.add(orderId);
        }
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断是否可以开船
     */
    public boolean canDepart() {
        return Constants.ContainerState.LOADED.equals(status) && voyageId != null;
    }

    /**
     * 判断是否可以提柜
     */
    public boolean canPick() {
        return Constants.ContainerState.ARRIVED.equals(status);
    }

    /**
     * 获取装载率（按容量）
     */
    public BigDecimal getLoadRateByCapacity() {
        if (maxCapacity == null || maxCapacity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentCapacity.divide(maxCapacity, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取装载率（按重量）
     */
    public BigDecimal getLoadRateByWeight() {
        if (maxWeight == null || maxWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentWeight.divide(maxWeight, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取订单数量
     */
    public int getOrderCount() {
        return orderIds != null ? orderIds.size() : 0;
    }

    // ==================== 状态转换方法 ====================

    /**
     * 开始排柜
     */
    public void startAssigning() {
        if (!Constants.ContainerState.EMPTY.equals(status)) {
            throw new IllegalStateException("只有空柜才能开始排柜");
        }
        this.status = Constants.ContainerState.ASSIGNING;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 开始装柜
     */
    public void startLoading() {
        if (!Constants.ContainerState.ASSIGNING.equals(status)) {
            throw new IllegalStateException("只有排柜中的柜子才能开始装柜");
        }
        this.status = Constants.ContainerState.LOADING;
        this.loadStartTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 完成装柜
     */
    public void finishLoading() {
        if (!Constants.ContainerState.LOADING.equals(status)) {
            throw new IllegalStateException("只有装柜中的柜子才能完成装柜");
        }
        this.status = Constants.ContainerState.LOADED;
        this.loadEndTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记开船
     */
    public void depart() {
        if (!Constants.ContainerState.LOADED.equals(status)) {
            throw new IllegalStateException("只有已装柜的柜子才能开船");
        }
        this.status = Constants.ContainerState.DEPARTED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记到港
     */
    public void arrive() {
        if (!Constants.ContainerState.DEPARTED.equals(status)) {
            throw new IllegalStateException("只有已开船的柜子才能到港");
        }
        this.status = Constants.ContainerState.ARRIVED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记提柜
     */
    public void pick() {
        if (!Constants.ContainerState.ARRIVED.equals(status)) {
            throw new IllegalStateException("只有已到港的柜子才能提柜");
        }
        this.status = Constants.ContainerState.PICKED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记派送完成
     */
    public void deliver() {
        if (!Constants.ContainerState.PICKED.equals(status)) {
            throw new IllegalStateException("只有已提柜的柜子才能派送");
        }
        this.status = Constants.ContainerState.DELIVERED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 获取当前状态的中文描述
     */
    public String getStatusDescription() {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case "empty" -> "空柜";
            case "assigning" -> "排柜中";
            case "loading" -> "装柜中";
            case "loaded" -> "已装柜";
            case "departed" -> "已开船";
            case "arrived" -> "已到港";
            case "picked" -> "已提柜";
            case "delivered" -> "已派送";
            default -> status;
        };
    }
}
