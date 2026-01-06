package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.DeliveryWarehousePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交货仓库 Mapper
 */
@Mapper
public interface IDeliveryWarehouseMapper extends BaseMapper<DeliveryWarehousePO> {
}
