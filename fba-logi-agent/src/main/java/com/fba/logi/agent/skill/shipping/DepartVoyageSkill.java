package com.fba.logi.agent.skill.shipping;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.shipping.model.entity.Voyage;
import com.fba.logi.domain.shipping.service.IShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * 开船 Skill - 批量更新航次、柜子、订单状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepartVoyageSkill extends AbstractSkill {

    private final IShippingService shippingService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String getSkillId() {
        return "depart_voyage";
    }

    @Override
    public String getSkillName() {
        return "开船";
    }

    @Override
    public String getDescription() {
        return "确认航次开船，批量更新该航次下所有柜子和订单的状态";
    }

    @Override
    public String getDomain() {
        return "shipping";
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
                                "voyageNo", "航次编号"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "actualDepartureTime", "实际开船时间（格式：yyyy-MM-dd HH:mm）")
                ))
                .required(List.of("voyageNo", "actualDepartureTime"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String voyageNo = getRequiredString(parameters, "voyageNo");
        String departureTimeStr = getRequiredString(parameters, "actualDepartureTime");

        try {
            // 解析时间
            LocalDateTime actualDeparture;
            try {
                actualDeparture = LocalDateTime.parse(departureTimeStr, DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                return SkillResult.failure(
                        "时间格式错误，请使用格式：yyyy-MM-dd HH:mm",
                        "INVALID_TIME_FORMAT");
            }

            // 查询航次
            Voyage voyage = shippingService.getVoyageByNo(voyageNo);
            if (voyage == null) {
                return SkillResult.failure("航次不存在: " + voyageNo, "VOYAGE_NOT_FOUND");
            }

            // 获取航次统计信息
            IShippingService.VoyageStatistics stats = shippingService.getVoyageStatistics(voyage.getVoyageId());

            // 执行开船操作
            int affectedOrders = shippingService.departVoyage(voyage.getVoyageId(), actualDeparture);

            log.info("开船成功: 航次 {}, 影响订单数: {}", voyageNo, affectedOrders);

            return SkillResult.success(
                    String.format("开船成功！\n航次: %s\n船名: %s\n实际开船时间: %s\n柜子数量: %d\n订单数量: %d",
                            voyageNo,
                            voyage.getVesselName(),
                            departureTimeStr,
                            stats.containerCount(),
                            affectedOrders),
                    Map.of(
                            "voyageNo", voyageNo,
                            "vesselName", voyage.getVesselName(),
                            "actualDepartureTime", departureTimeStr,
                            "containerCount", stats.containerCount(),
                            "orderCount", affectedOrders
                    )
            );
        } catch (Exception e) {
            log.error("开船操作失败: {}", e.getMessage(), e);
            return SkillResult.failure("开船操作失败: " + e.getMessage(), "DEPART_FAILED");
        }
    }
}
