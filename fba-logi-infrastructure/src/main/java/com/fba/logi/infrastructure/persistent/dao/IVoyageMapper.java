package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.VoyagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 航次 Mapper
 */
@Mapper
public interface IVoyageMapper extends BaseMapper<VoyagePO> {

    /**
     * 更新开船时间和状态
     */
    @Update("UPDATE voyage SET actual_departure = #{actualDeparture}, status = #{status}, update_time = NOW() " +
            "WHERE voyage_id = #{voyageId}")
    int updateDeparture(@Param("voyageId") Long voyageId,
                        @Param("actualDeparture") LocalDateTime actualDeparture,
                        @Param("status") String status);

    /**
     * 更新到港时间和状态
     */
    @Update("UPDATE voyage SET actual_arrival = #{actualArrival}, status = #{status}, update_time = NOW() " +
            "WHERE voyage_id = #{voyageId}")
    int updateArrival(@Param("voyageId") Long voyageId,
                      @Param("actualArrival") LocalDateTime actualArrival,
                      @Param("status") String status);
}
