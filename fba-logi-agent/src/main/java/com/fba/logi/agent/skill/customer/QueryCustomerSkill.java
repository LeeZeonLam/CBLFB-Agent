package com.fba.logi.agent.skill.customer;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.customer.model.entity.Customer;
import com.fba.logi.domain.customer.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询客户信息 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryCustomerSkill extends AbstractSkill {

    private final ICustomerService customerService;

    @Override
    public String getSkillId() {
        return "query_customer";
    }

    @Override
    public String getSkillName() {
        return "查询客户";
    }

    @Override
    public String getDescription() {
        return "查询客户基本信息，支持按客户编号或客户 ID 查询";
    }

    @Override
    public String getDomain() {
        return "customer";
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
                                "customerNo", "客户编号"),
                        SkillParameterSchema.ParameterDefinition.integerParam(
                                "customerId", "客户 ID"),
                        SkillParameterSchema.ParameterDefinition.booleanParam(
                                "listAll", "列出所有客户（默认 false）")
                ))
                .required(List.of())
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String customerNo = getOptionalString(parameters, "customerNo", null);
        Integer customerId = getOptionalInt(parameters, "customerId", null);
        boolean listAll = getOptionalBoolean(parameters, "listAll", false);

        try {
            // 按编号查询
            if (customerNo != null && !customerNo.isEmpty()) {
                Customer customer = customerService.getCustomerByNo(customerNo);
                return SkillResult.success(
                        formatCustomerDetail(customer),
                        Map.of("customer", customerToMap(customer))
                );
            }

            // 按 ID 查询
            if (customerId != null) {
                Customer customer = customerService.getCustomerById(customerId.longValue());
                return SkillResult.success(
                        formatCustomerDetail(customer),
                        Map.of("customer", customerToMap(customer))
                );
            }

            // 列出所有客户
            if (listAll) {
                List<Customer> customers = customerService.getActiveCustomers();
                if (customers.isEmpty()) {
                    return SkillResult.success("暂无客户数据", Map.of("customers", List.of()));
                }

                return SkillResult.success(
                        formatCustomerList(customers),
                        Map.of(
                                "customers", customers.stream()
                                        .map(this::customerToMap)
                                        .collect(Collectors.toList()),
                                "count", customers.size()
                        )
                );
            }

            return SkillResult.failure("请提供客户编号、客户 ID 或设置 listAll=true", "MISSING_PARAMETER");

        } catch (Exception e) {
            log.error("查询客户失败: {}", e.getMessage(), e);
            return SkillResult.failure("查询失败: " + e.getMessage(), "QUERY_FAILED");
        }
    }

    /**
     * 格式化客户详情
     */
    private String formatCustomerDetail(Customer customer) {
        StringBuilder sb = new StringBuilder();
        sb.append("客户信息:\n");
        sb.append(String.format("客户编号: %s\n", customer.getCustomerNo()));
        sb.append(String.format("公司名称: %s\n", customer.getCompanyName()));
        sb.append(String.format("联系人: %s\n", customer.getContactName()));
        sb.append(String.format("联系电话: %s\n", customer.getContactPhone()));
        if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
            sb.append(String.format("邮箱: %s\n", customer.getEmail()));
        }
        sb.append(String.format("状态: %s", "ACTIVE".equals(customer.getStatus()) ? "活跃" : "停用"));
        return sb.toString();
    }

    /**
     * 格式化客户列表
     */
    private String formatCustomerList(List<Customer> customers) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("找到 %d 个客户:\n\n", customers.size()));

        for (int i = 0; i < Math.min(customers.size(), 20); i++) {
            Customer c = customers.get(i);
            sb.append(String.format("%d. %s - %s (%s)\n",
                    i + 1,
                    c.getCustomerNo(),
                    c.getCompanyName(),
                    c.getContactName()));
        }

        if (customers.size() > 20) {
            sb.append(String.format("\n... 还有 %d 个客户未显示", customers.size() - 20));
        }

        return sb.toString();
    }

    /**
     * 将客户转换为 Map
     */
    private Map<String, Object> customerToMap(Customer customer) {
        Map<String, Object> map = new HashMap<>();
        map.put("customerId", customer.getCustomerId());
        map.put("customerNo", customer.getCustomerNo());
        map.put("companyName", customer.getCompanyName());
        map.put("contactName", customer.getContactName());
        map.put("contactPhone", customer.getContactPhone());
        if (customer.getEmail() != null) {
            map.put("email", customer.getEmail());
        }
        map.put("status", customer.getStatus());
        return map;
    }

    /**
     * 获取可选的整数参数
     */
    private Integer getOptionalInt(Map<String, Object> parameters, String key, Integer defaultValue) {
        Object value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取可选的布尔参数
     */
    private boolean getOptionalBoolean(Map<String, Object> parameters, String key, boolean defaultValue) {
        Object value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
