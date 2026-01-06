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
 * 到港 Skill - 批量更新航次、柜子、订单状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArrivePortSkill extends AbstractSkill {

    private final IShippingService shippingService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String getSkillId() {
        return "arrive_port";
    }

    @Override
    public String getSkillName() {
        return "到港";
    }

    @Override
    public String getDescription() {
        return "确认航次到港，批量更新该航次下所有柜子和订单的状态";
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
                                "actualArrivalTime", "实际到港时间（格式：yyyy-MM-dd HH:mm）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "arrivalPort", "到达港口")
                ))
                .required(List.of("voyageNo", "actualArrivalTime"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String voyageNo = getRequiredString(parameters, "voyageNo");
        String arrivalTimeStr = getRequiredString(parameters, "actualArrivalTime");
        String arrivalPort = getOptionalString(parameters, "arrivalPort", "");

        try {
            // 解析时间
            LocalDateTime actualArrival;
            try {
                actualArrival = LocalDateTime.parse(arrivalTimeStr, DATE_TIME_FORMATTER);
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

            // 执行到港操作
            int affectedOrders = shippingService.arrivePort(voyage.getVoyageId(), actualArrival);

            // 使用传入的港口或航次配置的港口
            String portName = arrivalPort.isEmpty() ? voyage.getArrivalPort() : arrivalPort;

            log.info("到港成功: 航次 {}, 港口 {}, 影响订单数: {}", voyageNo, portName, affectedOrders);

            return SkillResult.success(
                    String.format("到港成功！\n航次: %s\n船名: %s\n到达港口: %s\n实际到港时间: %s\n柜子数量: %d\n订单数量: %d",
                            voyageNo,
                            voyage.getVesselName(),
                            portName,
                            arrivalTimeStr,
                            stats.containerCount(),
                            affectedOrders),
                    Map.of(
                            "voyageNo", voyageNo,
                            "vesselName", voyage.getVesselName(),
                            "arrivalPort", portName,
                            "actualArrivalTime", arrivalTimeStr,
                            "containerCount", stats.containerCount(),
                            "orderCount", affectedOrders
                    )
            ).withNextSkill("pick_container");
        } catch (Exception e) {
            log.error("到港操作失败: {}", e.getMessage(), e);
            return SkillResult.failure("到港操作失败: " + e.getMessage(), "ARRIVE_FAILED");
        }
    }
}
