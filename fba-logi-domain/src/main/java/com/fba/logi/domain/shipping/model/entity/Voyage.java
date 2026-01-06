package com.fba.logi.domain.shipping.model.entity;

import com.fba.logi.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 航次/船运实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voyage {

    /**
     * 航次 ID
     */
    private Long voyageId;

    /**
     * 航次编号
     */
    private String voyageNo;

    /**
     * 船名
     */
    private String vesselName;

    /**
     * 船公司
     */
    private String carrier;

    /**
     * 渠道名称（美森快船、以星快船、盐田船等）
     */
    private String channelName;

    /**
     * 起运港
     */
    private String departurePort;

    /**
     * 目的港
     */
    private String arrivalPort;

    /**
     * 预计开船时间（ETD）
     */
    private LocalDateTime estimatedDeparture;

    /**
     * 实际开船时间（ATD）
     */
    private LocalDateTime actualDeparture;

    /**
     * 预计到港时间（ETA）
     */
    private LocalDateTime estimatedArrival;

    /**
     * 实际到港时间（ATA）
     */
    private LocalDateTime actualArrival;

    /**
     * 状态：SCHEDULED/DEPARTED/IN_TRANSIT/ARRIVED/COMPLETED
     */
    private String status;

    /**
     * 包含的柜子 ID 列表
     */
    @Builder.Default
    private List<Long> containerIds = new ArrayList<>();

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

    // ==================== 业务方法 ====================

    /**
     * 判断是否可以开船
     */
    public boolean canDepart() {
        return Constants.VoyageState.SCHEDULED.equals(status)
                && containerIds != null
                && !containerIds.isEmpty();
    }

    /**
     * 判断是否可以到港
     */
    public boolean canArrive() {
        return Constants.VoyageState.DEPARTED.equals(status)
                || Constants.VoyageState.IN_TRANSIT.equals(status);
    }

    /**
     * 获取柜子数量
     */
    public int getContainerCount() {
        return containerIds != null ? containerIds.size() : 0;
    }

    /**
     * 计算预计航程天数
     */
    public long getEstimatedTransitDays() {
        if (estimatedDeparture == null || estimatedArrival == null) {
            return 0;
        }
        return Duration.between(estimatedDeparture, estimatedArrival).toDays();
    }

    /**
     * 计算实际航程天数
     */
    public long getActualTransitDays() {
        if (actualDeparture == null || actualArrival == null) {
            return 0;
        }
        return Duration.between(actualDeparture, actualArrival).toDays();
    }

    /**
     * 添加柜子到航次
     */
    public void addContainer(Long containerId) {
        if (containerIds == null) {
            containerIds = new ArrayList<>();
        }
        if (!containerIds.contains(containerId)) {
            containerIds.add(containerId);
            this.updateTime = LocalDateTime.now();
        }
    }

    /**
     * 从航次移除柜子
     */
    public void removeContainer(Long containerId) {
        if (containerIds != null) {
            containerIds.remove(containerId);
            this.updateTime = LocalDateTime.now();
        }
    }

    // ==================== 状态转换方法 ====================

    /**
     * 开船
     */
    public void depart(LocalDateTime actualDepartureTime) {
        if (!canDepart()) {
            throw new IllegalStateException("航次状态不允许开船或没有柜子");
        }
        this.status = Constants.VoyageState.DEPARTED;
        this.actualDeparture = actualDepartureTime;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记运输中
     */
    public void inTransit() {
        if (!Constants.VoyageState.DEPARTED.equals(status)) {
            throw new IllegalStateException("只有已开船的航次才能标记运输中");
        }
        this.status = Constants.VoyageState.IN_TRANSIT;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 到港
     */
    public void arrive(LocalDateTime actualArrivalTime) {
        if (!canArrive()) {
            throw new IllegalStateException("航次状态不允许到港");
        }
        this.status = Constants.VoyageState.ARRIVED;
        this.actualArrival = actualArrivalTime;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 完成航次
     */
    public void complete() {
        if (!Constants.VoyageState.ARRIVED.equals(status)) {
            throw new IllegalStateException("只有已到港的航次才能完成");
        }
        this.status = Constants.VoyageState.COMPLETED;
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
            case "scheduled" -> "已排期";
            case "departed" -> "已开船";
            case "in_transit" -> "运输中";
            case "arrived" -> "已到港";
            case "completed" -> "完成";
            default -> status;
        };
    }

    /**
     * 获取渠道的中文名称
     */
    public String getChannelDisplayName() {
        if (channelName == null) {
            return "未知渠道";
        }
        return switch (channelName) {
            case Constants.ChannelType.MATSON -> "美森快船";
            case Constants.ChannelType.ZIM -> "以星快船";
            case Constants.ChannelType.YANTIAN -> "盐田船";
            default -> channelName;
        };
    }
}
