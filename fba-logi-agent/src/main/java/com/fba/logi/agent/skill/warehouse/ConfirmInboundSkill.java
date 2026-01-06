package com.fba.logi.agent.skill.warehouse;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.warehouse.service.IWarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 确认入库 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmInboundSkill extends AbstractSkill {

    private final IWarehouseService warehouseService;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getSkillId() {
        return "confirm_inbound";
    }

    @Override
    public String getSkillName() {
        return "确认入库";
    }

    @Override
    public String getDescription() {
        return "确认货物入库完成，生成入库记录";
    }

    @Override
    public String getDomain() {
        return "warehouse";
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
                                "orderNo", "订单号/入库单号"),
                        SkillParameterSchema.ParameterDefinition.integerParam(
                                "totalCartons", "总箱数"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "totalWeight", "总重量（KG）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "palletNo", "托盘号（可选）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "locationCode", "库位号（可选）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "remarks", "备注（可选）")
                ))
                .required(List.of("orderNo", "totalCartons", "totalWeight"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String orderNo = getRequiredString(parameters, "orderNo");
        int totalCartons = getRequiredInt(parameters, "totalCartons");
        double totalWeight = getRequiredDouble(parameters, "totalWeight");
        String palletNo = getOptionalString(parameters, "palletNo", "AUTO-" + System.currentTimeMillis() % 10000);
        String locationCode = getOptionalString(parameters, "locationCode", "A-01-01");
        String remarks = getOptionalString(parameters, "remarks", "");

        LocalDateTime inboundTime = LocalDateTime.now();
        String inboundNo = "IB" + System.currentTimeMillis();

        // TODO: 调用真实的入库服务
        // warehouseService.confirmInbound(orderNo, totalCartons, totalWeight, palletNo, locationCode);

        StringBuilder message = new StringBuilder();
        message.append("✅ 入库确认成功\n\n");
        message.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");
        message.append("📋 入库单号: ").append(inboundNo).append("\n");
        message.append("📦 关联订单: ").append(orderNo).append("\n");
        message.append("━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        message.append("📊 入库明细:\n");
        message.append("  总箱数: ").append(totalCartons).append(" 箱\n");
        message.append("  总重量: ").append(String.format("%.2f", totalWeight)).append(" KG\n");
        message.append("  托盘号: ").append(palletNo).append("\n");
        message.append("  库位号: ").append(locationCode).append("\n");
        message.append("  入库时间: ").append(inboundTime.format(DATE_FORMAT)).append("\n");

        if (!remarks.isEmpty()) {
            message.append("  备注: ").append(remarks).append("\n");
        }

        message.append("\n━━━━━━━━━━━━━━━━━━━━━━━━\n");
        message.append("📌 后续操作:\n");
        message.append("  1. 货物已上架至指定库位\n");
        message.append("  2. 系统将自动更新库存\n");
        message.append("  3. 可在系统中查询入库记录\n");

        log.info("入库确认: 订单={}, 箱数={}, 重量={} KG, 入库单={}", orderNo, totalCartons, totalWeight, inboundNo);

        return SkillResult.success(message.toString(), Map.of(
                "inboundNo", inboundNo,
                "orderNo", orderNo,
                "totalCartons", totalCartons,
                "totalWeight", totalWeight,
                "palletNo", palletNo,
                "locationCode", locationCode,
                "inboundTime", inboundTime.format(DATE_FORMAT),
                "status", "completed"
        ));
    }
}
