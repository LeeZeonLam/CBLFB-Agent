package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.WarehouseLocationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 库位 Mapper
 */
@Mapper
public interface IWarehouseLocationMapper extends BaseMapper<WarehouseLocationPO> {

    /**
     * 根据库位编号查询
     */
    @Select("SELECT * FROM warehouse_location WHERE location_code = #{locationCode}")
    WarehouseLocationPO selectByLocationCode(@Param("locationCode") String locationCode);

    /**
     * 查询可用库位
     */
    @Select("SELECT * FROM warehouse_location WHERE warehouse_code = #{warehouseCode} " +
            "AND location_type = #{locationType} AND status = 'AVAILABLE'")
    List<WarehouseLocationPO> selectAvailableLocations(@Param("warehouseCode") String warehouseCode,
                                                        @Param("locationType") String locationType);

    /**
     * 更新库位状态
     */
    @Update("UPDATE warehouse_location SET status = #{status}, update_time = NOW() WHERE location_id = #{locationId}")
    int updateStatus(@Param("locationId") Long locationId, @Param("status") String status);

    /**
     * 分配托盘到库位
     */
    @Update("UPDATE warehouse_location SET current_pallet_id = #{palletId}, status = 'OCCUPIED', " +
            "update_time = NOW() WHERE location_id = #{locationId}")
    int assignPallet(@Param("locationId") Long locationId, @Param("palletId") Long palletId);

    /**
     * 释放库位
     */
    @Update("UPDATE warehouse_location SET current_pallet_id = NULL, status = 'AVAILABLE', " +
            "update_time = NOW() WHERE location_id = #{locationId}")
    int releaseLocation(@Param("locationId") Long locationId);

}
