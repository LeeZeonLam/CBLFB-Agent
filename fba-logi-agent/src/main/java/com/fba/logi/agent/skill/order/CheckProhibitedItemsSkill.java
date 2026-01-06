package com.fba.logi.agent.skill.order;

import com.fba.logi.agent.skill.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 禁运品检查 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CheckProhibitedItemsSkill extends AbstractSkill {

    // 禁运品关键词列表
    private static final Set<String> PROHIBITED_KEYWORDS = Set.of(
            "电池", "battery", "锂电", "lithium",
            "刀", "knife", "武器", "weapon", "枪", "gun",
            "烟", "cigarette", "烟草", "tobacco",
            "酒", "alcohol", "酒精",
            "药", "drug", "medicine", "pharmaceutical",
            "粉末", "powder",
            "液体", "liquid", "化学品", "chemical",
            "易燃", "flammable", "易爆", "explosive",
            "毒", "poison", "toxic",
            "活体", "live animal", "动物", "animal",
            "植物", "plant", "种子", "seed",
            "食品", "food", "肉", "meat", "奶", "dairy",
            "仿牌", "counterfeit", "假货", "replica"
    );

    // 敏感品关键词（需要额外申报）
    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
            "磁铁", "magnet", "电子", "electronic",
            "马达", "motor", "音箱", "speaker",
            "化妆品", "cosmetic", "美妆",
            "纺织品", "textile", "服装", "clothing"
    );

    @Override
    public String getSkillId() {
        return "check_prohibited_items";
    }

    @Override
    public String getSkillName() {
        return "禁运品检查";
    }

    @Override
    public String getDescription() {
        return "检查货物描述是否包含禁运品或敏感品";
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
                                "productDesc", "货物描述（名称、材质、用途等）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "destCountry", "目的国家代码（不同国家禁运规则不同）")
                ))
                .required(List.of("productDesc"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String productDesc = getRequiredString(parameters, "productDesc").toLowerCase();
        String destCountry = getOptionalString(parameters, "destCountry", "US");

        List<String> prohibitedFound = new ArrayList<>();
        List<String> sensitiveFound = new ArrayList<>();

        // 检查禁运品
        for (String keyword : PROHIBITED_KEYWORDS) {
            if (productDesc.contains(keyword.toLowerCase())) {
                prohibitedFound.add(keyword);
            }
        }

        // 检查敏感品
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (productDesc.contains(keyword.toLowerCase())) {
                sensitiveFound.add(keyword);
            }
        }

        // 特定国家的额外限制
        if ("AU".equals(destCountry) || "NZ".equals(destCountry)) {
            // 澳新对木材有严格检疫
            if (productDesc.contains("木") || productDesc.contains("wood")) {
                sensitiveFound.add("木制品（需检疫证明）");
            }
        }

        boolean hasProhibited = !prohibitedFound.isEmpty();
        boolean hasSensitive = !sensitiveFound.isEmpty();

        StringBuilder message = new StringBuilder();
        message.append("📋 禁运品检查结果\n");
        message.append("目的国: ").append(destCountry).append("\n\n");

        if (hasProhibited) {
            message.append("🚫 发现禁运品关键词:\n");
            for (String item : prohibitedFound) {
                message.append("  - ").append(item).append("\n");
            }
            message.append("\n⚠️ 该货物可能无法正常发运，请核实货物实际情况或联系客服确认。\n");
        }

        if (hasSensitive) {
            message.append("\n⚡ 发现敏感品关键词:\n");
            for (String item : sensitiveFound) {
                message.append("  - ").append(item).append("\n");
            }
            message.append("\n📌 敏感品需要额外申报，可能产生附加费用。\n");
        }

        if (!hasProhibited && !hasSensitive) {
            message.append("✅ 未发现禁运品或敏感品，货物可正常发运。\n");
        }

        log.info("禁运品检查完成: 禁运={}, 敏感={}", prohibitedFound, sensitiveFound);

        return SkillResult.success(message.toString(), Map.of(
                "hasProhibited", hasProhibited,
                "hasSensitive", hasSensitive,
                "prohibitedItems", prohibitedFound,
                "sensitiveItems", sensitiveFound,
                "canShip", !hasProhibited,
                "needsDeclare", hasSensitive
        ));
    }
}
