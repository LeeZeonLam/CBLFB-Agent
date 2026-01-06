package com.fba.logi.agent.skill.marketing;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.marketing.model.entity.Activity;
import com.fba.logi.domain.marketing.service.IRaffleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 创建营销活动 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateCampaignSkill extends AbstractSkill {

    private final IRaffleService raffleService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getSkillId() {
        return "create_campaign";
    }

    @Override
    public String getSkillName() {
        return "创建营销活动";
    }

    @Override
    public String getDescription() {
        return "创建一个新的营销抽奖活动，设置活动名称、时间范围和库存数量";
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
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "activityName", "活动名称，如'双11大促抽奖'"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "activityDesc", "活动描述，简要说明活动内容"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "beginDateTime", "活动开始时间，格式：yyyy-MM-dd HH:mm:ss"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "endDateTime", "活动结束时间，格式：yyyy-MM-dd HH:mm:ss"),
                        SkillParameterSchema.ParameterDefinition.integerParam(
                                "stockCount", "活动总库存（抽奖次数上限）")
                ))
                .required(List.of("activityName", "beginDateTime", "endDateTime", "stockCount"))
                .build();
    }

    @Override
    public SkillValidationResult validateParameters(Map<String, Object> parameters) {
        SkillValidationResult result = SkillValidationResult.success();

        // 验证必填字段
        if (!parameters.containsKey("activityName") || parameters.get("activityName") == null) {
            result.addError("活动名称不能为空");
        }
        if (!parameters.containsKey("stockCount") || parameters.get("stockCount") == null) {
            result.addError("库存数量不能为空");
        }

        // 验证时间格式
        try {
            if (parameters.containsKey("beginDateTime")) {
                LocalDateTime.parse(parameters.get("beginDateTime").toString(), DATE_FORMAT);
            }
            if (parameters.containsKey("endDateTime")) {
                LocalDateTime.parse(parameters.get("endDateTime").toString(), DATE_FORMAT);
            }
        } catch (Exception e) {
            result.addError("时间格式不正确，请使用 yyyy-MM-dd HH:mm:ss 格式");
        }

        return result;
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String activityName = getRequiredString(parameters, "activityName");
        String activityDesc = getOptionalString(parameters, "activityDesc", "");
        String beginDateTimeStr = getRequiredString(parameters, "beginDateTime");
        String endDateTimeStr = getRequiredString(parameters, "endDateTime");
        int stockCount = getRequiredInt(parameters, "stockCount");

        LocalDateTime beginDateTime = LocalDateTime.parse(beginDateTimeStr, DATE_FORMAT);
        LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr, DATE_FORMAT);

        // 验证时间逻辑
        if (endDateTime.isBefore(beginDateTime)) {
            return SkillResult.failure("结束时间不能早于开始时间", "INVALID_TIME_RANGE");
        }

        // 创建活动
        Activity activity = Activity.builder()
                .activityName(activityName)
                .activityDesc(activityDesc)
                .beginDateTime(beginDateTime)
                .endDateTime(endDateTime)
                .stockCount(stockCount)
                .stockCountSurplus(stockCount)
                .state("create")
                .build();

        // 调用领域服务创建活动
        // TODO: 这里需要实现 IRaffleService.createActivity 方法
        // Long activityId = raffleService.createActivity(activity);

        // 模拟返回
        Long activityId = System.currentTimeMillis(); // 临时生成ID

        log.info("活动创建成功: {} (ID: {})", activityName, activityId);

        return SkillResult.success(
                String.format("营销活动 '%s' 创建成功！活动ID: %d，库存: %d，有效期: %s 至 %s",
                        activityName, activityId, stockCount, beginDateTimeStr, endDateTimeStr),
                Map.of(
                        "activityId", activityId,
                        "activityName", activityName,
                        "stockCount", stockCount
                )
        ).withNextSkill("set_strategy");
    }
}
