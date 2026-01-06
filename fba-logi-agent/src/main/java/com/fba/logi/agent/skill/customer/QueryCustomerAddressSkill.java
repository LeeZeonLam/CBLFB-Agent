package com.fba.logi.agent.skill.customer;

import com.fba.logi.agent.skill.*;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.customer.model.entity.Customer;
import com.fba.logi.domain.customer.model.entity.CustomerAddress;
import com.fba.logi.domain.customer.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询客户地址 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryCustomerAddressSkill extends AbstractSkill {

    private final ICustomerService customerService;

    @Override
    public String getSkillId() {
        return "query_customer_address";
    }

    @Override
    public String getSkillName() {
        return "查询客户地址";
    }

    @Override
    public String getDescription() {
        return "查询客户的派送地址（商业地址/住宅地址）";
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
                        SkillParameterSchema.ParameterDefinition.enumParam(
                                "addressType", "地址类型",
                                List.of("COMMERCIAL", "RESIDENTIAL", "ALL"))
                ))
                .required(List.of())
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        String customerNo = getOptionalString(parameters, "customerNo", null);
        Integer customerIdParam = getOptionalInt(parameters, "customerId", null);
        String addressType = getOptionalString(parameters, "addressType", "ALL");

        try {
            // 获取客户 ID
            Long customerId;
            String customerName;

            if (customerNo != null && !customerNo.isEmpty()) {
                Customer customer = customerService.getCustomerByNo(customerNo);
                customerId = customer.getCustomerId();
                customerName = customer.getCompanyName();
            } else if (customerIdParam != null) {
                Customer customer = customerService.getCustomerById(customerIdParam.longValue());
                customerId = customer.getCustomerId();
                customerName = customer.getCompanyName();
            } else {
                return SkillResult.failure("请提供客户编号或客户 ID", "MISSING_PARAMETER");
            }

            // 查询地址
            List<CustomerAddress> addresses;
            if ("ALL".equals(addressType)) {
                addresses = customerService.getCustomerAddresses(customerId);
            } else {
                addresses = customerService.getCustomerAddressesByType(customerId, addressType);
            }

            if (addresses.isEmpty()) {
                return SkillResult.success(
                        String.format("客户 %s 暂无%s", customerName, getAddressTypeDisplay(addressType)),
                        Map.of("addresses", List.of())
                );
            }

            // 格式化输出
            String result = formatAddressList(customerName, addresses);

            return SkillResult.success(
                    result,
                    Map.of(
                            "customerId", customerId,
                            "customerName", customerName,
                            "addresses", addresses.stream()
                                    .map(this::addressToMap)
                                    .collect(Collectors.toList()),
                            "count", addresses.size()
                    )
            );

        } catch (Exception e) {
            log.error("查询客户地址失败: {}", e.getMessage(), e);
            return SkillResult.failure("查询失败: " + e.getMessage(), "QUERY_FAILED");
        }
    }

    /**
     * 格式化地址列表
     */
    private String formatAddressList(String customerName, List<CustomerAddress> addresses) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("客户 %s 的地址（共 %d 个）:\n\n", customerName, addresses.size()));

        for (int i = 0; i < addresses.size(); i++) {
            CustomerAddress addr = addresses.get(i);
            sb.append(String.format("%d. %s%s\n",
                    i + 1,
                    addr.getAddressName() != null ? addr.getAddressName() : "地址" + (i + 1),
                    Boolean.TRUE.equals(addr.getIsDefault()) ? " [默认]" : ""));
            sb.append(String.format("   类型: %s\n", getAddressTypeDisplay(addr.getAddressType())));
            sb.append(String.format("   收件人: %s\n", addr.getRecipientName()));
            if (addr.getCompanyName() != null && !addr.getCompanyName().isEmpty()) {
                sb.append(String.format("   公司: %s\n", addr.getCompanyName()));
            }
            sb.append(String.format("   地址: %s\n", addr.getAddressLine1()));
            if (addr.getAddressLine2() != null && !addr.getAddressLine2().isEmpty()) {
                sb.append(String.format("         %s\n", addr.getAddressLine2()));
            }
            sb.append(String.format("   城市: %s, %s %s, %s\n",
                    addr.getCity(),
                    addr.getState(),
                    addr.getZipCode(),
                    addr.getCountryCode()));
            sb.append(String.format("   电话: %s\n", addr.getPhone()));
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 获取地址类型显示名称
     */
    private String getAddressTypeDisplay(String addressType) {
        if (addressType == null || "ALL".equals(addressType)) {
            return "派送地址";
        }
        switch (addressType) {
            case Constants.AddressType.COMMERCIAL:
                return "商业地址";
            case Constants.AddressType.RESIDENTIAL:
                return "住宅地址";
            default:
                return addressType;
        }
    }

    /**
     * 将地址转换为 Map
     */
    private Map<String, Object> addressToMap(CustomerAddress address) {
        Map<String, Object> map = new HashMap<>();
        map.put("addressId", address.getAddressId());
        map.put("addressType", address.getAddressType());
        if (address.getAddressName() != null) {
            map.put("addressName", address.getAddressName());
        }
        map.put("recipientName", address.getRecipientName());
        if (address.getCompanyName() != null) {
            map.put("companyName", address.getCompanyName());
        }
        map.put("addressLine1", address.getAddressLine1());
        if (address.getAddressLine2() != null) {
            map.put("addressLine2", address.getAddressLine2());
        }
        map.put("city", address.getCity());
        map.put("state", address.getState());
        map.put("zipCode", address.getZipCode());
        map.put("countryCode", address.getCountryCode());
        map.put("phone", address.getPhone());
        map.put("isDefault", address.getIsDefault());
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
}
