package com.fba.logi.agent.skill.marketing;

import com.fba.logi.agent.skill.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查询活动列表 Skill
 * 可用于策略官查询所有活动，也可用于销售助手查询进行中的活动
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryCampaignsSkill extends AbstractSkill {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getSkillId() {
        return "query_campaigns";
    }

    @Override
    public String getSkillName() {
        return "查询营销活动";
    }

    @Override
    public String getDescription() {
        return "查询营销活动列表，可按状态筛选";
    }

    @Override
    public String getDomain() {
        return "marketing";
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.enumParam(
                                "status", "活动状态筛选",
                                List.of("all", "create", "open", "close")),
                        SkillParameterSchema.ParameterDefinition.booleanParam(
                                "onlyActive", "是否只查询进行中的活动")
                ))
                .required(List.of())
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String status = getOptionalString(parameters, "status", "all");
        boolean onlyActive = getBoolean(parameters, "onlyActive", false);

        // TODO: 调用真实的活动查询服务
        // List<Activity> activities = activityService.queryActivities(status, onlyActive);

        // 模拟活动数据
        List<Map<String, Object>> activities = getMockActivities(status, onlyActive);

        if (activities.isEmpty()) {
            return SkillResult.success("当前没有符合条件的活动", Map.of("count", 0));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(activities.size()).append(" 个活动:\n\n");

        for (Map<String, Object> activity : activities) {
            sb.append(String.format("📌 【%s】\n", activity.get("name")));
            sb.append(String.format("   活动ID: %s\n", activity.get("id")));
            sb.append(String.format("   状态: %s\n", activity.get("status")));
            sb.append(String.format("   时间: %s ~ %s\n", activity.get("startTime"), activity.get("endTime")));
            sb.append(String.format("   剩余库存: %s/%s\n\n", activity.get("stockSurplus"), activity.get("stockTotal")));
        }

        return SkillResult.success(sb.toString(), Map.of(
                "count", activities.size(),
                "activities", activities
        ));
    }

    private List<Map<String, Object>> getMockActivities(String status, boolean onlyActive) {
        List<Map<String, Object>> all = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        // 模拟数据
        all.add(Map.of(
                "id", 1001,
                "name", "双11大促抽奖",
                "status", "open",
                "startTime", now.minusDays(5).format(DATE_FORMAT),
                "endTime", now.plusDays(10).format(DATE_FORMAT),
                "stockTotal", 10000,
                "stockSurplus", 8523
        ));

        all.add(Map.of(
                "id", 1002,
                "name", "新用户注册礼",
                "status", "open",
                "startTime", now.minusDays(30).format(DATE_FORMAT),
                "endTime", now.plusDays(60).format(DATE_FORMAT),
                "stockTotal", 5000,
                "stockSurplus", 3200
        ));

        all.add(Map.of(
                "id", 1003,
                "name", "国庆活动",
                "status", "close",
                "startTime", now.minusDays(60).format(DATE_FORMAT),
                "endTime", now.minusDays(30).format(DATE_FORMAT),
                "stockTotal", 8000,
                "stockSurplus", 0
        ));

        // 筛选
        if (onlyActive || "open".equals(status)) {
            return all.stream()
                    .filter(a -> "open".equals(a.get("status")))
                    .toList();
        }

        if (!"all".equals(status)) {
            return all.stream()
                    .filter(a -> status.equals(a.get("status")))
                    .toList();
        }

        return all;
    }
}
