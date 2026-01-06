package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.StrategyPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 策略 Mapper
 */
@Mapper
public interface IStrategyMapper extends BaseMapper<StrategyPO> {

}
