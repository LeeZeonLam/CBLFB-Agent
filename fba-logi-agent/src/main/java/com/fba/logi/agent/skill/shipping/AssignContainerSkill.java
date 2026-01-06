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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 排柜 Skill - 将订单分配到柜子
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignContainerSkill extends AbstractSkill {

    private final IShippingService shippingService;
    private final IOrderService orderService;

    @Override
    public String getSkillId() {
        return "assign_container";
    }

    @Override
    public String getSkillName() {
        return "排柜";
    }

    @Override
    public String getDescription() {
        return "将订单分配到指定柜子，或自动分配到合适的柜子";
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
                                "containerNo", "柜号（可选，不填则自动分配）"),
                        SkillParameterSchema.ParameterDefinition.enumParam(
                                "containerType", "柜型（新建柜子时使用）",
                                List.of("20GP", "40GP", "40HQ", "45HQ"))
                ))
                .required(List.of("orderNo"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String orderNo = getRequiredString(parameters, "orderNo");
        String containerNo = getOptionalString(parameters, "containerNo", null);
        String containerType = getOptionalString(parameters, "containerType", Constants.ContainerType.HQ40);

        try {
            // 查询订单
            ShipmentOrder order = orderService.getOrderByNo(orderNo);
            if (order == null) {
                return SkillResult.failure("订单不存在: " + orderNo, "ORDER_NOT_FOUND");
            }

            // 检查订单状态
            if (!Constants.OrderState.DIMENSION_RECORDED.equals(order.getState())) {
                return SkillResult.failure(
                        "订单状态不允许排柜，当前状态: " + order.getState() + "，需要状态: " + Constants.OrderState.DIMENSION_RECORDED,
                        "INVALID_ORDER_STATE");
            }

            Long containerId;
            String assignedContainerNo;

            if (containerNo != null && !containerNo.isEmpty()) {
                // 使用指定柜子
                Container container = shippingService.getContainerByNo(containerNo);
                containerId = container.getContainerId();
                assignedContainerNo = containerNo;
            } else {
                // 查找可用柜子或创建新柜子
                List<Container> availableContainers = shippingService.getAvailableContainers(containerType);
                if (!availableContainers.isEmpty()) {
                    Container container = availableContainers.get(0);
                    containerId = container.getContainerId();
                    assignedContainerNo = container.getContainerNo();
                } else {
                    // 创建新柜子
                    containerId = shippingService.createContainer(containerType, null);
                    Container container = shippingService.getContainerDetail(containerId);
                    assignedContainerNo = container.getContainerNo();
                }
            }

            // 排柜
            BigDecimal volume = order.getTotalVolume() != null ? order.getTotalVolume() : BigDecimal.ZERO;
            BigDecimal weight = order.getTotalWeight() != null ? order.getTotalWeight() : BigDecimal.ZERO;
            shippingService.assignOrderToContainer(order.getOrderId(), containerId, volume, weight);

            log.info("排柜成功: 订单 {} -> 柜子 {}", orderNo, assignedContainerNo);

            return SkillResult.success(
                    String.format("排柜成功！\n订单号: %s\n柜号: %s\n货物体积: %.2f CBM\n货物重量: %.2f KG",
                            orderNo, assignedContainerNo, volume, weight),
                    Map.of(
                            "orderNo", orderNo,
                            "containerNo", assignedContainerNo,
                            "containerId", containerId,
                            "volume", volume,
                            "weight", weight
                    )
            ).withNextSkill("load_container");
        } catch (Exception e) {
            log.error("排柜失败: {}", e.getMessage(), e);
            return SkillResult.failure("排柜失败: " + e.getMessage(), "ASSIGN_FAILED");
        }
    }
}
