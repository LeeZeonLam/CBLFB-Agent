package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.CartonPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 纸箱 Mapper
 */
@Mapper
public interface ICartonMapper extends BaseMapper<CartonPO> {

    /**
     * 根据订单 ID 查询纸箱列表
     */
    @Select("SELECT * FROM carton WHERE order_id = #{orderId}")
    List<CartonPO> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据托盘 ID 查询纸箱列表
     */
    @Select("SELECT * FROM carton WHERE pallet_id = #{palletId}")
    List<CartonPO> selectByPalletId(@Param("palletId") Long palletId);

    /**
     * 根据纸箱编号查询
     */
    @Select("SELECT * FROM carton WHERE carton_no = #{cartonNo}")
    CartonPO selectByCartonNo(@Param("cartonNo") String cartonNo);

    /**
     * 更新纸箱状态
     */
    @Update("UPDATE carton SET status = #{status}, update_time = NOW() WHERE carton_id = #{cartonId}")
    int updateStatus(@Param("cartonId") Long cartonId, @Param("status") String status);

    /**
     * 分配纸箱到托盘
     */
    @Update("UPDATE carton SET pallet_id = #{palletId}, update_time = NOW() WHERE carton_id = #{cartonId}")
    int assignToPallet(@Param("cartonId") Long cartonId, @Param("palletId") Long palletId);

}
