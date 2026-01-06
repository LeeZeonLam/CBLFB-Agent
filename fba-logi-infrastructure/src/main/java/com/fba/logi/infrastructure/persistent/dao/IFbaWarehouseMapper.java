package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.FbaWarehousePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * FBA/AWD 仓库 Mapper
 */
@Mapper
public interface IFbaWarehouseMapper extends BaseMapper<FbaWarehousePO> {
}
