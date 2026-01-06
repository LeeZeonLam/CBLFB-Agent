package com.fba.logi.agent.skill.shipping;

import com.fba.logi.agent.skill.*;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.order.model.entity.ShipmentOrder;
import com.fba.logi.domain.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 完成订单 Skill - 确认订单已签收完成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteOrderSkill extends AbstractSkill {

    private final IOrderService orderService;

    @Override
    public String getSkillId() {
        return "complete_order";
    }

    @Override
    public String getSkillName() {
        return "完成订单";
    }

    @Override
    public String getDescription() {
        return "确认订单已送达并签收，完成订单流程";
    }

    @Override
    public String getDomain() {
        return "shipping";
    }

    @Override
    public boolean requiresConfirmation() {
        return true;
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "orderNo", "订单号"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "signedBy", "签收人")
                ))
                .required(List.of("orderNo", "signedBy"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String orderNo = getRequiredString(parameters, "orderNo");
        String signedBy = getRequiredString(parameters, "signedBy");

        try {
            // 查询订单
            ShipmentOrder order = orderService.getOrderByNo(orderNo);
            if (order == null) {
                return SkillResult.failure("订单不存在: " + orderNo, "ORDER_NOT_FOUND");
            }

            // 检查订单状态
            if (!Constants.OrderState.DELIVERING.equals(order.getState())) {
                return SkillResult.failure(
                        "订单状态不允许完成，当前状态: " + order.getState(),
                        "INVALID_ORDER_STATE");
            }

            // 完成订单
            orderService.completeOrder(order.getOrderId(), signedBy);

            // 计算全程时效
            String transitTime = calculateTransitTime(order);

            log.info("订单完成: 订单 {}, 签收人 {}", orderNo, signedBy);

            return SkillResult.success(
                    String.format("订单已完成！\n订单号: %s\n签收人: %s\n全程时效: %s\n目的地类型: %s",
                            orderNo,
                            signedBy,
                            transitTime,
                            getAddressTypeDisplay(order.getDeliveryAddressType())),
                    Map.of(
                            "orderNo", orderNo,
                            "signedBy", signedBy,
                            "status", Constants.OrderState.COMPLETED,
                            "transitTime", transitTime
                    )
            );
        } catch (Exception e) {
            log.error("完成订单失败: {}", e.getMessage(), e);
            return SkillResult.failure("完成订单失败: " + e.getMessage(), "COMPLETE_FAILED");
        }
    }

    /**
     * 计算全程时效
     */
    private String calculateTransitTime(ShipmentOrder order) {
        LocalDateTime createTime = order.getCreateTime();
        LocalDateTime now = LocalDateTime.now();

        if (createTime == null) {
            return "未知";
        }

        Duration duration = Duration.between(createTime, now);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;

        if (days > 0) {
            return String.format("%d天%d小时", days, hours);
        } else {
            return String.format("%d小时", hours);
        }
    }

    /**
     * 获取地址类型显示名称
     */
    private String getAddressTypeDisplay(String addressType) {
        if (addressType == null) {
            return "未知";
        }
        switch (addressType) {
            case Constants.DeliveryAddressType.FBA:
                return "FBA仓库";
            case Constants.DeliveryAddressType.AWD:
                return "AWD仓库";
            case Constants.DeliveryAddressType.THIRD_PARTY:
                return "第三方海外仓";
            case Constants.DeliveryAddressType.COMMERCIAL:
                return "商业地址";
            case Constants.DeliveryAddressType.RESIDENTIAL:
                return "住宅地址";
            default:
                return addressType;
        }
    }
}
