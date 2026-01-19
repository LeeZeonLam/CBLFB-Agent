package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.AwardPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 奖品 Mapper
 */
@Mapper
public interface IAwardMapper extends BaseMapper<AwardPO> {

}
