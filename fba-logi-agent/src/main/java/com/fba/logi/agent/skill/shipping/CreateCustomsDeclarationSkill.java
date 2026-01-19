package com.fba.logi.agent.skill.shipping;

import com.fba.logi.agent.skill.*;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.shipping.model.entity.Container;
import com.fba.logi.domain.shipping.model.entity.CustomsDeclaration;
import com.fba.logi.domain.shipping.repository.IContainerRepository;
import com.fba.logi.domain.shipping.repository.ICustomsDeclarationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 创建报关单 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateCustomsDeclarationSkill extends AbstractSkill {

    private final ICustomsDeclarationRepository customsDeclarationRepository;
    private final IContainerRepository containerRepository;

    @Override
    public String getSkillId() {
        return "create_customs_declaration";
    }

    @Override
    public String getSkillName() {
        return "创建报关单";
    }

    @Override
    public String getDescription() {
        return "为指定柜子创建出口报关单";
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
                                "containerNo", "柜号"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "customsPort", "报关口岸（如：深圳盐田、宁波北仑）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "brokerName", "报关行名称"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "declaredValue", "申报总金额（USD）"),
                        SkillParameterSchema.ParameterDefinition.numberParam(
                                "declaredWeight", "申报总重量（KG）"),
                        SkillParameterSchema.ParameterDefinition.integerParam(
                                "declaredPieces", "申报总件数"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "hsCodes", "HS编码（多个用逗号分隔）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "goodsDescription", "商品描述")
                ))
                .required(List.of("containerNo", "customsPort", "declaredValue", "declaredWeight", "declaredPieces"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String containerNo = getRequiredString(parameters, "containerNo");
        String customsPort = getRequiredString(parameters, "customsPort");
        BigDecimal declaredValue = new BigDecimal(parameters.get("declaredValue").toString());
        BigDecimal declaredWeight = new BigDecimal(parameters.get("declaredWeight").toString());
        Integer declaredPieces = ((Number) parameters.get("declaredPieces")).intValue();

        String brokerName = getOptionalString(parameters, "brokerName", null);
        String hsCodes = getOptionalString(parameters, "hsCodes", null);
        String goodsDescription = getOptionalString(parameters, "goodsDescription", null);

        try {
            // 查询柜子
            Container container = containerRepository.queryByContainerNo(containerNo);
            if (container == null) {
                return SkillResult.failure("柜子不存在: " + containerNo, "CONTAINER_NOT_FOUND");
            }

            // 检查是否已有报关单
            CustomsDeclaration existing = customsDeclarationRepository.queryByContainerId(container.getContainerId());
            if (existing != null) {
                return SkillResult.failure(
                        String.format("该柜子已有报关单: %s", existing.getDeclarationNo()),
                        "DECLARATION_EXISTS");
            }

            // 生成报关单号
            String declarationNo = customsDeclarationRepository.generateDeclarationNo();
            Long declarationId = System.currentTimeMillis();

            // 创建报关单
            CustomsDeclaration declaration = CustomsDeclaration.builder()
                    .declarationId(declarationId)
                    .declarationNo(declarationNo)
                    .containerId(container.getContainerId())
                    .containerNo(containerNo)
                    .voyageId(container.getVoyageId())
                    .declarationType("EXPORT")
                    .customsPort(customsPort)
                    .brokerName(brokerName)
                    .status(Constants.CustomsStatus.PENDING)
                    .declaredValue(declaredValue)
                    .currency("USD")
                    .declaredWeight(declaredWeight)
                    .declaredPieces(declaredPieces)
                    .hsCodes(hsCodes)
                    .goodsDescription(goodsDescription)
                    .build();

            customsDeclarationRepository.save(declaration);

            log.info("创建报关单成功: {}, 柜号: {}", declarationNo, containerNo);

            return SkillResult.success(
                    String.format("报关单创建成功！\n报关单号: %s\n柜号: %s\n报关口岸: %s\n申报金额: $%.2f\n申报重量: %.2f KG\n申报件数: %d",
                            declarationNo, containerNo, customsPort, declaredValue, declaredWeight, declaredPieces),
                    Map.of(
                            "declarationNo", declarationNo,
                            "containerNo", containerNo,
                            "customsPort", customsPort,
                            "declaredValue", declaredValue,
                            "declaredWeight", declaredWeight,
                            "declaredPieces", declaredPieces,
                            "status", "pending"
                    )
            );
        } catch (Exception e) {
            log.error("创建报关单失败: {}", e.getMessage(), e);
            return SkillResult.failure("创建报关单失败: " + e.getMessage(), "CREATE_FAILED");
        }
    }
}
