package com.fba.logi.agent.skill.shipping;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.shipping.model.entity.CustomsDeclaration;
import com.fba.logi.domain.shipping.repository.ICustomsDeclarationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询报关单 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryCustomsDeclarationSkill extends AbstractSkill {

    private final ICustomsDeclarationRepository customsDeclarationRepository;

    @Override
    public String getSkillId() {
        return "query_customs_declaration";
    }

    @Override
    public String getSkillName() {
        return "查询报关单";
    }

    @Override
    public String getDescription() {
        return "查询报关单信息，支持按报关单号、柜号或状态查询";
    }

    @Override
    public String getDomain() {
        return "shipping";
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "declarationNo", "报关单号（可选）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "containerNo", "柜号（可选）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "status", "状态（可选）：pending/processing/cleared/inspecting/rejected")
                ))
                .required(List.of())
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String declarationNo = getOptionalString(parameters, "declarationNo", null);
        String containerNo = getOptionalString(parameters, "containerNo", null);
        String status = getOptionalString(parameters, "status", null);

        try {
            // 按报关单号查询
            if (declarationNo != null && !declarationNo.isEmpty()) {
                CustomsDeclaration declaration = customsDeclarationRepository.queryByDeclarationNo(declarationNo);
                if (declaration == null) {
                    return SkillResult.failure("报关单不存在: " + declarationNo, "NOT_FOUND");
                }
                return SkillResult.success(
                        formatDeclaration(declaration),
                        Map.of("declaration", convertToMap(declaration))
                );
            }

            // 按柜号查询
            if (containerNo != null && !containerNo.isEmpty()) {
                // 需要先通过柜号获取柜子ID，这里简化处理
                return SkillResult.failure("请提供报关单号或状态进行查询", "INVALID_PARAMS");
            }

            // 按状态查询
            if (status != null && !status.isEmpty()) {
                List<CustomsDeclaration> declarations = customsDeclarationRepository.queryByStatus(status);
                if (declarations.isEmpty()) {
                    return SkillResult.success(
                            "未找到状态为 [" + status + "] 的报关单",
                            Map.of("declarations", List.of())
                    );
                }
                return SkillResult.success(
                        formatDeclarationList(declarations),
                        Map.of("declarations", declarations.stream()
                                .map(this::convertToMap)
                                .collect(Collectors.toList()))
                );
            }

            // 查询待处理的报关单
            List<CustomsDeclaration> pendingDeclarations = customsDeclarationRepository.queryPendingDeclarations();
            List<CustomsDeclaration> inspectingDeclarations = customsDeclarationRepository.queryInspectingDeclarations();

            StringBuilder sb = new StringBuilder();
            sb.append("=== 报关单概览 ===\n\n");

            if (!pendingDeclarations.isEmpty()) {
                sb.append("【待报关】").append(pendingDeclarations.size()).append(" 单\n");
                for (CustomsDeclaration d : pendingDeclarations) {
                    sb.append(String.format("  - %s | 柜号: %s | 口岸: %s\n",
                            d.getDeclarationNo(), d.getContainerNo(), d.getCustomsPort()));
                }
                sb.append("\n");
            }

            if (!inspectingDeclarations.isEmpty()) {
                sb.append("【查验中】").append(inspectingDeclarations.size()).append(" 单\n");
                for (CustomsDeclaration d : inspectingDeclarations) {
                    sb.append(String.format("  - %s | 柜号: %s | 原因: %s\n",
                            d.getDeclarationNo(), d.getContainerNo(), d.getInspectionReason()));
                }
            }

            if (pendingDeclarations.isEmpty() && inspectingDeclarations.isEmpty()) {
                sb.append("暂无待处理的报关单");
            }

            return SkillResult.success(
                    sb.toString(),
                    Map.of(
                            "pendingCount", pendingDeclarations.size(),
                            "inspectingCount", inspectingDeclarations.size()
                    )
            );
        } catch (Exception e) {
            log.error("查询报关单失败: {}", e.getMessage(), e);
            return SkillResult.failure("查询报关单失败: " + e.getMessage(), "QUERY_FAILED");
        }
    }

    private String formatDeclaration(CustomsDeclaration d) {
        return String.format("""
                === 报关单详情 ===
                报关单号: %s
                柜号: %s
                报关类型: %s
                报关口岸: %s
                报关行: %s
                状态: %s
                申报金额: $%.2f
                申报重量: %.2f KG
                申报件数: %d
                HS编码: %s
                商品描述: %s
                申报时间: %s
                放行时间: %s
                """,
                d.getDeclarationNo(),
                d.getContainerNo(),
                d.getDeclarationType(),
                d.getCustomsPort(),
                d.getBrokerName() != null ? d.getBrokerName() : "-",
                d.getStatusDescription(),
                d.getDeclaredValue() != null ? d.getDeclaredValue() : 0,
                d.getDeclaredWeight() != null ? d.getDeclaredWeight() : 0,
                d.getDeclaredPieces() != null ? d.getDeclaredPieces() : 0,
                d.getHsCodes() != null ? d.getHsCodes() : "-",
                d.getGoodsDescription() != null ? d.getGoodsDescription() : "-",
                d.getDeclaredTime() != null ? d.getDeclaredTime().toString() : "-",
                d.getClearedTime() != null ? d.getClearedTime().toString() : "-"
        );
    }

    private String formatDeclarationList(List<CustomsDeclaration> declarations) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== 查询结果（共 %d 条）===\n\n", declarations.size()));
        for (CustomsDeclaration d : declarations) {
            sb.append(String.format("%s | 柜号: %s | 口岸: %s | 状态: %s | 金额: $%.2f\n",
                    d.getDeclarationNo(),
                    d.getContainerNo(),
                    d.getCustomsPort(),
                    d.getStatusDescription(),
                    d.getDeclaredValue() != null ? d.getDeclaredValue() : 0));
        }
        return sb.toString();
    }

    private Map<String, Object> convertToMap(CustomsDeclaration d) {
        return Map.of(
                "declarationNo", d.getDeclarationNo(),
                "containerNo", d.getContainerNo() != null ? d.getContainerNo() : "",
                "customsPort", d.getCustomsPort() != null ? d.getCustomsPort() : "",
                "status", d.getStatus(),
                "statusDescription", d.getStatusDescription(),
                "declaredValue", d.getDeclaredValue() != null ? d.getDeclaredValue() : 0,
                "declaredWeight", d.getDeclaredWeight() != null ? d.getDeclaredWeight() : 0
        );
    }
}
