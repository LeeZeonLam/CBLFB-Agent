package com.fba.logi.agent.skill.warehouse;

import com.fba.logi.agent.skill.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 超重超尺检查 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CheckOverweightSkill extends AbstractSkill {

    // 亚马逊FBA标准尺寸限制
    private static final double STANDARD_MAX_WEIGHT_KG = 22.68; // 50磅
    private static final double STANDARD_MAX_LENGTH_CM = 45.72; // 18英寸
    private static final double STANDARD_MAX_WIDTH_CM = 35.56;  // 14英寸
    private static final double STANDARD_MAX_HEIGHT_CM = 20.32; // 8英寸

    // 大件尺寸限制
    private static final double LARGE_MAX_WEIGHT_KG = 68.04;    // 150磅
    private static final double LARGE_MAX_LENGTH_CM = 152.4;    // 60英寸
    private static final double LARGE_MAX_GIRTH_CM = 330.2;     // 130英寸（长+周长）

    // 超大件限制
    private static final double OVERSIZE_MAX_WEIGHT_KG = 68.04;
    private static final double OVERSIZE_MAX_LENGTH_CM = 274.32; // 108英寸
    private static final double OVERSIZE_MAX_GIRTH_CM = 419.1;   // 165英寸

    @Override
    public String getSkillId() {
        return "check_overweight";
    }

    @Override
    public String getSkillName() {
        return "超重超尺检查";
    }

    @Override
    public String getDescription() {
        return "检查货物是否超过FBA尺寸和重量限制，判断货物分类";
    }

    @Override
    public String getDomain() {
        return "warehouse";
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "length", "长度（CM）"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "width", "宽度（CM）"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "height", "高度（CM）"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "weight", "重量（KG）")
                ))
                .required(List.of("length", "width", "height", "weight"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        double length = getRequiredDouble(parameters, "length");
        double width = getRequiredDouble(parameters, "width");
        double height = getRequiredDouble(parameters, "height");
        double weight = getRequiredDouble(parameters, "weight");

        // 确保 length 是最长边
        double[] dims = {length, width, height};
        java.util.Arrays.sort(dims);
        length = dims[2];
        width = dims[1];
        height = dims[0];

        // 计算周长（长 + 2*(宽+高)）
        double girth = length + 2 * (width + height);

        // 判断分类
        String sizeCategory;
        List<String> warnings = new ArrayList<>();
        List<String> details = new ArrayList<>();
        boolean canShip = true;

        // 判断分类
        if (weight <= STANDARD_MAX_WEIGHT_KG &&
                length <= STANDARD_MAX_LENGTH_CM &&
                width <= STANDARD_MAX_WIDTH_CM &&
                height <= STANDARD_MAX_HEIGHT_CM) {
            sizeCategory = "标准尺寸";
            details.add("✅ 符合标准尺寸规格");
            details.add("  费用等级: 标准费率");
        } else if (weight <= LARGE_MAX_WEIGHT_KG &&
                length <= LARGE_MAX_LENGTH_CM &&
                girth <= LARGE_MAX_GIRTH_CM) {
            sizeCategory = "大件";
            details.add("📦 归类为大件");
            details.add("  费用等级: 大件费率（较高）");
            if (weight > STANDARD_MAX_WEIGHT_KG) {
                warnings.add(String.format("重量 %.2f KG 超过标准限制 %.2f KG", weight, STANDARD_MAX_WEIGHT_KG));
            }
            if (length > STANDARD_MAX_LENGTH_CM) {
                warnings.add(String.format("长度 %.1f CM 超过标准限制 %.1f CM", length, STANDARD_MAX_LENGTH_CM));
            }
        } else if (weight <= OVERSIZE_MAX_WEIGHT_KG &&
                length <= OVERSIZE_MAX_LENGTH_CM &&
                girth <= OVERSIZE_MAX_GIRTH_CM) {
            sizeCategory = "超大件";
            details.add("📦 归类为超大件");
            details.add("  费用等级: 超大件费率（很高）");
            warnings.add("超大件需要特殊处理，额外费用较高");
        } else {
            sizeCategory = "不可入仓";
            canShip = false;
            details.add("🚫 超过FBA最大限制，无法入仓");
            if (weight > OVERSIZE_MAX_WEIGHT_KG) {
                warnings.add(String.format("重量 %.2f KG 超过最大限制 %.2f KG", weight, OVERSIZE_MAX_WEIGHT_KG));
            }
            if (length > OVERSIZE_MAX_LENGTH_CM) {
                warnings.add(String.format("长度 %.1f CM 超过最大限制 %.1f CM", length, OVERSIZE_MAX_LENGTH_CM));
            }
            if (girth > OVERSIZE_MAX_GIRTH_CM) {
                warnings.add(String.format("周长 %.1f CM 超过最大限制 %.1f CM", girth, OVERSIZE_MAX_GIRTH_CM));
            }
        }

        // 构建返回消息
        StringBuilder message = new StringBuilder();
        message.append("📏 超重超尺检查结果\n\n");
        message.append("货物尺寸: ").append(String.format("%.1f x %.1f x %.1f CM\n", length, width, height));
        message.append("货物重量: ").append(String.format("%.2f KG\n", weight));
        message.append("周长计算: ").append(String.format("%.1f CM\n\n", girth));
        message.append("尺寸分类: ").append(sizeCategory).append("\n\n");

        for (String detail : details) {
            message.append(detail).append("\n");
        }

        if (!warnings.isEmpty()) {
            message.append("\n⚠️ 警告:\n");
            for (String warning : warnings) {
                message.append("  - ").append(warning).append("\n");
            }
        }

        if (!canShip) {
            message.append("\n建议: 请分拆货物或选择其他物流方式。");
        }

        log.info("超重超尺检查: {}x{}x{} CM, {} KG -> {}", length, width, height, weight, sizeCategory);

        return SkillResult.success(message.toString(), Map.of(
                "length", length,
                "width", width,
                "height", height,
                "weight", weight,
                "girth", girth,
                "sizeCategory", sizeCategory,
                "canShip", canShip,
                "warnings", warnings
        ));
    }
}
