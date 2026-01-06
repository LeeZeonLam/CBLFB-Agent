package com.fba.logi.domain.marketing.service.impl;

import com.fba.logi.common.exception.BusinessException;
import com.fba.logi.domain.marketing.model.entity.Activity;
import com.fba.logi.domain.marketing.model.entity.StrategyAward;
import com.fba.logi.domain.marketing.repository.IActivityRepository;
import com.fba.logi.domain.marketing.repository.IStrategyRepository;
import com.fba.logi.domain.marketing.service.IRaffleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

/**
 * 抽奖服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RaffleServiceImpl implements IRaffleService {

    private final IStrategyRepository strategyRepository;
    private final IActivityRepository activityRepository;
    private final SecureRandom random = new SecureRandom();

    @Override
    public StrategyAward performRaffle(Long strategyId, String userId) {
        log.info("执行抽奖，策略ID: {}，用户ID: {}", strategyId, userId);

        // 1. 查询策略奖品列表
        List<StrategyAward> awards = strategyRepository.queryStrategyAwardList(strategyId);
        if (awards == null || awards.isEmpty()) {
            throw BusinessException.notFound("策略奖品列表为空");
        }

        // 2. 执行抽奖算法（权重随机）
        StrategyAward selectedAward = doRaffle(awards);

        // 3. 扣减库存
        boolean deducted = strategyRepository.deductAwardStock(strategyId, selectedAward.getAwardId());
        if (!deducted) {
            log.warn("奖品库存不足，奖品ID: {}", selectedAward.getAwardId());
            // 库存不足时，重新抽奖或返回兜底奖品
            selectedAward = getFallbackAward(awards);
        }

        log.info("抽奖完成，用户: {} 获得奖品: {}", userId, selectedAward.getAwardTitle());
        return selectedAward;
    }

    @Override
    public StrategyAward performRaffleWithActivity(Long activityId, String userId) {
        log.info("执行活动抽奖，活动ID: {}，用户ID: {}", activityId, userId);

        // 1. 查询活动
        Activity activity = activityRepository.queryActivityById(activityId);
        if (activity == null) {
            throw BusinessException.notFound("活动不存在");
        }

        // 2. 校验活动状态
        if (!activity.isValid()) {
            throw new BusinessException("活动未开启或已结束");
        }

        // 3. 扣减活动库存
        boolean deducted = activityRepository.deductActivityStock(activityId);
        if (!deducted) {
            throw new BusinessException("活动库存不足");
        }

        // 4. 执行抽奖
        return performRaffle(activity.getStrategyId(), userId);
    }

    /**
     * 执行抽奖算法（基于概率权重）
     */
    private StrategyAward doRaffle(List<StrategyAward> awards) {
        // 计算总概率
        BigDecimal totalRate = awards.stream()
                .map(StrategyAward::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 生成随机数
        BigDecimal randomValue = BigDecimal.valueOf(random.nextDouble()).multiply(totalRate);

        // 查找中奖奖品
        BigDecimal currentRate = BigDecimal.ZERO;
        for (StrategyAward award : awards) {
            currentRate = currentRate.add(award.getAwardRate());
            if (randomValue.compareTo(currentRate) <= 0) {
                return award;
            }
        }

        // 兜底返回最后一个奖品
        return awards.get(awards.size() - 1);
    }

    /**
     * 获取兜底奖品（库存最多的奖品）
     */
    private StrategyAward getFallbackAward(List<StrategyAward> awards) {
        return awards.stream()
                .filter(a -> a.getAwardCountSurplus() > 0)
                .max((a, b) -> a.getAwardCountSurplus().compareTo(b.getAwardCountSurplus()))
                .orElseThrow(() -> new BusinessException("所有奖品库存已耗尽"));
    }

}
