package com.fba.logi.agent.skill.warehouse;

import com.fba.logi.agent.skill.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 标签验证 Skill
 * 验证FBA标签的正确性和完整性
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerifyLabelSkill extends AbstractSkill {

    // FBA标签格式正则（FNSKU格式: X开头，后接9位字母数字）
    private static final Pattern FNSKU_PATTERN = Pattern.compile("^X[0-9A-Z]{9}$");

    // ASIN格式正则（B开头，后接9位字母数字）
    private static final Pattern ASIN_PATTERN = Pattern.compile("^B[0-9A-Z]{9}$");

    // 箱唛格式正则（FBA开头+仓库代码+序号）
    private static final Pattern CARTON_LABEL_PATTERN = Pattern.compile("^FBA[A-Z0-9]{4,6}-[0-9]+$");

    @Override
    public String getSkillId() {
        return "verify_label";
    }

    @Override
    public String getSkillName() {
        return "验证FBA标签";
    }

    @Override
    public String getDescription() {
        return "验证FBA标签的格式正确性，包括FNSKU、ASIN、箱唛等";
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
                                "labelCode", "标签条码内容"),
                        SkillParameterSchema.ParameterDefinition.enumParam(
                                "labelType", "标签类型",
                                List.of("FNSKU", "ASIN", "CARTON", "AUTO"))
                ))
                .required(List.of("labelCode"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String labelCode = getRequiredString(parameters, "labelCode").toUpperCase().trim();
        String labelType = getOptionalString(parameters, "labelType", "AUTO");

        List<String> validationResults = new ArrayList<>();
        boolean isValid = false;
        String detectedType = null;

        // 自动检测标签类型
        if ("AUTO".equals(labelType)) {
            if (FNSKU_PATTERN.matcher(labelCode).matches()) {
                detectedType = "FNSKU";
                isValid = true;
            } else if (ASIN_PATTERN.matcher(labelCode).matches()) {
                detectedType = "ASIN";
                isValid = true;
            } else if (CARTON_LABEL_PATTERN.matcher(labelCode).matches()) {
                detectedType = "CARTON";
                isValid = true;
            }
        } else {
            // 验证指定类型
            switch (labelType) {
                case "FNSKU" -> {
                    isValid = FNSKU_PATTERN.matcher(labelCode).matches();
                    detectedType = "FNSKU";
                }
                case "ASIN" -> {
                    isValid = ASIN_PATTERN.matcher(labelCode).matches();
                    detectedType = "ASIN";
                }
                case "CARTON" -> {
                    isValid = CARTON_LABEL_PATTERN.matcher(labelCode).matches();
                    detectedType = "CARTON";
                }
            }
        }

        StringBuilder message = new StringBuilder();

        if (isValid) {
            message.append("✅ 标签验证通过\n\n");
            message.append("标签内容: ").append(labelCode).append("\n");
            message.append("标签类型: ").append(detectedType).append("\n\n");

            switch (detectedType) {
                case "FNSKU" -> {
                    message.append("📌 FNSKU说明:\n");
                    message.append("  - 这是亚马逊仓库内部商品识别码\n");
                    message.append("  - 每个商品在FBA仓库中的唯一标识\n");
                    message.append("  - 标签需贴在商品包装外层\n");
                }
                case "ASIN" -> {
                    message.append("📌 ASIN说明:\n");
                    message.append("  - 这是亚马逊标准识别号\n");
                    message.append("  - 是商品在亚马逊平台的唯一编号\n");
                    message.append("  - 如需入FBA仓，请使用对应的FNSKU\n");
                }
                case "CARTON" -> {
                    message.append("📌 箱唛说明:\n");
                    message.append("  - 这是FBA货件箱唛标签\n");
                    message.append("  - 需贴在纸箱两个相邻面\n");
                    message.append("  - 确保条码清晰可扫描\n");
                }
            }
            validationResults.add("格式验证通过");
            validationResults.add("长度验证通过");
        } else {
            message.append("❌ 标签验证失败\n\n");
            message.append("标签内容: ").append(labelCode).append("\n");
            message.append("期望类型: ").append("AUTO".equals(labelType) ? "自动识别" : labelType).append("\n\n");
            message.append("🚫 错误原因:\n");
            message.append("  - 标签格式不符合规范\n\n");
            message.append("📋 格式要求:\n");
            message.append("  - FNSKU: X开头+9位字母数字，如 X001ABC123\n");
            message.append("  - ASIN: B开头+9位字母数字，如 B08XYZ1234\n");
            message.append("  - 箱唛: FBA+仓库代码+序号，如 FBAONT8-001\n");
            validationResults.add("格式验证失败");
        }

        log.info("标签验证: {} -> {} (类型: {})", labelCode, isValid ? "通过" : "失败", detectedType);

        return SkillResult.success(message.toString(), Map.of(
                "labelCode", labelCode,
                "labelType", detectedType != null ? detectedType : "UNKNOWN",
                "valid", isValid,
                "validationResults", validationResults
        ));
    }
}
