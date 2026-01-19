package com.fba.logi.agent.skill.shipping;

import com.fba.logi.agent.skill.*;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.shipping.model.entity.Container;
import com.fba.logi.domain.shipping.model.entity.Voyage;
import com.fba.logi.domain.shipping.repository.IContainerRepository;
import com.fba.logi.domain.shipping.repository.IVoyageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * 创建柜子/航次 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateContainerSkill extends AbstractSkill {

    private final IContainerRepository containerRepository;
    private final IVoyageRepository voyageRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String getSkillId() {
        return "create_container";
    }

    @Override
    public String getSkillName() {
        return "创建柜子";
    }

    @Override
    public String getDescription() {
        return "创建海运柜子，包括柜号、柜型、关联航次等信息";
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
                                "containerNo", "柜号（如：MSKU1234567）"),
                        SkillParameterSchema.ParameterDefinition.enumParam(
                                "containerType", "柜型",
                                List.of("20GP", "40GP", "40HQ", "45HQ")),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "sealNo", "封条号（可选）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "voyageNo", "航次编号（可选，如已有航次）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "vesselName", "船名（可选，创建新航次时使用）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "carrier", "船公司（可选）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "departurePort", "起运港（可选）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "arrivalPort", "目的港（可选）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "etd", "预计开船时间（可选，格式：yyyy-MM-dd HH:mm）"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "eta", "预计到港时间（可选，格式：yyyy-MM-dd HH:mm）")
                ))
                .required(List.of("containerNo", "containerType"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String containerNo = getRequiredString(parameters, "containerNo");
        String containerType = getRequiredString(parameters, "containerType");
        String sealNo = getOptionalString(parameters, "sealNo", null);
        String voyageNo = getOptionalString(parameters, "voyageNo", null);
        String vesselName = getOptionalString(parameters, "vesselName", null);
        String carrier = getOptionalString(parameters, "carrier", null);
        String departurePort = getOptionalString(parameters, "departurePort", null);
        String arrivalPort = getOptionalString(parameters, "arrivalPort", null);
        String etdStr = getOptionalString(parameters, "etd", null);
        String etaStr = getOptionalString(parameters, "eta", null);

        try {
            // 检查柜号是否已存在
            Container existingContainer = containerRepository.queryByContainerNo(containerNo);
            if (existingContainer != null) {
                return SkillResult.failure("柜号已存在: " + containerNo, "CONTAINER_EXISTS");
            }

            Long voyageId = null;
            String finalVoyageNo = voyageNo;

            // 处理航次
            if (voyageNo != null && !voyageNo.isEmpty()) {
                // 使用已有航次
                Voyage voyage = voyageRepository.queryByVoyageNo(voyageNo);
                if (voyage == null) {
                    return SkillResult.failure("航次不存在: " + voyageNo, "VOYAGE_NOT_FOUND");
                }
                voyageId = voyage.getVoyageId();
            } else if (vesselName != null && !vesselName.isEmpty()) {
                // 创建新航次
                LocalDateTime etd = null;
                LocalDateTime eta = null;

                if (etdStr != null && !etdStr.isEmpty()) {
                    try {
                        etd = LocalDateTime.parse(etdStr, DATE_TIME_FORMATTER);
                    } catch (DateTimeParseException e) {
                        return SkillResult.failure("ETD时间格式错误，请使用格式：yyyy-MM-dd HH:mm", "INVALID_ETD_FORMAT");
                    }
                }

                if (etaStr != null && !etaStr.isEmpty()) {
                    try {
                        eta = LocalDateTime.parse(etaStr, DATE_TIME_FORMATTER);
                    } catch (DateTimeParseException e) {
                        return SkillResult.failure("ETA时间格式错误，请使用格式：yyyy-MM-dd HH:mm", "INVALID_ETA_FORMAT");
                    }
                }

                // 生成航次号
                finalVoyageNo = "VY" + System.currentTimeMillis();
                voyageId = System.currentTimeMillis();

                Voyage voyage = Voyage.builder()
                        .voyageId(voyageId)
                        .voyageNo(finalVoyageNo)
                        .vesselName(vesselName)
                        .carrier(carrier)
                        .departurePort(departurePort)
                        .arrivalPort(arrivalPort)
                        .estimatedDeparture(etd)
                        .estimatedArrival(eta)
                        .status(Constants.VoyageState.SCHEDULED)
                        .build();

                voyageRepository.save(voyage);
                log.info("创建航次成功: {}", finalVoyageNo);
            }

            // 获取柜型容量
            BigDecimal maxCapacity = getContainerCapacity(containerType);
            BigDecimal maxWeight = getContainerMaxWeight(containerType);

            // 创建柜子
            Long containerId = System.currentTimeMillis();
            Container container = Container.builder()
                    .containerId(containerId)
                    .containerNo(containerNo)
                    .containerType(containerType)
                    .sealNo(sealNo)
                    .maxCapacity(maxCapacity)
                    .maxWeight(maxWeight)
                    .currentCapacity(BigDecimal.ZERO)
                    .currentWeight(BigDecimal.ZERO)
                    .voyageId(voyageId)
                    .voyageNo(finalVoyageNo)
                    .status(Constants.ContainerState.EMPTY)
                    .build();

            containerRepository.save(container);

            log.info("创建柜子成功: {}, 柜型: {}, 航次: {}", containerNo, containerType, finalVoyageNo);

            StringBuilder message = new StringBuilder();
            message.append("柜子创建成功！\n\n");
            message.append("柜号: ").append(containerNo).append("\n");
            message.append("柜型: ").append(containerType).append("\n");
            message.append("最大容量: ").append(maxCapacity).append(" CBM\n");
            message.append("最大载重: ").append(maxWeight).append(" KG\n");

            if (sealNo != null) {
                message.append("封条号: ").append(sealNo).append("\n");
            }

            if (finalVoyageNo != null) {
                message.append("\n关联航次: ").append(finalVoyageNo).append("\n");
                if (vesselName != null) {
                    message.append("船名: ").append(vesselName).append("\n");
                }
                if (departurePort != null) {
                    message.append("起运港: ").append(departurePort).append("\n");
                }
                if (arrivalPort != null) {
                    message.append("目的港: ").append(arrivalPort).append("\n");
                }
            }

            return SkillResult.success(
                    message.toString(),
                    Map.of(
                            "containerId", containerId,
                            "containerNo", containerNo,
                            "containerType", containerType,
                            "maxCapacity", maxCapacity,
                            "maxWeight", maxWeight,
                            "voyageNo", finalVoyageNo != null ? finalVoyageNo : "",
                            "status", "empty"
                    )
            );
        } catch (Exception e) {
            log.error("创建柜子失败: {}", e.getMessage(), e);
            return SkillResult.failure("创建柜子失败: " + e.getMessage(), "CREATE_FAILED");
        }
    }

    private BigDecimal getContainerCapacity(String containerType) {
        return switch (containerType) {
            case "20GP" -> new BigDecimal("28");
            case "40GP" -> new BigDecimal("58");
            case "40HQ" -> new BigDecimal("68");
            case "45HQ" -> new BigDecimal("78");
            default -> new BigDecimal("58");
        };
    }

    private BigDecimal getContainerMaxWeight(String containerType) {
        return switch (containerType) {
            case "20GP" -> new BigDecimal("18000");
            case "40GP" -> new BigDecimal("26000");
            case "40HQ" -> new BigDecimal("26000");
            case "45HQ" -> new BigDecimal("25000");
            default -> new BigDecimal("26000");
        };
    }
}
