package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.ContainerPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 集装箱 Mapper
 */
@Mapper
public interface IContainerMapper extends BaseMapper<ContainerPO> {

    /**
     * 批量更新状态
     */
    @Update("<script>" +
            "UPDATE container SET status = #{status}, update_time = NOW() " +
            "WHERE container_id IN " +
            "<foreach collection='containerIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("containerIds") List<Long> containerIds, @Param("status") String status);

    /**
     * 更新柜子容量
     */
    @Update("UPDATE container SET current_capacity = #{currentCapacity}, current_weight = #{currentWeight}, update_time = NOW() " +
            "WHERE container_id = #{containerId}")
    int updateCapacity(@Param("containerId") Long containerId,
                       @Param("currentCapacity") java.math.BigDecimal currentCapacity,
                       @Param("currentWeight") java.math.BigDecimal currentWeight);
}
