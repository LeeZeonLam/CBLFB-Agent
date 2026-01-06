package com.fba.logi.agent.skill.basedata;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.basedata.model.entity.ShippingChannel;
import com.fba.logi.domain.basedata.service.IBaseDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询渠道 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryChannelSkill extends AbstractSkill {

    private final IBaseDataService baseDataService;

    @Override
    public String getSkillId() {
        return "query_channel";
    }

    @Override
    public String getSkillName() {
        return "查询物流渠道";
    }

    @Override
    public String getDescription() {
        return "查询可用的物流渠道（美森快船、以星快船、盐田船等）";
    }

    @Override
    public String getDomain() {
        return "basedata";
    }

    @Override
    public boolean requiresConfirmation() {
        return false;
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "channelCode", "渠道代码"),
                        SkillParameterSchema.ParameterDefinition.enumParam(
                                "destCountry", "目的国家",
                                List.of("US", "CA", "DE", "UK", "JP", "ALL"))
                ))
                .required(List.of())
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String channelCode = getOptionalString(parameters, "channelCode", null);
        String destCountry = getOptionalString(parameters, "destCountry", "ALL");

        try {
            // 如果指定了渠道代码，直接查询
            if (channelCode != null && !channelCode.isEmpty()) {
                ShippingChannel channel = baseDataService.getChannelByCode(channelCode);
                return SkillResult.success(
                        formatChannelDetail(channel),
                        Map.of("channel", channelToMap(channel))
                );
            }

            // 查询渠道列表
            List<ShippingChannel> channels;
            if ("ALL".equals(destCountry)) {
                channels = baseDataService.getAvailableChannels();
            } else {
                channels = baseDataService.getChannelsByDestCountry(destCountry);
            }

            if (channels.isEmpty()) {
                return SkillResult.success("未找到符合条件的渠道", Map.of("channels", List.of()));
            }

            // 格式化输出
            String result = formatChannelList(channels);

            return SkillResult.success(
                    result,
                    Map.of(
                            "channels", channels.stream()
                                    .map(this::channelToMap)
                                    .collect(Collectors.toList()),
                            "count", channels.size()
                    )
            );
        } catch (Exception e) {
            log.error("查询渠道失败: {}", e.getMessage(), e);
            return SkillResult.failure("查询失败: " + e.getMessage(), "QUERY_FAILED");
        }
    }

    /**
     * 格式化渠道详情
     */
    private String formatChannelDetail(ShippingChannel channel) {
        StringBuilder sb = new StringBuilder();
        sb.append("渠道信息:\n");
        sb.append(String.format("代码: %s\n", channel.getChannelCode()));
        sb.append(String.format("名称: %s\n", channel.getChannelName()));
        sb.append(String.format("承运商: %s\n", channel.getCarrier()));
        sb.append(String.format("运输类型: %s\n", getTransportTypeDisplay(channel.getTransportType())));
        sb.append(String.format("起运港: %s\n", channel.getDeparturePort()));
        sb.append(String.format("可达港口: %s\n", String.join(", ", channel.getArrivalPorts())));
        sb.append(String.format("预计时效: %d 天\n", channel.getTransitDays()));
        if (channel.getPricePerCbm() != null) {
            sb.append(String.format("每立方价格: %.2f 元/CBM\n", channel.getPricePerCbm()));
        }
        if (channel.getPricePerKg() != null) {
            sb.append(String.format("每公斤价格: %.2f 元/KG\n", channel.getPricePerKg()));
        }
        sb.append(String.format("支持柜型: %s", String.join(", ", channel.getSupportedContainerTypes())));
        return sb.toString();
    }

    /**
     * 格式化渠道列表
     */
    private String formatChannelList(List<ShippingChannel> channels) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("找到 %d 个可用渠道:\n\n", channels.size()));

        for (int i = 0; i < channels.size(); i++) {
            ShippingChannel c = channels.get(i);
            sb.append(String.format("%d. %s (%s)\n", i + 1, c.getChannelName(), c.getChannelCode()));
            sb.append(String.format("   承运商: %s | 时效: %d天 | 起运港: %s\n",
                    c.getCarrier(),
                    c.getTransitDays(),
                    c.getDeparturePort()));
            if (c.getArrivalPorts() != null && !c.getArrivalPorts().isEmpty()) {
                sb.append(String.format("   可达港口: %s\n", String.join(", ", c.getArrivalPorts())));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 获取运输类型显示名称
     */
    private String getTransportTypeDisplay(String transportType) {
        if (transportType == null) {
            return "未知";
        }
        switch (transportType) {
            case "SEA_EXPRESS":
                return "海运快船";
            case "SEA_STANDARD":
                return "海运普船";
            case "AIR":
                return "空运";
            default:
                return transportType;
        }
    }

    /**
     * 将渠道转换为 Map
     */
    private Map<String, Object> channelToMap(ShippingChannel channel) {
        Map<String, Object> map = new HashMap<>();
        map.put("channelCode", channel.getChannelCode());
        map.put("channelName", channel.getChannelName());
        map.put("carrier", channel.getCarrier());
        map.put("transportType", channel.getTransportType());
        map.put("departurePort", channel.getDeparturePort());
        map.put("arrivalPorts", channel.getArrivalPorts());
        map.put("transitDays", channel.getTransitDays());
        if (channel.getPricePerCbm() != null) {
            map.put("pricePerCbm", channel.getPricePerCbm());
        }
        if (channel.getPricePerKg() != null) {
            map.put("pricePerKg", channel.getPricePerKg());
        }
        return map;
    }
}
