package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.ShipmentOrderPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 发货订单 Mapper
 */
@Mapper
public interface IShipmentOrderMapper extends BaseMapper<ShipmentOrderPO> {

    /**
     * 更新订单状态
     */
    @Update("UPDATE shipment_order SET state = #{state}, update_time = NOW() WHERE order_id = #{orderId}")
    int updateState(@Param("orderId") Long orderId, @Param("state") String state);

    /**
     * 批量更新订单状态
     */
    @Update("<script>" +
            "UPDATE shipment_order SET state = #{state}, update_time = NOW() " +
            "WHERE order_id IN " +
            "<foreach collection='orderIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateState(@Param("orderIds") List<Long> orderIds, @Param("state") String state);

    /**
     * 更新柜子信息
     */
    @Update("UPDATE shipment_order SET container_id = #{containerId}, container_no = #{containerNo}, update_time = NOW() " +
            "WHERE order_id = #{orderId}")
    int updateContainerInfo(@Param("orderId") Long orderId,
                            @Param("containerId") Long containerId,
                            @Param("containerNo") String containerNo);

    /**
     * 更新装柜时间
     */
    @Update("UPDATE shipment_order SET loaded_time = #{loadedTime}, update_time = NOW() WHERE order_id = #{orderId}")
    int updateLoadedTime(@Param("orderId") Long orderId, @Param("loadedTime") LocalDateTime loadedTime);

    /**
     * 批量更新开船时间
     */
    @Update("<script>" +
            "UPDATE shipment_order SET actual_departure_time = #{departureTime}, update_time = NOW() " +
            "WHERE order_id IN " +
            "<foreach collection='orderIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateDepartureTime(@Param("orderIds") List<Long> orderIds, @Param("departureTime") LocalDateTime departureTime);

    /**
     * 批量更新到港时间
     */
    @Update("<script>" +
            "UPDATE shipment_order SET actual_arrival_port_time = #{arrivalTime}, update_time = NOW() " +
            "WHERE order_id IN " +
            "<foreach collection='orderIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateArrivalTime(@Param("orderIds") List<Long> orderIds, @Param("arrivalTime") LocalDateTime arrivalTime);

    /**
     * 批量更新提柜时间
     */
    @Update("<script>" +
            "UPDATE shipment_order SET picked_time = #{pickedTime}, update_time = NOW() " +
            "WHERE order_id IN " +
            "<foreach collection='orderIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdatePickedTime(@Param("orderIds") List<Long> orderIds, @Param("pickedTime") LocalDateTime pickedTime);

    /**
     * 生成订单号
     */
    @Select("SELECT CONCAT('FBA', TO_CHAR(NOW(), 'YYYYMMDD'), LPAD(COALESCE(MAX(CAST(SUBSTRING(order_no, 12) AS INTEGER)), 0) + 1, 6, '0')) " +
            "FROM shipment_order WHERE order_no LIKE CONCAT('FBA', TO_CHAR(NOW(), 'YYYYMMDD'), '%')")
    String generateOrderNo();
}
