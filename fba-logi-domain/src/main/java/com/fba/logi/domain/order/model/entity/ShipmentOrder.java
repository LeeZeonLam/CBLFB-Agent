package com.fba.logi.domain.order.model.entity;

import com.fba.logi.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 货运订单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentOrder {

    /**
     * 订单 ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户 ID
     */
    private String userId;

    // ==================== 客户关联 ====================

    /**
     * 客户 ID
     */
    private Long customerId;

    /**
     * 客户编号（冗余，方便展示）
     */
    private String customerNo;

    /**
     * 客户公司名称（冗余）
     */
    private String customerName;

    /**
     * 客户地址 ID（当派送地址类型为商业/住宅时使用）
     */
    private Long customerAddressId;

    // ==================== 订单基础信息 ====================

    /**
     * 订单类型：FBA/FBM/自发货
     */
    private String orderType;

    /**
     * 订单状态
     */
    private String state;

    /**
     * 渠道名称：美森快船、以星快船、盐田船等
     */
    private String channelName;

    /**
     * 装柜包装类型：LOOSE(散货)/PALLET(托盘)/FCL(整柜)
     */
    private String packagingType;

    // ==================== 地址信息 ====================

    /**
     * 发货地址
     */
    private String originAddress;

    /**
     * 目的地址
     */
    private String destAddress;

    /**
     * 目的国家
     */
    private String destCountry;

    /**
     * 目的 FBA 仓库代码
     */
    private String fbaWarehouseCode;

    /**
     * 派送地址类型：FBA/AWD/THIRD_PARTY/COMMERCIAL/RESIDENTIAL
     */
    private String deliveryAddressType;

    /**
     * 派送地址详情（JSON 格式存储，根据类型有不同结构）
     */
    private String deliveryAddressDetail;

    /**
     * 交货仓库代码（国内仓库）
     */
    private String deliveryWarehouseCode;

    // ==================== 货物信息 ====================

    /**
     * 货物总重量（KG）
     */
    private BigDecimal totalWeight;

    /**
     * 货物总体积（CBM）
     */
    private BigDecimal totalVolume;

    /**
     * 货物件数
     */
    private Integer totalPieces;

    /**
     * 运输方式：海运/空运/快递
     */
    private String shippingMethod;

    // ==================== 费用信息 ====================

    /**
     * 预计运费
     */
    private BigDecimal estimatedCost;

    /**
     * 实际运费
     */
    private BigDecimal actualCost;

    /**
     * 币种
     */
    private String currency;

    // ==================== 柜子关联 ====================

    /**
     * 关联柜子 ID（排柜后设置）
     */
    private Long containerId;

    /**
     * 柜子编号（冗余字段，方便查询）
     */
    private String containerNo;

    // ==================== 关联数据 ====================

    /**
     * 商品列表
     */
    private List<Product> products;

    /**
     * FBA 信息
     */
    private FbaInfo fbaInfo;

    // ==================== 时间节点 ====================

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 预计到达时间
     */
    private LocalDateTime estimatedArrivalTime;

    /**
     * 装柜时间
     */
    private LocalDateTime loadedTime;

    /**
     * 预计开船时间
     */
    private LocalDateTime estimatedDepartureTime;

    /**
     * 实际开船时间
     */
    private LocalDateTime actualDepartureTime;

    /**
     * 预计到港时间
     */
    private LocalDateTime estimatedArrivalPortTime;

    /**
     * 实际到港时间
     */
    private LocalDateTime actualArrivalPortTime;

    /**
     * 提柜时间
     */
    private LocalDateTime pickedTime;

    /**
     * 派送开始时间
     */
    private LocalDateTime deliveryStartTime;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 备注
     */
    private String remark;

    // ==================== 状态转换规则 ====================

    /**
     * 状态转换规则定义
     */
    private static final Map<String, Set<String>> STATE_TRANSITIONS = Map.ofEntries(
            Map.entry(Constants.OrderState.DRAFT, Set.of(Constants.OrderState.PENDING)),
            Map.entry(Constants.OrderState.PENDING, Set.of(Constants.OrderState.APPROVED, Constants.OrderState.REJECTED)),
            Map.entry(Constants.OrderState.APPROVED, Set.of(Constants.OrderState.RECEIVED)),
            Map.entry(Constants.OrderState.RECEIVED, Set.of(Constants.OrderState.DIMENSION_RECORDED)),
            Map.entry(Constants.OrderState.DIMENSION_RECORDED, Set.of(Constants.OrderState.CONTAINER_ASSIGNED)),
            Map.entry(Constants.OrderState.CONTAINER_ASSIGNED, Set.of(Constants.OrderState.CONTAINER_LOADED)),
            Map.entry(Constants.OrderState.CONTAINER_LOADED, Set.of(Constants.OrderState.DEPARTED)),
            Map.entry(Constants.OrderState.DEPARTED, Set.of(Constants.OrderState.ARRIVED)),
            Map.entry(Constants.OrderState.ARRIVED, Set.of(Constants.OrderState.CONTAINER_PICKED)),
            Map.entry(Constants.OrderState.CONTAINER_PICKED, Set.of(Constants.OrderState.DELIVERING)),
            Map.entry(Constants.OrderState.DELIVERING, Set.of(Constants.OrderState.COMPLETED))
    );

    // ==================== 状态机方法 ====================

    /**
     * 检查状态转换是否合法
     */
    public boolean canTransitionTo(String targetState) {
        Set<String> allowedStates = STATE_TRANSITIONS.get(this.state);
        return allowedStates != null && allowedStates.contains(targetState);
    }

    /**
     * 执行状态转换
     */
    public void transitionTo(String targetState) {
        if (!canTransitionTo(targetState)) {
            throw new IllegalStateException(
                    String.format("不允许从状态 [%s] 转换到 [%s]", this.state, targetState));
        }
        this.state = targetState;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断订单是否可以提交
     */
    public boolean canSubmit() {
        return Constants.OrderState.DRAFT.equals(state);
    }

    /**
     * 判断订单是否可以审核
     */
    public boolean canAudit() {
        return Constants.OrderState.PENDING.equals(state);
    }

    /**
     * 提交订单（DRAFT → PENDING）
     */
    public void submit() {
        transitionTo(Constants.OrderState.PENDING);
    }

    /**
     * 审核通过（PENDING → APPROVED）
     */
    public void approve() {
        transitionTo(Constants.OrderState.APPROVED);
    }

    /**
     * 审核拒绝（PENDING → REJECTED）
     */
    public void reject(String reason) {
        if (!canAudit()) {
            throw new IllegalStateException("订单状态不允许审核");
        }
        this.state = Constants.OrderState.REJECTED;
        this.remark = reason;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 确认收货入仓（APPROVED → RECEIVED）
     */
    public void receive() {
        transitionTo(Constants.OrderState.RECEIVED);
    }

    /**
     * 完成材积录入（RECEIVED → DIMENSION_RECORDED）
     */
    public void recordDimension() {
        transitionTo(Constants.OrderState.DIMENSION_RECORDED);
    }

    /**
     * 分配到柜子/排柜（DIMENSION_RECORDED → CONTAINER_ASSIGNED）
     */
    public void assignContainer(Long containerId, String containerNo) {
        transitionTo(Constants.OrderState.CONTAINER_ASSIGNED);
        this.containerId = containerId;
        this.containerNo = containerNo;
    }

    /**
     * 完成装柜（CONTAINER_ASSIGNED → CONTAINER_LOADED）
     */
    public void loadToContainer() {
        transitionTo(Constants.OrderState.CONTAINER_LOADED);
        this.loadedTime = LocalDateTime.now();
    }

    /**
     * 标记开船（CONTAINER_LOADED → DEPARTED）
     */
    public void depart(LocalDateTime departureTime) {
        transitionTo(Constants.OrderState.DEPARTED);
        this.actualDepartureTime = departureTime;
    }

    /**
     * 标记到港（DEPARTED → ARRIVED）
     */
    public void arrive(LocalDateTime arrivalTime) {
        transitionTo(Constants.OrderState.ARRIVED);
        this.actualArrivalPortTime = arrivalTime;
    }

    /**
     * 标记提柜（ARRIVED → CONTAINER_PICKED）
     */
    public void pickContainer() {
        transitionTo(Constants.OrderState.CONTAINER_PICKED);
        this.pickedTime = LocalDateTime.now();
    }

    /**
     * 开始派送（CONTAINER_PICKED → DELIVERING）
     */
    public void startDelivery() {
        transitionTo(Constants.OrderState.DELIVERING);
        this.deliveryStartTime = LocalDateTime.now();
    }

    /**
     * 完成订单（DELIVERING → COMPLETED）
     */
    public void complete() {
        transitionTo(Constants.OrderState.COMPLETED);
        this.completedTime = LocalDateTime.now();
    }

    // ==================== 兼容旧方法（已废弃） ====================

    /**
     * 标记发货
     * @deprecated 使用 loadToContainer() 和 depart() 代替
     */
    @Deprecated
    public void ship() {
        if (!Constants.OrderState.APPROVED.equals(state)) {
            throw new IllegalStateException("订单状态不允许发货");
        }
        this.state = Constants.OrderState.SHIPPED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记送达
     * @deprecated 使用 complete() 代替
     */
    @Deprecated
    public void deliver() {
        if (!Constants.OrderState.SHIPPED.equals(state)) {
            throw new IllegalStateException("订单状态不允许送达");
        }
        this.state = Constants.OrderState.DELIVERED;
        this.updateTime = LocalDateTime.now();
    }

    // ==================== 业务计算方法 ====================

    /**
     * 计算计费重量
     */
    public BigDecimal calculateChargeableWeight() {
        if (totalVolume == null || totalWeight == null) {
            return BigDecimal.ZERO;
        }
        // 体积重 = 体积 * 167 (航空) 或 * 1000 (海运)
        BigDecimal volumeWeight;
        if ("air".equals(shippingMethod)) {
            volumeWeight = totalVolume.multiply(BigDecimal.valueOf(167));
        } else {
            volumeWeight = totalVolume.multiply(BigDecimal.valueOf(1000));
        }
        // 取实重和体积重的较大值
        return totalWeight.max(volumeWeight);
    }

    /**
     * 获取当前状态的中文描述
     */
    public String getStateDescription() {
        return switch (state) {
            case "draft" -> "草稿";
            case "pending" -> "待审核";
            case "approved" -> "已审核";
            case "rejected" -> "已驳回";
            case "received" -> "货物入仓";
            case "dimension_recorded" -> "材积录入完成";
            case "container_assigned" -> "已排柜";
            case "container_loaded" -> "已装柜";
            case "departed" -> "已开船";
            case "arrived" -> "已到港";
            case "container_picked" -> "已提柜";
            case "delivering" -> "派送中";
            case "completed" -> "完成";
            default -> state;
        };
    }

}
