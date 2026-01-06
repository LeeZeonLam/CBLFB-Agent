package com.fba.logi.agent.skill.order;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.order.model.entity.ShipmentOrder;
import com.fba.logi.domain.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 创建订单 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderSkill extends AbstractSkill {

    private final IOrderService orderService;

    @Override
    public String getSkillId() {
        return "create_order";
    }

    @Override
    public String getSkillName() {
        return "创建发货订单";
    }

    @Override
    public String getDescription() {
        return "创建一个新的FBA发货订单，包含收发货地址、货物信息等";
    }

    @Override
    public String getDomain() {
        return "order";
    }

    @Override
    public boolean requiresConfirmation() {
        return true;
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.enumParam(
                                "orderType", "订单类型",
                                List.of("FBA", "FBM", "SELF_DELIVERY")),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "originAddress", "发货地址"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "destCountry", "目的国家代码，如 US, DE, JP"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "fbaWarehouseCode", "FBA仓库代码，如 ONT8, PHX6"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "totalWeight", "总重量（KG）"),
                        SkillParameterSchema.ParameterDefinition.integerParam(
                                "totalPieces", "总件数"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "productDesc", "货物描述")
                ))
                .required(List.of("orderType", "originAddress", "destCountry", "totalWeight", "totalPieces"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String orderType = getRequiredString(parameters, "orderType");
        String originAddress = getRequiredString(parameters, "originAddress");
        String destCountry = getRequiredString(parameters, "destCountry");
        String fbaWarehouseCode = getOptionalString(parameters, "fbaWarehouseCode", null);
        double totalWeight = getRequiredDouble(parameters, "totalWeight");
        int totalPieces = getRequiredInt(parameters, "totalPieces");
        String productDesc = getOptionalString(parameters, "productDesc", "");

        // 验证 FBA 订单必须有仓库代码
        if ("FBA".equals(orderType) && (fbaWarehouseCode == null || fbaWarehouseCode.isEmpty())) {
            return SkillResult.failure("FBA订单必须指定仓库代码", "MISSING_FBA_CODE");
        }

        // 创建订单
        ShipmentOrder order = ShipmentOrder.builder()
                .orderType(orderType)
                .originAddress(originAddress)
                .destCountry(destCountry)
                .fbaWarehouseCode(fbaWarehouseCode)
                .totalWeight(BigDecimal.valueOf(totalWeight))
                .totalPieces(totalPieces)
                .state("draft")
                .userId(context.getUserId())
                .build();

        // TODO: 调用真实的订单服务
        // String orderId = orderService.createOrder(order);

        // 模拟返回
        String orderNo = "FBA" + System.currentTimeMillis();

        log.info("订单创建成功: {}", orderNo);

        return SkillResult.success(
                String.format("订单创建成功！订单号: %s\n类型: %s\n目的地: %s\n重量: %.2fKG\n件数: %d件",
                        orderNo, orderType, destCountry, totalWeight, totalPieces),
                Map.of(
                        "orderNo", orderNo,
                        "orderType", orderType,
                        "destCountry", destCountry,
                        "status", "draft"
                )
        ).withNextSkill("query_shipping_rate");
    }
}
