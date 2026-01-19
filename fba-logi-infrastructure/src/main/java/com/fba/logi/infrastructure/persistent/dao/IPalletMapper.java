package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.PalletPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 托盘 Mapper
 */
@Mapper
public interface IPalletMapper extends BaseMapper<PalletPO> {

    /**
     * 根据订单 ID 查询托盘列表
     *
     * @param orderId 订单 ID
     * @return 托盘列表
     */
    @Select("SELECT * FROM pallet WHERE order_id = #{orderId}")
    List<PalletPO> selectByOrderId(@Param("orderId") String orderId);

    /**
     * 更新托盘标签验证状态
     *
     * @param palletId      托盘 ID
     * @param labelVerified 是否验证
     * @return 影响行数
     */
    @Update("UPDATE pallet SET label_verified = #{labelVerified}, update_time = NOW() " +
            "WHERE pallet_id = #{palletId}")
    int updateLabelVerified(@Param("palletId") String palletId, @Param("labelVerified") Boolean labelVerified);

    /**
     * 更新托盘状态
     *
     * @param palletId 托盘 ID
     * @param state    状态
     * @return 影响行数
     */
    @Update("UPDATE pallet SET state = #{state}, update_time = NOW() WHERE pallet_id = #{palletId}")
    int updateState(@Param("palletId") String palletId, @Param("state") String state);

}
