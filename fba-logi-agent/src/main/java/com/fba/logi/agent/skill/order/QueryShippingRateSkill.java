package com.fba.logi.agent.skill.order;

import com.fba.logi.agent.skill.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 查询运费 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryShippingRateSkill extends AbstractSkill {

    @Override
    public String getSkillId() {
        return "query_shipping_rate";
    }

    @Override
    public String getSkillName() {
        return "查询运费";
    }

    @Override
    public String getDescription() {
        return "根据目的地、重量和运输方式查询运费报价";
    }

    @Override
    public String getDomain() {
        return "order";
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "destCountry", "目的国家代码"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "weight", "货物重量（KG）"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "volume", "货物体积（CBM），可选"),
                        SkillParameterSchema.ParameterDefinition.enumParam(
                                "shippingMethod", "运输方式",
                                List.of("AIR_EXPRESS", "AIR_STANDARD", "SEA_FCL", "SEA_LCL", "RAIL"))
                ))
                .required(List.of("destCountry", "weight", "shippingMethod"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String destCountry = getRequiredString(parameters, "destCountry");
        double weight = getRequiredDouble(parameters, "weight");
        double volume = getOptionalDouble(parameters, "volume", 0);
        String shippingMethod = getRequiredString(parameters, "shippingMethod");

        // 计算计费重（取实重和体积重的较大者）
        double volumetricWeight = volume * 167; // 航空：1CBM = 167KG
        double chargeableWeight = Math.max(weight, volumetricWeight);

        // 模拟运费计算（实际应查询运费表或调用第三方API）
        double rate = calculateRate(destCountry, shippingMethod);
        double totalCost = chargeableWeight * rate;
        int transitDays = getTransitDays(destCountry, shippingMethod);

        log.info("运费查询: {} -> {}, 重量: {}KG, 方式: {}, 费用: ${}",
                "CN", destCountry, chargeableWeight, shippingMethod, totalCost);

        return SkillResult.success(
                String.format("运费报价结果:\n" +
                        "目的地: %s\n" +
                        "运输方式: %s\n" +
                        "计费重量: %.2f KG\n" +
                        "单价: $%.2f/KG\n" +
                        "预估运费: $%.2f\n" +
                        "预计时效: %d-%d 天",
                        destCountry, shippingMethod, chargeableWeight, rate,
                        totalCost, transitDays, transitDays + 3),
                Map.of(
                        "destCountry", destCountry,
                        "shippingMethod", shippingMethod,
                        "chargeableWeight", chargeableWeight,
                        "rate", rate,
                        "totalCost", totalCost,
                        "transitDays", transitDays
                )
        );
    }

    private double calculateRate(String destCountry, String shippingMethod) {
        // 模拟运费单价（$/KG）
        return switch (shippingMethod) {
            case "AIR_EXPRESS" -> switch (destCountry) {
                case "US" -> 8.5;
                case "DE", "UK", "FR" -> 9.2;
                case "JP" -> 6.8;
                default -> 10.0;
            };
            case "AIR_STANDARD" -> switch (destCountry) {
                case "US" -> 5.5;
                case "DE", "UK", "FR" -> 6.2;
                case "JP" -> 4.5;
                default -> 7.0;
            };
            case "SEA_FCL", "SEA_LCL" -> 1.2;
            case "RAIL" -> 2.8;
            default -> 5.0;
        };
    }

    private int getTransitDays(String destCountry, String shippingMethod) {
        return switch (shippingMethod) {
            case "AIR_EXPRESS" -> 3;
            case "AIR_STANDARD" -> 7;
            case "SEA_FCL", "SEA_LCL" -> switch (destCountry) {
                case "US" -> 25;
                case "DE", "UK", "FR" -> 30;
                case "JP" -> 10;
                default -> 35;
            };
            case "RAIL" -> 18;
            default -> 14;
        };
    }
}
