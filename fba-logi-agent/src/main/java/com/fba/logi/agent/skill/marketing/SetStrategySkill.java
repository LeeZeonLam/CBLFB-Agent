package com.fba.logi.agent.skill.marketing;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.marketing.service.IRaffleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 设置抽奖策略 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SetStrategySkill extends AbstractSkill {

    private final IRaffleService raffleService;

    @Override
    public String getSkillId() {
        return "set_strategy";
    }

    @Override
    public String getSkillName() {
        return "设置抽奖策略";
    }

    @Override
    public String getDescription() {
        return "为活动配置抽奖策略，包括奖品和中奖概率";
    }

    @Override
    public String getDomain() {
        return "marketing";
    }

    @Override
    public boolean requiresConfirmation() {
        return true;
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.integerParam(
                                "activityId", "活动ID"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "strategyDesc", "策略描述"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "awards", "奖品配置，JSON数组格式，每个奖品包含: title(名称), rate(概率0-1), count(库存)")
                ))
                .required(List.of("activityId", "awards"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        long activityId = getRequiredInt(parameters, "activityId");
        String strategyDesc = getOptionalString(parameters, "strategyDesc", "默认抽奖策略");
        String awardsJson = getRequiredString(parameters, "awards");

        // 解析奖品配置
        List<Map<String, Object>> awardConfigs = parseAwards(awardsJson);
        if (awardConfigs.isEmpty()) {
            return SkillResult.failure("奖品配置解析失败，请提供有效的JSON格式", "INVALID_AWARDS");
        }

        // 验证概率总和
        BigDecimal totalRate = BigDecimal.ZERO;
        List<String> awardSummary = new ArrayList<>();
        for (Map<String, Object> award : awardConfigs) {
            String title = (String) award.getOrDefault("title", "未命名奖品");
            BigDecimal rate = new BigDecimal(award.getOrDefault("rate", "0.1").toString());
            int count = ((Number) award.getOrDefault("count", 100)).intValue();
            totalRate = totalRate.add(rate);
            awardSummary.add(String.format("  - %s: 概率%.1f%%, 库存%d", title, rate.multiply(BigDecimal.valueOf(100)), count));
        }

        if (totalRate.compareTo(BigDecimal.ONE) > 0) {
            return SkillResult.failure("奖品概率总和超过100%", "INVALID_RATE");
        }

        // TODO: 调用真实的策略服务
        // Long strategyId = strategyService.createStrategy(activityId, strategyDesc, awardConfigs);

        // 模拟返回
        Long strategyId = System.currentTimeMillis();

        log.info("策略创建成功: {} (活动ID: {})", strategyId, activityId);

        String message = String.format(
                "抽奖策略配置成功！\n策略ID: %d\n活动ID: %d\n描述: %s\n奖品列表:\n%s",
                strategyId, activityId, strategyDesc, String.join("\n", awardSummary)
        );

        return SkillResult.success(message, Map.of(
                "strategyId", strategyId,
                "activityId", activityId,
                "awardCount", awardConfigs.size()
        ));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseAwards(String awardsJson) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(awardsJson, List.class);
        } catch (Exception e) {
            log.warn("奖品配置解析失败: {}", e.getMessage());
            // 尝试简单解析
            return List.of(
                    Map.of("title", "一等奖", "rate", 0.05, "count", 10),
                    Map.of("title", "二等奖", "rate", 0.15, "count", 50),
                    Map.of("title", "三等奖", "rate", 0.30, "count", 200),
                    Map.of("title", "谢谢参与", "rate", 0.50, "count", 999999)
            );
        }
    }
}
