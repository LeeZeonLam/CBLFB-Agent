package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.OrderProductPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单产品关联 Mapper
 */
@Mapper
public interface IOrderProductMapper extends BaseMapper<OrderProductPO> {

    /**
     * 根据订单ID查询产品列表
     */
    @Select("SELECT * FROM order_product WHERE order_id = #{orderId}")
    List<OrderProductPO> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 删除订单的所有产品
     */
    @Delete("DELETE FROM order_product WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计订单产品数量
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM order_product WHERE order_id = #{orderId}")
    Integer countProductsByOrderId(@Param("orderId") Long orderId);
}
