package com.fba.logi.agent.skill.order;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单审核 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditOrderSkill extends AbstractSkill {

    private final IOrderService orderService;

    @Override
    public String getSkillId() {
        return "audit_order";
    }

    @Override
    public String getSkillName() {
        return "审核订单";
    }

    @Override
    public String getDescription() {
        return "审核订单，检查信息完整性和合规性，给出审核意见";
    }

    @Override
    public String getDomain() {
        return "order";
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
                                "orderNo", "订单号"),
                        SkillParameterSchema.ParameterDefinition.booleanParam(
                                "approved", "是否审核通过"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "reason", "审核意见/拒绝原因")
                ))
                .required(List.of("orderNo", "approved"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String orderNo = getRequiredString(parameters, "orderNo");
        boolean approved = getBoolean(parameters, "approved", false);
        String reason = getOptionalString(parameters, "reason", approved ? "审核通过" : "");

        // 审核检查项
        List<String> checkItems = new ArrayList<>();
        List<String> issues = new ArrayList<>();

        // 模拟审核检查
        checkItems.add("✅ 发货地址完整性检查");
        checkItems.add("✅ 收货地址完整性检查");
        checkItems.add("✅ 货物描述清晰度检查");
        checkItems.add("✅ FBA仓库代码有效性检查");

        // 模拟发现的问题
        if (!approved && (reason == null || reason.isEmpty())) {
            issues.add("申报价值偏低，需补充商业发票");
            issues.add("货物描述不够详细");
            reason = String.join("; ", issues);
        }

        // TODO: 调用真实的审核服务
        // orderService.auditOrder(orderId, approved, reason);

        StringBuilder message = new StringBuilder();
        if (approved) {
            message.append("✅ 订单审核通过\n\n");
            message.append("订单号: ").append(orderNo).append("\n");
            message.append("审核状态: 通过\n");
            message.append("审核意见: ").append(reason).append("\n\n");
            message.append("📋 审核项目:\n");
            for (String item : checkItems) {
                message.append("  ").append(item).append("\n");
            }
            message.append("\n订单已流转至发货环节，请及时安排发货。");
        } else {
            message.append("❌ 订单审核未通过\n\n");
            message.append("订单号: ").append(orderNo).append("\n");
            message.append("审核状态: 驳回\n\n");
            message.append("📋 审核项目:\n");
            for (String item : checkItems) {
                message.append("  ").append(item).append("\n");
            }
            message.append("\n🚫 驳回原因:\n").append(reason).append("\n\n");
            message.append("请修正上述问题后重新提交审核。");
        }

        log.info("订单审核完成: {} - {}", orderNo, approved ? "通过" : "驳回");

        return SkillResult.success(message.toString(), Map.of(
                "orderNo", orderNo,
                "approved", approved,
                "reason", reason,
                "checkItems", checkItems
        ));
    }
}
