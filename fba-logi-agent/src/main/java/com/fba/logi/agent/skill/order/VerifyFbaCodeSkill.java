package com.fba.logi.agent.skill.order;

import com.fba.logi.agent.skill.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * FBA仓库代码验证 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerifyFbaCodeSkill extends AbstractSkill {

    // 美国FBA仓库代码（部分）
    private static final Map<String, String> US_WAREHOUSES = Map.ofEntries(
            Map.entry("ONT8", "加州安大略仓"),
            Map.entry("PHX6", "亚利桑那凤凰城仓"),
            Map.entry("SBD1", "加州圣贝纳迪诺仓"),
            Map.entry("LAX9", "加州洛杉矶仓"),
            Map.entry("SMF3", "加州萨克拉门托仓"),
            Map.entry("SNA4", "加州圣安娜仓"),
            Map.entry("TEB9", "新泽西仓"),
            Map.entry("JFK8", "纽约仓"),
            Map.entry("MDW2", "伊利诺伊芝加哥仓"),
            Map.entry("DFW7", "德州达拉斯仓"),
            Map.entry("HOU2", "德州休斯顿仓"),
            Map.entry("FTW1", "德州沃斯堡仓"),
            Map.entry("SEA8", "华盛顿西雅图仓"),
            Map.entry("BFI4", "华盛顿仓")
    );

    // 欧洲FBA仓库代码（部分）
    private static final Map<String, String> EU_WAREHOUSES = Map.ofEntries(
            Map.entry("LTN1", "英国卢顿仓"),
            Map.entry("MAN1", "英国曼彻斯特仓"),
            Map.entry("BHX1", "英国伯明翰仓"),
            Map.entry("FRA1", "德国法兰克福仓"),
            Map.entry("MUC3", "德国慕尼黑仓"),
            Map.entry("LEJ1", "德国莱比锡仓"),
            Map.entry("CDG1", "法国巴黎仓"),
            Map.entry("ORY1", "法国奥利仓"),
            Map.entry("FCO1", "意大利罗马仓"),
            Map.entry("MXP5", "意大利米兰仓"),
            Map.entry("MAD4", "西班牙马德里仓"),
            Map.entry("BCN1", "西班牙巴塞罗那仓")
    );

    // 日本FBA仓库代码
    private static final Map<String, String> JP_WAREHOUSES = Map.of(
            "NRT5", "日本成田仓",
            "KIX1", "日本关西仓",
            "FSZ1", "日本静冈仓",
            "HND3", "日本羽田仓"
    );

    @Override
    public String getSkillId() {
        return "verify_fba_code";
    }

    @Override
    public String getSkillName() {
        return "验证FBA仓库代码";
    }

    @Override
    public String getDescription() {
        return "验证亚马逊FBA仓库代码是否有效，并返回仓库信息";
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
                                "fbaCode", "FBA仓库代码，如 ONT8, PHX6, LTN1"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "destCountry", "目的国家代码（可选，用于交叉验证）")
                ))
                .required(List.of("fbaCode"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String fbaCode = getRequiredString(parameters, "fbaCode").toUpperCase().trim();
        String destCountry = getOptionalString(parameters, "destCountry", null);

        // 查找仓库
        String warehouseName = null;
        String warehouseCountry = null;

        if (US_WAREHOUSES.containsKey(fbaCode)) {
            warehouseName = US_WAREHOUSES.get(fbaCode);
            warehouseCountry = "US";
        } else if (EU_WAREHOUSES.containsKey(fbaCode)) {
            warehouseName = EU_WAREHOUSES.get(fbaCode);
            warehouseCountry = getEuCountry(fbaCode);
        } else if (JP_WAREHOUSES.containsKey(fbaCode)) {
            warehouseName = JP_WAREHOUSES.get(fbaCode);
            warehouseCountry = "JP";
        }

        if (warehouseName == null) {
            log.warn("无效的FBA仓库代码: {}", fbaCode);
            return SkillResult.failure(
                    String.format("FBA仓库代码 '%s' 无效，请核实后重新输入。\n\n常用代码示例:\n" +
                            "美国: ONT8, PHX6, LAX9, JFK8\n" +
                            "欧洲: LTN1, FRA1, CDG1\n" +
                            "日本: NRT5, KIX1", fbaCode),
                    "INVALID_FBA_CODE"
            );
        }

        // 交叉验证国家
        boolean countryMatch = destCountry == null || destCountry.equalsIgnoreCase(warehouseCountry);

        StringBuilder message = new StringBuilder();
        message.append("✅ FBA仓库代码验证通过\n\n");
        message.append("仓库代码: ").append(fbaCode).append("\n");
        message.append("仓库名称: ").append(warehouseName).append("\n");
        message.append("所属国家: ").append(warehouseCountry).append("\n");

        if (!countryMatch) {
            message.append("\n⚠️ 警告: 仓库国家(").append(warehouseCountry)
                    .append(")与目的国(").append(destCountry).append(")不一致，请确认。\n");
        }

        log.info("FBA代码验证: {} -> {} ({})", fbaCode, warehouseName, warehouseCountry);

        return SkillResult.success(message.toString(), Map.of(
                "fbaCode", fbaCode,
                "warehouseName", warehouseName,
                "country", warehouseCountry,
                "valid", true,
                "countryMatch", countryMatch
        ));
    }

    private String getEuCountry(String fbaCode) {
        String prefix = fbaCode.substring(0, 3);
        return switch (prefix) {
            case "LTN", "MAN", "BHX" -> "UK";
            case "FRA", "MUC", "LEJ" -> "DE";
            case "CDG", "ORY" -> "FR";
            case "FCO", "MXP" -> "IT";
            case "MAD", "BCN" -> "ES";
            default -> "EU";
        };
    }
}
