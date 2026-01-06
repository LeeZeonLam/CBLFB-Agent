package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.ShippingChannelPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 渠道 Mapper
 */
@Mapper
public interface IShippingChannelMapper extends BaseMapper<ShippingChannelPO> {
}
