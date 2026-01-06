package com.fba.logi.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fba.logi.domain.marketing.model.entity.Strategy;
import com.fba.logi.domain.marketing.model.entity.StrategyAward;
import com.fba.logi.domain.marketing.repository.IStrategyRepository;
import com.fba.logi.infrastructure.persistent.dao.IStrategyAwardMapper;
import com.fba.logi.infrastructure.persistent.dao.IStrategyMapper;
import com.fba.logi.infrastructure.persistent.po.StrategyAwardPO;
import com.fba.logi.infrastructure.persistent.po.StrategyPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 策略仓储实现
 */
@Repository
@RequiredArgsConstructor
public class StrategyRepository implements IStrategyRepository {

    private final IStrategyMapper strategyMapper;
    private final IStrategyAwardMapper strategyAwardMapper;

    @Override
    public Strategy queryStrategyById(Long strategyId) {
        LambdaQueryWrapper<StrategyPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrategyPO::getStrategyId, strategyId);
        StrategyPO po = strategyMapper.selectOne(wrapper);
        if (po == null) {
            return null;
        }

        List<StrategyAward> awards = queryStrategyAwardList(strategyId);
        return Strategy.builder()
                .strategyId(po.getStrategyId())
                .strategyDesc(po.getStrategyDesc())
                .strategyAwards(awards)
                .build();
    }

    @Override
    public List<StrategyAward> queryStrategyAwardList(Long strategyId) {
        LambdaQueryWrapper<StrategyAwardPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrategyAwardPO::getStrategyId, strategyId)
                .orderByAsc(StrategyAwardPO::getSort);
        return strategyAwardMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public StrategyAward queryStrategyAward(Long strategyId, Integer awardId) {
        LambdaQueryWrapper<StrategyAwardPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrategyAwardPO::getStrategyId, strategyId)
                .eq(StrategyAwardPO::getAwardId, awardId);
        StrategyAwardPO po = strategyAwardMapper.selectOne(wrapper);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public boolean deductAwardStock(Long strategyId, Integer awardId) {
        return strategyAwardMapper.deductStock(strategyId, awardId) > 0;
    }

    @Override
    public void saveStrategy(Strategy strategy) {
        StrategyPO po = StrategyPO.builder()
                .strategyId(strategy.getStrategyId())
                .strategyDesc(strategy.getStrategyDesc())
                .build();

        LambdaQueryWrapper<StrategyPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrategyPO::getStrategyId, strategy.getStrategyId());
        if (strategyMapper.selectCount(wrapper) > 0) {
            strategyMapper.update(po, wrapper);
        } else {
            strategyMapper.insert(po);
        }
    }

    @Override
    public void saveStrategyAward(StrategyAward strategyAward) {
        StrategyAwardPO po = convertToPO(strategyAward);

        LambdaQueryWrapper<StrategyAwardPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrategyAwardPO::getStrategyId, strategyAward.getStrategyId())
                .eq(StrategyAwardPO::getAwardId, strategyAward.getAwardId());
        if (strategyAwardMapper.selectCount(wrapper) > 0) {
            strategyAwardMapper.update(po, wrapper);
        } else {
            strategyAwardMapper.insert(po);
        }
    }

    /**
     * PO 转换为实体
     */
    private StrategyAward convertToEntity(StrategyAwardPO po) {
        return StrategyAward.builder()
                .strategyId(po.getStrategyId())
                .awardId(po.getAwardId())
                .awardTitle(po.getAwardTitle())
                .awardSubtitle(po.getAwardSubtitle())
                .awardCount(po.getAwardCount())
                .awardCountSurplus(po.getAwardCountSurplus())
                .awardRate(po.getAwardRate())
                .ruleModels(po.getRuleModels())
                .sort(po.getSort())
                .build();
    }

    /**
     * 实体转换为 PO
     */
    private StrategyAwardPO convertToPO(StrategyAward entity) {
        return StrategyAwardPO.builder()
                .strategyId(entity.getStrategyId())
                .awardId(entity.getAwardId())
                .awardTitle(entity.getAwardTitle())
                .awardSubtitle(entity.getAwardSubtitle())
                .awardCount(entity.getAwardCount())
                .awardCountSurplus(entity.getAwardCountSurplus())
                .awardRate(entity.getAwardRate())
                .ruleModels(entity.getRuleModels())
                .sort(entity.getSort())
                .build();
    }

}
