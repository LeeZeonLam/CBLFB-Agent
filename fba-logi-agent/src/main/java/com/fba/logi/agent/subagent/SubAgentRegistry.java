package com.fba.logi.agent.subagent;

import com.fba.logi.agent.core.AgentConfig;
import com.fba.logi.agent.core.AgentRouter;
import com.fba.logi.common.constants.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sub-Agent 注册中心
 * 管理所有可被委托的子Agent
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubAgentRegistry {

    private final AgentRouter agentRouter;

    private final Map<String, SubAgentConfig> subAgents = new LinkedHashMap<>();
    private final Map<String, List<SubAgentConfig>> domainAgents = new HashMap<>();

    @PostConstruct
    public void init() {
        // 注册营销域子Agent
        registerSubAgent(SubAgentConfig.builder()
                .agentType(Constants.AgentType.MARKETING_STRATEGIST)
                .agentName("营销策略官")
                .domain("marketing")
                .responsibility("配置和管理抽奖活动、营销策略、奖品设置")
                .applicableScenarios(List.of(
                        "用户想创建新的营销活动",
                        "用户询问如何配置抽奖策略",
                        "用户要修改活动参数"
                ))
                .priority(10)
                .build());

        registerSubAgent(SubAgentConfig.builder()
                .agentType(Constants.AgentType.MARKETING_SALES)
                .agentName("销售助手")
                .domain("marketing")
                .responsibility("引导客户参与活动、执行抽奖、查询中奖结果")
                .applicableScenarios(List.of(
                        "用户想参与抽奖活动",
                        "用户询问有什么优惠活动",
                        "用户查询抽奖结果"
                ))
                .priority(20)
                .build());

        // 注册订单域子Agent
        registerSubAgent(SubAgentConfig.builder()
                .agentType(Constants.AgentType.ORDER_BOOKING)
                .agentName("订舱助手")
                .domain("order")
                .responsibility("帮助客户创建发货订单、解析文档、查询运费")
                .applicableScenarios(List.of(
                        "用户要发货到FBA仓库",
                        "用户上传了托书或订单文档",
                        "用户询问运费报价"
                ))
                .priority(10)
                .build());

        registerSubAgent(SubAgentConfig.builder()
                .agentType(Constants.AgentType.ORDER_AUDITOR)
                .agentName("订单审核官")
                .domain("order")
                .responsibility("审核订单合规性、检查禁运品、验证FBA仓库代码")
                .applicableScenarios(List.of(
                        "需要审核订单是否合规",
                        "检查货物是否包含禁运品",
                        "验证FBA仓库代码是否正确"
                ))
                .priority(20)
                .delegatable(false) // 审核官不能被外部直接委托
                .build());

        // 注册仓储域子Agent
        registerSubAgent(SubAgentConfig.builder()
                .agentType(Constants.AgentType.WAREHOUSE_OPS)
                .agentName("仓库主管")
                .domain("warehouse")
                .responsibility("管理入库操作、材积录入、FBA标签校验、超重预警")
                .applicableScenarios(List.of(
                        "货物到仓需要入库",
                        "需要录入货物尺寸重量",
                        "检查货物是否超重超尺"
                ))
                .priority(10)
                .build());

        log.info("SubAgentRegistry 初始化完成，注册了 {} 个子Agent", subAgents.size());
    }

    /**
     * 注册子Agent
     */
    public void registerSubAgent(SubAgentConfig config) {
        subAgents.put(config.getAgentType(), config);
        domainAgents.computeIfAbsent(config.getDomain(), k -> new ArrayList<>()).add(config);
        log.debug("注册 SubAgent: {} [{}]", config.getAgentType(), config.getAgentName());
    }

    /**
     * 获取所有可委托的子Agent
     */
    public List<SubAgentConfig> getDelegatableAgents() {
        return subAgents.values().stream()
                .filter(SubAgentConfig::isEnabled)
                .filter(SubAgentConfig::isDelegatable)
                .sorted(Comparator.comparingInt(SubAgentConfig::getPriority))
                .collect(Collectors.toList());
    }

    /**
     * 获取某个领域的所有子Agent
     */
    public List<SubAgentConfig> getAgentsByDomain(String domain) {
        List<SubAgentConfig> agents = domainAgents.get(domain);
        if (agents == null) {
            return Collections.emptyList();
        }
        return agents.stream()
                .filter(SubAgentConfig::isEnabled)
                .sorted(Comparator.comparingInt(SubAgentConfig::getPriority))
                .collect(Collectors.toList());
    }

    /**
     * 获取子Agent配置
     */
    public Optional<SubAgentConfig> getSubAgent(String agentType) {
        return Optional.ofNullable(subAgents.get(agentType));
    }

    /**
     * 检查Agent是否可委托
     */
    public boolean isDelegatable(String agentType) {
        SubAgentConfig config = subAgents.get(agentType);
        return config != null && config.isEnabled() && config.isDelegatable();
    }

    /**
     * 生成子Agent选择提示词（供主Agent使用）
     */
    public String generateAgentSelectionPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("## 可用的专家助手\n\n");
        sb.append("根据用户的需求，选择最合适的专家助手来处理：\n\n");

        for (SubAgentConfig agent : getDelegatableAgents()) {
            sb.append("### ").append(agent.getAgentName()).append("\n");
            sb.append("- 标识: `").append(agent.getAgentType()).append("`\n");
            sb.append("- 职责: ").append(agent.getResponsibility()).append("\n");
            sb.append("- 适用场景:\n");
            for (String scenario : agent.getApplicableScenarios()) {
                sb.append("  - ").append(scenario).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 获取所有已注册的Agent类型
     */
    public Set<String> getAllAgentTypes() {
        return new HashSet<>(subAgents.keySet());
    }
}
