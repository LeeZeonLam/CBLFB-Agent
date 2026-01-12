package com.fba.logi.agent.skill.logistics;

import com.fba.logi.agent.skill.*;
import com.fba.logi.infrastructure.adapter.huolala.HuolalaClient;
import com.fba.logi.infrastructure.adapter.huolala.dto.HuolalaOrderDetail;
import com.fba.logi.infrastructure.adapter.huolala.dto.HuolalaOrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 货拉拉订单物流追踪 Skill
 *
 * 查询货拉拉订单的实时物流状态和轨迹
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrackHuolalaOrderSkill extends AbstractSkill {

    private final HuolalaClient huolalaClient;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    @Override
    public String getSkillId() {
        return "track_huolala_order";
    }

    @Override
    public String getSkillName() {
        return "货拉拉物流追踪";
    }

    @Override
    public String getDescription() {
        return "查询货拉拉订单的实时物流状态，显示当前运输节点和完整物流轨迹";
    }

    @Override
    public String getDomain() {
        return "logistics";
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "orderNo", "货拉拉订单号（如 HL20250108001234）")
                ))
                .required(List.of("orderNo"))
                .build();
    }

    @Override
    public SkillValidationResult validateParameters(Map<String, Object> parameters) {
        Object orderNo = parameters.get("orderNo");
        if (orderNo == null || orderNo.toString().trim().isEmpty()) {
            return SkillValidationResult.failure("订单号不能为空");
        }
        return SkillValidationResult.success();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String orderNo = getRequiredString(parameters, "orderNo").trim();
        log.info("查询货拉拉订单物流: {}", orderNo);

        // 调用货拉拉 API 查询订单
        Optional<HuolalaOrderDetail> orderOpt = huolalaClient.getOrderDetail(orderNo);

        if (orderOpt.isEmpty()) {
            return SkillResult.failure(
                    String.format("未找到订单 %s，请检查订单号是否正确", orderNo),
                    "ORDER_NOT_FOUND"
            );
        }

        HuolalaOrderDetail order = orderOpt.get();
        String resultMessage = formatOrderDetail(order);
        Map<String, Object> data = buildResultData(order);

        log.info("订单 {} 当前状态: {}", orderNo, order.getStatus().getName());

        return SkillResult.success(resultMessage, data);
    }

    /**
     * 格式化订单详情为可读文本
     */
    private String formatOrderDetail(HuolalaOrderDetail order) {
        StringBuilder sb = new StringBuilder();

        sb.append("══════════════════════════════════════════════\n");
        sb.append("  货拉拉订单物流追踪\n");
        sb.append("══════════════════════════════════════════════\n\n");

        // 基本信息
        sb.append(String.format("订单号: %s\n", order.getOrderDisplayId()));
        sb.append(String.format("当前状态: %s %s\n",
                getStatusEmoji(order.getStatus()),
                order.getStatus().getName()));
        sb.append("\n");

        // 物流轨迹
        sb.append("── 物流轨迹 ──────────────────────────────────\n\n");
        formatTrackingNodes(sb, order.getTrackingNodes(), order.getStatus());

        // 订单信息
        sb.append("\n── 订单信息 ──────────────────────────────────\n\n");
        sb.append(String.format("取货地址: %s\n", order.getPickupAddress()));
        sb.append(String.format("送货地址: %s\n", order.getDeliveryAddress()));
        sb.append(String.format("车    型: %s\n", order.getVehicleType()));

        if (order.getDriverName() != null) {
            sb.append(String.format("司    机: %s (%s)\n",
                    order.getDriverName(), order.getDriverPhone()));
            sb.append(String.format("车 牌 号: %s\n", order.getLicensePlate()));
        }

        if (order.getOrderTime() != null) {
            sb.append(String.format("下单时间: %s\n",
                    order.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        }

        if (order.getEstimatedArrival() != null && order.getStatus().isInProgress()) {
            sb.append(String.format("预计送达: %s\n",
                    order.getEstimatedArrival().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        }

        if (order.getCompletedTime() != null) {
            sb.append(String.format("完成时间: %s\n",
                    order.getCompletedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        }

        sb.append("\n══════════════════════════════════════════════\n");

        return sb.toString();
    }

    /**
     * 格式化物流轨迹节点
     */
    private void formatTrackingNodes(StringBuilder sb, List<HuolalaOrderDetail.TrackingNode> nodes,
                                      HuolalaOrderStatus currentStatus) {
        if (nodes == null || nodes.isEmpty()) {
            sb.append("  暂无轨迹信息\n");
            return;
        }

        // 倒序显示（最新的在前）
        for (int i = nodes.size() - 1; i >= 0; i--) {
            HuolalaOrderDetail.TrackingNode node = nodes.get(i);
            boolean isCurrent = (i == nodes.size() - 1);
            String marker = isCurrent ? "●" : "○";
            String timeStr = node.getTime() != null ? node.getTime().format(TIME_FORMATTER) : "";

            sb.append(String.format("  %s %s - %s\n", marker, timeStr, node.getDescription()));

            if (i > 0) {
                sb.append("  │\n");
            }
        }
    }

    /**
     * 获取状态对应的图标
     */
    private String getStatusEmoji(HuolalaOrderStatus status) {
        return switch (status) {
            case PENDING -> "[等待]";
            case ACCEPTED -> "[接单]";
            case HEADING_TO_PICKUP -> "[出发]";
            case ARRIVED_PICKUP -> "[到达]";
            case LOADING -> "[装货]";
            case IN_TRANSIT -> "[运输]";
            case ARRIVED_DELIVERY -> "[到达]";
            case UNLOADING -> "[卸货]";
            case COMPLETED -> "[完成]";
            case CANCELLED -> "[取消]";
        };
    }

    /**
     * 构建结果数据
     */
    private Map<String, Object> buildResultData(HuolalaOrderDetail order) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getOrderDisplayId());
        data.put("statusCode", order.getStatus().getCode());
        data.put("statusName", order.getStatus().getName());
        data.put("statusDescription", order.getStatus().getDescription());
        data.put("pickupAddress", order.getPickupAddress());
        data.put("deliveryAddress", order.getDeliveryAddress());
        data.put("vehicleType", order.getVehicleType());
        data.put("driverName", order.getDriverName());
        data.put("driverPhone", order.getDriverPhone());
        data.put("licensePlate", order.getLicensePlate());
        data.put("isCompleted", order.getStatus().isFinal());
        data.put("isInProgress", order.getStatus().isInProgress());

        if (order.getDriverLongitude() != null && order.getDriverLatitude() != null) {
            data.put("driverLocation", Map.of(
                    "longitude", order.getDriverLongitude(),
                    "latitude", order.getDriverLatitude()
            ));
        }

        return data;
    }
}
