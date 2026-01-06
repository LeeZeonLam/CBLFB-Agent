package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.ThirdPartyWarehousePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 第三方海外仓 Mapper
 */
@Mapper
public interface IThirdPartyWarehouseMapper extends BaseMapper<ThirdPartyWarehousePO> {
}
