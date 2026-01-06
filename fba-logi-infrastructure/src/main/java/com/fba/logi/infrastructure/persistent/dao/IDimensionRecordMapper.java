package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.DimensionRecordPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 材积记录 Mapper
 */
@Mapper
public interface IDimensionRecordMapper extends BaseMapper<DimensionRecordPO> {

    /**
     * 将记录标记为最终确认值
     */
    @Update("UPDATE dimension_record SET is_final = TRUE, update_time = NOW() WHERE record_id = #{recordId}")
    int markAsFinal(@Param("recordId") Long recordId);

    /**
     * 取消订单其他记录的最终标记
     */
    @Update("UPDATE dimension_record SET is_final = FALSE, update_time = NOW() " +
            "WHERE order_id = #{orderId} AND record_id != #{recordId} AND is_final = TRUE")
    int clearOtherFinalMarks(@Param("orderId") Long orderId, @Param("recordId") Long recordId);
}
