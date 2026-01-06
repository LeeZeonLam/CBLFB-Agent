package com.fba.logi.agent.skill;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 技能注册中心
 * 管理所有可用的 Skill，支持按 Agent 类型获取可用技能
 */
@Slf4j
@Component
public class SkillRegistry {

    /**
     * 所有已注册的 Skill
     */
    private final Map<String, ISkill> skills = new ConcurrentHashMap<>();

    /**
     * Agent 类型到可用 Skill 的映射
     */
    private final Map<String, Set<String>> agentSkillMapping = new ConcurrentHashMap<>();

    /**
     * 领域到 Skill 的映射
     */
    private final Map<String, Set<String>> domainSkillMapping = new ConcurrentHashMap<>();

    /**
     * 自动注入所有实现了 ISkill 接口的 Bean
     */
    private final List<ISkill> autoRegisteredSkills;

    public SkillRegistry(List<ISkill> autoRegisteredSkills) {
        this.autoRegisteredSkills = autoRegisteredSkills != null ? autoRegisteredSkills : new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        // 自动注册所有 Spring 管理的 Skill
        for (ISkill skill : autoRegisteredSkills) {
            registerSkill(skill);
        }
        log.info("SkillRegistry 初始化完成，已注册 {} 个 Skill", skills.size());
    }

    /**
     * 注册 Skill
     */
    public void registerSkill(ISkill skill) {
        String skillId = skill.getSkillId();
        if (skills.containsKey(skillId)) {
            log.warn("Skill '{}' 已存在，将被覆盖", skillId);
        }
        skills.put(skillId, skill);

        // 更新领域映射
        String domain = skill.getDomain();
        domainSkillMapping.computeIfAbsent(domain, k -> new HashSet<>()).add(skillId);

        log.debug("已注册 Skill: {} [{}] - {}", skillId, skill.getSkillName(), skill.getDescription());
    }

    /**
     * 为 Agent 类型绑定可用的 Skill
     */
    public void bindSkillsToAgent(String agentType, String... skillIds) {
        Set<String> skillSet = agentSkillMapping.computeIfAbsent(agentType, k -> new HashSet<>());
        for (String skillId : skillIds) {
            if (skills.containsKey(skillId)) {
                skillSet.add(skillId);
            } else {
                log.warn("尝试绑定不存在的 Skill '{}' 到 Agent '{}'", skillId, agentType);
            }
        }
    }

    /**
     * 为 Agent 类型绑定整个领域的所有 Skill
     */
    public void bindDomainToAgent(String agentType, String domain) {
        Set<String> domainSkills = domainSkillMapping.get(domain);
        if (domainSkills != null) {
            agentSkillMapping.computeIfAbsent(agentType, k -> new HashSet<>()).addAll(domainSkills);
        }
    }

    /**
     * 获取 Skill
     */
    public Optional<ISkill> getSkill(String skillId) {
        return Optional.ofNullable(skills.get(skillId));
    }

    /**
     * 获取 Agent 可用的所有 Skill
     */
    public List<ISkill> getSkillsForAgent(String agentType) {
        Set<String> skillIds = agentSkillMapping.get(agentType);
        if (skillIds == null || skillIds.isEmpty()) {
            return Collections.emptyList();
        }
        return skillIds.stream()
                .map(skills::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取某个领域的所有 Skill
     */
    public List<ISkill> getSkillsByDomain(String domain) {
        Set<String> skillIds = domainSkillMapping.get(domain);
        if (skillIds == null) {
            return Collections.emptyList();
        }
        return skillIds.stream()
                .map(skills::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有已注册的 Skill
     */
    public List<ISkill> getAllSkills() {
        return new ArrayList<>(skills.values());
    }

    /**
     * 检查 Skill 是否存在
     */
    public boolean hasSkill(String skillId) {
        return skills.containsKey(skillId);
    }

    /**
     * 检查 Agent 是否可以使用某个 Skill
     */
    public boolean canAgentUseSkill(String agentType, String skillId) {
        Set<String> skillIds = agentSkillMapping.get(agentType);
        return skillIds != null && skillIds.contains(skillId);
    }

    /**
     * 生成 Agent 可用 Skill 的 Function Calling 定义
     * 供 LLM 使用
     */
    public List<Map<String, Object>> generateFunctionDefinitions(String agentType) {
        List<ISkill> agentSkills = getSkillsForAgent(agentType);
        List<Map<String, Object>> functions = new ArrayList<>();

        for (ISkill skill : agentSkills) {
            Map<String, Object> function = new LinkedHashMap<>();
            function.put("name", skill.getSkillId());
            function.put("description", skill.getDescription());
            function.put("parameters", skill.getParameterSchema().toJsonSchema());
            functions.add(function);
        }

        return functions;
    }

    /**
     * 注销 Skill
     */
    public void unregisterSkill(String skillId) {
        ISkill removed = skills.remove(skillId);
        if (removed != null) {
            // 清理映射
            domainSkillMapping.values().forEach(set -> set.remove(skillId));
            agentSkillMapping.values().forEach(set -> set.remove(skillId));
            log.info("已注销 Skill: {}", skillId);
        }
    }

    /**
     * 获取注册统计信息
     */
    public Map<String, Object> getStats() {
        return Map.of(
                "totalSkills", skills.size(),
                "domains", domainSkillMapping.keySet(),
                "agentMappings", agentSkillMapping.size()
        );
    }
}
