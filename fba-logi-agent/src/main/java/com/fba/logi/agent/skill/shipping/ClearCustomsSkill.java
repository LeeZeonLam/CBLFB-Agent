package com.fba.logi.agent.skill.shipping;

import com.fba.logi.agent.skill.*;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.shipping.model.entity.CustomsDeclaration;
import com.fba.logi.domain.shipping.repository.ICustomsDeclarationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 报关放行 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClearCustomsSkill extends AbstractSkill {

    private final ICustomsDeclarationRepository customsDeclarationRepository;

    @Override
    public String getSkillId() {
        return "clear_customs";
    }

    @Override
    public String getSkillName() {
        return "报关放行";
    }

    @Override
    public String getDescription() {
        return "确认报关单已放行，更新报关状态";
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
                                "declarationNo", "报关单号")
                ))
                .required(List.of("declarationNo"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String declarationNo = getRequiredString(parameters, "declarationNo");

        try {
            // 查询报关单
            CustomsDeclaration declaration = customsDeclarationRepository.queryByDeclarationNo(declarationNo);
            if (declaration == null) {
                return SkillResult.failure("报关单不存在: " + declarationNo, "DECLARATION_NOT_FOUND");
            }

            // 检查状态
            if (declaration.isCleared()) {
                return SkillResult.failure("报关单已放行", "ALREADY_CLEARED");
            }

            if (!declaration.canTransitionTo(Constants.CustomsStatus.CLEARED)) {
                return SkillResult.failure(
                        String.format("当前状态 [%s] 不允许放行", declaration.getStatusDescription()),
                        "INVALID_STATUS");
            }

            // 执行放行
            declaration.clear();
            customsDeclarationRepository.update(declaration);

            log.info("报关放行成功: {}, 柜号: {}", declarationNo, declaration.getContainerNo());

            return SkillResult.success(
                    String.format("报关放行成功！\n报关单号: %s\n柜号: %s\n报关口岸: %s\n放行时间: %s",
                            declarationNo,
                            declaration.getContainerNo(),
                            declaration.getCustomsPort(),
                            declaration.getClearedTime()),
                    Map.of(
                            "declarationNo", declarationNo,
                            "containerNo", declaration.getContainerNo(),
                            "status", "cleared",
                            "clearedTime", declaration.getClearedTime().toString()
                    )
            );
        } catch (Exception e) {
            log.error("报关放行失败: {}", e.getMessage(), e);
            return SkillResult.failure("报关放行失败: " + e.getMessage(), "CLEAR_FAILED");
        }
    }
}
