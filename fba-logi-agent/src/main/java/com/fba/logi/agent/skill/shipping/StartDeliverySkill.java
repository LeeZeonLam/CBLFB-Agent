package com.fba.logi.agent.skill.shipping;

import com.fba.logi.agent.skill.*;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.order.model.entity.ShipmentOrder;
import com.fba.logi.domain.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 开始派送 Skill - 订单开始派送到最终目的地
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartDeliverySkill extends AbstractSkill {

    private final IOrderService orderService;

    @Override
    public String getSkillId() {
        return "start_delivery";
    }

    @Override
    public String getSkillName() {
        return "开始派送";
    }

    @Override
    public String getDescription() {
        return "订单开始派送到最终目的地（FBA仓库/海外仓/商业地址/住宅地址）";
    }

    @Override
    public String getDomain() {
        return "shipping";
    }

    @Override
    public boolean requiresConfirmation() {
        return false;
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "orderNo", "订单号"),
                        SkillParameterSchema.ParameterDefinition.enumParam(
                                "deliveryMethod", "派送方式",
                                List.of("TRUCK", "UPS", "FEDEX", "USPS", "DHL", "OTHER")),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "trackingNo", "跟踪单号（可选）")
                ))
                .required(List.of("orderNo", "deliveryMethod"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String orderNo = getRequiredString(parameters, "orderNo");
        String deliveryMethod = getRequiredString(parameters, "deliveryMethod");
        String trackingNo = getOptionalString(parameters, "trackingNo", "");

        try {
            // 查询订单
            ShipmentOrder order = orderService.getOrderByNo(orderNo);
            if (order == null) {
                return SkillResult.failure("订单不存在: " + orderNo, "ORDER_NOT_FOUND");
            }

            // 检查订单状态
            if (!Constants.OrderState.CONTAINER_PICKED.equals(order.getState())) {
                return SkillResult.failure(
                        "订单状态不允许开始派送，当前状态: " + order.getState(),
                        "INVALID_ORDER_STATE");
            }

            // 开始派送
            orderService.startDelivery(order.getOrderId(), deliveryMethod, trackingNo);

            log.info("开始派送: 订单 {}, 方式 {}, 跟踪号 {}", orderNo, deliveryMethod, trackingNo);

            // 构建派送地址信息
            String destInfo = buildDestinationInfo(order);

            return SkillResult.success(
                    String.format("派送已开始！\n订单号: %s\n派送方式: %s\n跟踪号: %s\n目的地: %s",
                            orderNo,
                            deliveryMethod,
                            trackingNo.isEmpty() ? "未提供" : trackingNo,
                            destInfo),
                    Map.of(
                            "orderNo", orderNo,
                            "deliveryMethod", deliveryMethod,
                            "trackingNo", trackingNo,
                            "status", Constants.OrderState.DELIVERING
                    )
            ).withNextSkill("complete_order");
        } catch (Exception e) {
            log.error("开始派送失败: {}", e.getMessage(), e);
            return SkillResult.failure("开始派送失败: " + e.getMessage(), "DELIVERY_START_FAILED");
        }
    }

    /**
     * 构建目的地信息描述
     */
    private String buildDestinationInfo(ShipmentOrder order) {
        String addressType = order.getDeliveryAddressType();
        if (addressType == null) {
            return order.getDestCountry();
        }

        switch (addressType) {
            case Constants.DeliveryAddressType.FBA:
                return String.format("FBA仓库 %s", order.getFbaWarehouseCode());
            case Constants.DeliveryAddressType.AWD:
                return String.format("AWD仓库 %s", order.getFbaWarehouseCode());
            case Constants.DeliveryAddressType.THIRD_PARTY:
                return "第三方海外仓";
            case Constants.DeliveryAddressType.COMMERCIAL:
                return "商业地址";
            case Constants.DeliveryAddressType.RESIDENTIAL:
                return "住宅地址";
            default:
                return order.getDestCountry();
        }
    }
}
