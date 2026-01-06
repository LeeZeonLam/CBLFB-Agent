package com.fba.logi.agent.skill.shipping;

import com.fba.logi.agent.skill.*;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.order.model.entity.ShipmentOrder;
import com.fba.logi.domain.order.service.IOrderService;
import com.fba.logi.domain.shipping.model.entity.Container;
import com.fba.logi.domain.shipping.service.IShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 装柜确认 Skill - 确认订单装入柜子
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoadContainerSkill extends AbstractSkill {

    private final IShippingService shippingService;
    private final IOrderService orderService;

    @Override
    public String getSkillId() {
        return "load_container";
    }

    @Override
    public String getSkillName() {
        return "装柜确认";
    }

    @Override
    public String getDescription() {
        return "确认订单货物已装入柜子";
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
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "containerNo", "柜号"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "remarks", "备注（可选）")
                ))
                .required(List.of("orderNo", "containerNo"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String orderNo = getRequiredString(parameters, "orderNo");
        String containerNo = getRequiredString(parameters, "containerNo");
        String remarks = getOptionalString(parameters, "remarks", "");

        try {
            // 查询订单
            ShipmentOrder order = orderService.getOrderByNo(orderNo);
            if (order == null) {
                return SkillResult.failure("订单不存在: " + orderNo, "ORDER_NOT_FOUND");
            }

            // 检查订单状态
            if (!Constants.OrderState.CONTAINER_ASSIGNED.equals(order.getState())) {
                return SkillResult.failure(
                        "订单状态不允许装柜，当前状态: " + order.getState(),
                        "INVALID_ORDER_STATE");
            }

            // 查询柜子
            Container container = shippingService.getContainerByNo(containerNo);
            if (container == null) {
                return SkillResult.failure("柜子不存在: " + containerNo, "CONTAINER_NOT_FOUND");
            }

            // 验证订单是否属于该柜子
            if (!containerNo.equals(order.getContainerNo())) {
                return SkillResult.failure(
                        String.format("订单 %s 不属于柜子 %s", orderNo, containerNo),
                        "ORDER_NOT_IN_CONTAINER");
            }

            // 确认装柜
            shippingService.loadOrderToContainer(order.getOrderId(), container.getContainerId());

            log.info("装柜确认成功: 订单 {} -> 柜子 {}", orderNo, containerNo);

            return SkillResult.success(
                    String.format("装柜确认成功！\n订单号: %s\n柜号: %s\n备注: %s",
                            orderNo, containerNo, remarks),
                    Map.of(
                            "orderNo", orderNo,
                            "containerNo", containerNo,
                            "status", Constants.OrderState.CONTAINER_LOADED
                    )
            );
        } catch (Exception e) {
            log.error("装柜确认失败: {}", e.getMessage(), e);
            return SkillResult.failure("装柜确认失败: " + e.getMessage(), "LOAD_FAILED");
        }
    }
}
