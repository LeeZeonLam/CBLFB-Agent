package com.fba.logi.agent.skill.basedata;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.basedata.model.entity.FbaWarehouse;
import com.fba.logi.domain.basedata.service.IBaseDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询 FBA 仓库 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryFbaWarehouseSkill extends AbstractSkill {

    private final IBaseDataService baseDataService;

    @Override
    public String getSkillId() {
        return "query_fba_warehouse";
    }

    @Override
    public String getSkillName() {
        return "查询FBA仓库";
    }

    @Override
    public String getDescription() {
        return "查询亚马逊 FBA 仓库信息，支持按国家、仓库代码查询";
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
                                "warehouseCode", "仓库代码（如 ONT8, PHX6）"),
                        SkillParameterSchema.ParameterDefinition.enumParam(
                                "country", "国家代码",
                                List.of("US", "CA", "DE", "UK", "JP", "ALL")),
                        SkillParameterSchema.ParameterDefinition.enumParam(
                                "warehouseType", "仓库类型",
                                List.of("FBA", "AWD", "ALL"))
                ))
                .required(List.of())
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String warehouseCode = getOptionalString(parameters, "warehouseCode", null);
        String country = getOptionalString(parameters, "country", "ALL");
        String warehouseType = getOptionalString(parameters, "warehouseType", "ALL");

        try {
            // 如果指定了仓库代码，直接查询
            if (warehouseCode != null && !warehouseCode.isEmpty()) {
                FbaWarehouse warehouse = baseDataService.getFbaWarehouseByCode(warehouseCode);
                return SkillResult.success(
                        formatWarehouseDetail(warehouse),
                        Map.of("warehouse", warehouseToMap(warehouse))
                );
            }

            // 查询仓库列表
            List<FbaWarehouse> warehouses;
            if ("AWD".equals(warehouseType)) {
                warehouses = baseDataService.getAwdWarehouses("ALL".equals(country) ? null : country);
            } else if ("FBA".equals(warehouseType)) {
                if ("ALL".equals(country)) {
                    warehouses = baseDataService.getAvailableFbaWarehouses();
                } else {
                    warehouses = baseDataService.getFbaWarehousesByCountry(country);
                }
            } else {
                // 查询所有
                if ("ALL".equals(country)) {
                    warehouses = baseDataService.getAvailableFbaWarehouses();
                    warehouses.addAll(baseDataService.getAwdWarehouses(null));
                } else {
                    warehouses = baseDataService.getFbaWarehousesByCountry(country);
                    warehouses.addAll(baseDataService.getAwdWarehouses(country));
                }
            }

            if (warehouses.isEmpty()) {
                return SkillResult.success("未找到符合条件的仓库", Map.of("warehouses", List.of()));
            }

            // 格式化输出
            String result = formatWarehouseList(warehouses);

            return SkillResult.success(
                    result,
                    Map.of(
                            "warehouses", warehouses.stream()
                                    .map(this::warehouseToMap)
                                    .collect(Collectors.toList()),
                            "count", warehouses.size()
                    )
            );
        } catch (Exception e) {
            log.error("查询 FBA 仓库失败: {}", e.getMessage(), e);
            return SkillResult.failure("查询失败: " + e.getMessage(), "QUERY_FAILED");
        }
    }

    /**
     * 格式化仓库详情
     */
    private String formatWarehouseDetail(FbaWarehouse warehouse) {
        StringBuilder sb = new StringBuilder();
        sb.append("仓库信息:\n");
        sb.append(String.format("代码: %s\n", warehouse.getWarehouseCode()));
        sb.append(String.format("名称: %s\n", warehouse.getWarehouseName()));
        sb.append(String.format("类型: %s\n", warehouse.getWarehouseType()));
        sb.append(String.format("国家: %s\n", warehouse.getCountry()));
        sb.append(String.format("州/省: %s\n", warehouse.getState()));
        sb.append(String.format("城市: %s\n", warehouse.getCity()));
        sb.append(String.format("地址: %s\n", warehouse.getAddressLine1()));
        if (warehouse.getAddressLine2() != null && !warehouse.getAddressLine2().isEmpty()) {
            sb.append(String.format("      %s\n", warehouse.getAddressLine2()));
        }
        sb.append(String.format("邮编: %s\n", warehouse.getZipCode()));
        sb.append(String.format("接受货物: %s", warehouse.getAcceptingShipments() ? "是" : "否"));
        return sb.toString();
    }

    /**
     * 格式化仓库列表
     */
    private String formatWarehouseList(List<FbaWarehouse> warehouses) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("找到 %d 个仓库:\n\n", warehouses.size()));

        for (int i = 0; i < Math.min(warehouses.size(), 20); i++) {
            FbaWarehouse w = warehouses.get(i);
            sb.append(String.format("%d. %s (%s) - %s, %s\n",
                    i + 1,
                    w.getWarehouseCode(),
                    w.getWarehouseType(),
                    w.getCity(),
                    w.getState()));
        }

        if (warehouses.size() > 20) {
            sb.append(String.format("\n... 还有 %d 个仓库未显示", warehouses.size() - 20));
        }

        return sb.toString();
    }

    /**
     * 将仓库转换为 Map
     */
    private Map<String, Object> warehouseToMap(FbaWarehouse warehouse) {
        return Map.of(
                "warehouseCode", warehouse.getWarehouseCode(),
                "warehouseName", warehouse.getWarehouseName(),
                "warehouseType", warehouse.getWarehouseType(),
                "country", warehouse.getCountry(),
                "state", warehouse.getState() != null ? warehouse.getState() : "",
                "city", warehouse.getCity() != null ? warehouse.getCity() : "",
                "acceptingShipments", warehouse.getAcceptingShipments()
        );
    }
}
