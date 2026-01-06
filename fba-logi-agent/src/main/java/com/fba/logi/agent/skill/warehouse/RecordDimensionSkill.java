package com.fba.logi.agent.skill.warehouse;

import com.fba.logi.agent.skill.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 录入货物尺寸 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecordDimensionSkill extends AbstractSkill {

    // 亚马逊FBA超大件标准
    private static final double MAX_WEIGHT_KG = 22.68; // 50磅
    private static final double MAX_LENGTH_CM = 152; // 60英寸
    private static final double MAX_GIRTH_CM = 300; // 长+周长不超过130英寸

    @Override
    public String getSkillId() {
        return "record_dimension";
    }

    @Override
    public String getSkillName() {
        return "录入货物尺寸";
    }

    @Override
    public String getDescription() {
        return "录入货物的长宽高和重量数据，检查是否超过FBA限制";
    }

    @Override
    public String getDomain() {
        return "warehouse";
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "cartonNo", "箱号/标签号"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "length", "长度（CM）"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "width", "宽度（CM）"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "height", "高度（CM）"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "weight", "重量（KG）")
                ))
                .required(List.of("cartonNo", "length", "width", "height", "weight"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String cartonNo = getRequiredString(parameters, "cartonNo");
        double length = getRequiredDouble(parameters, "length");
        double width = getRequiredDouble(parameters, "width");
        double height = getRequiredDouble(parameters, "height");
        double weight = getRequiredDouble(parameters, "weight");

        // 计算体积和周长
        double volume = length * width * height / 1000000; // 转换为CBM
        double volumetricWeight = volume * 167; // 体积重
        double girth = 2 * (width + height) + length; // 周长计算

        // 检查是否超限
        List<String> warnings = new java.util.ArrayList<>();

        if (weight > MAX_WEIGHT_KG) {
            warnings.add(String.format("⚠️ 超重警告: %.2fKG 超过FBA限制 (%.2fKG)", weight, MAX_WEIGHT_KG));
        }
        if (length > MAX_LENGTH_CM) {
            warnings.add(String.format("⚠️ 超长警告: %.1fCM 超过FBA限制 (%.1fCM)", length, MAX_LENGTH_CM));
        }
        if (girth > MAX_GIRTH_CM) {
            warnings.add(String.format("⚠️ 周长超限: %.1fCM 超过FBA限制 (%.1fCM)", girth, MAX_GIRTH_CM));
        }

        boolean hasWarning = !warnings.isEmpty();
        double chargeableWeight = Math.max(weight, volumetricWeight);

        String message = String.format(
                "箱号 %s 尺寸录入完成:\n" +
                "尺寸: %.1f x %.1f x %.1f CM\n" +
                "实重: %.2f KG\n" +
                "体积: %.4f CBM\n" +
                "体积重: %.2f KG\n" +
                "计费重: %.2f KG%s",
                cartonNo, length, width, height, weight, volume,
                volumetricWeight, chargeableWeight,
                hasWarning ? "\n\n" + String.join("\n", warnings) : ""
        );

        log.info("尺寸录入: {} - {}x{}x{} CM, {} KG, 警告: {}",
                cartonNo, length, width, height, weight, hasWarning);

        return SkillResult.success(message, Map.of(
                "cartonNo", cartonNo,
                "length", length,
                "width", width,
                "height", height,
                "weight", weight,
                "volume", volume,
                "volumetricWeight", volumetricWeight,
                "chargeableWeight", chargeableWeight,
                "hasWarning", hasWarning,
                "warnings", warnings
        ));
    }
}
