package com.fba.logi.agent.skill.shipping;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.shipping.model.entity.Container;
import com.fba.logi.domain.shipping.service.IShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 提柜 Skill - 确认柜子已提柜，批量更新订单状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PickContainerSkill extends AbstractSkill {

    private final IShippingService shippingService;

    @Override
    public String getSkillId() {
        return "pick_container";
    }

    @Override
    public String getSkillName() {
        return "提柜";
    }

    @Override
    public String getDescription() {
        return "确认柜子已从港口提出，批量更新该柜子内所有订单的状态";
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
                                "containerNo", "柜号"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "pickupLocation", "提柜地点（可选）")
                ))
                .required(List.of("containerNo"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String containerNo = getRequiredString(parameters, "containerNo");
        String pickupLocation = getOptionalString(parameters, "pickupLocation", "");

        try {
            // 查询柜子
            Container container = shippingService.getContainerByNo(containerNo);
            if (container == null) {
                return SkillResult.failure("柜子不存在: " + containerNo, "CONTAINER_NOT_FOUND");
            }

            // 获取柜内订单数量
            List<Long> orderIds = shippingService.getOrdersInContainer(container.getContainerId());

            // 执行提柜操作
            int affectedOrders = shippingService.pickContainer(container.getContainerId());

            log.info("提柜成功: 柜子 {}, 影响订单数: {}", containerNo, affectedOrders);

            return SkillResult.success(
                    String.format("提柜成功！\n柜号: %s\n柜型: %s\n提柜地点: %s\n订单数量: %d",
                            containerNo,
                            container.getContainerType(),
                            pickupLocation.isEmpty() ? "未指定" : pickupLocation,
                            affectedOrders),
                    Map.of(
                            "containerNo", containerNo,
                            "containerType", container.getContainerType(),
                            "orderCount", affectedOrders
                    )
            ).withNextSkill("start_delivery");
        } catch (Exception e) {
            log.error("提柜操作失败: {}", e.getMessage(), e);
            return SkillResult.failure("提柜操作失败: " + e.getMessage(), "PICK_FAILED");
        }
    }
}
