package com.fba.logi.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fba.logi.domain.marketing.model.entity.Activity;
import com.fba.logi.domain.marketing.repository.IActivityRepository;
import com.fba.logi.infrastructure.persistent.dao.IActivityMapper;
import com.fba.logi.infrastructure.persistent.po.ActivityPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ActivityRepository implements IActivityRepository {

    private final IActivityMapper activityMapper;

    @Override
    public Activity queryActivityById(Long activityId) {
        LambdaQueryWrapper<ActivityPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityPO::getActivityId, activityId);
        ActivityPO po = activityMapper.selectOne(wrapper);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<Activity> queryValidActivities() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ActivityPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityPO::getState, "open")
                .le(ActivityPO::getBeginDateTime, now)
                .ge(ActivityPO::getEndDateTime, now)
                .gt(ActivityPO::getStockCountSurplus, 0);
        return activityMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deductActivityStock(Long activityId) {
        return activityMapper.deductStock(activityId) > 0;
    }

    @Override
    public void saveActivity(Activity activity) {
        ActivityPO po = convertToPO(activity);
        if (po.getId() == null) {
            activityMapper.insert(po);
        } else {
            activityMapper.updateById(po);
        }
    }

    @Override
    public void updateActivityState(Long activityId, String state) {
        activityMapper.updateState(activityId, state);
    }

    /**
     * PO 转换为实体
     */
    private Activity convertToEntity(ActivityPO po) {
        return Activity.builder()
                .activityId(po.getActivityId())
                .activityName(po.getActivityName())
                .activityDesc(po.getActivityDesc())
                .beginDateTime(po.getBeginDateTime())
                .endDateTime(po.getEndDateTime())
                .stockCount(po.getStockCount())
                .stockCountSurplus(po.getStockCountSurplus())
                .state(po.getState())
                .build();
    }

    /**
     * 实体转换为 PO
     */
    private ActivityPO convertToPO(Activity entity) {
        return ActivityPO.builder()
                .activityId(entity.getActivityId())
                .activityName(entity.getActivityName())
                .activityDesc(entity.getActivityDesc())
                .beginDateTime(entity.getBeginDateTime())
                .endDateTime(entity.getEndDateTime())
                .stockCount(entity.getStockCount())
                .stockCountSurplus(entity.getStockCountSurplus())
                .state(entity.getState())
                .build();
    }

}
