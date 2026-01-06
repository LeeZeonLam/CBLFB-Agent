package com.fba.logi.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 材积记录持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dimension_record")
public class DimensionRecordPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;
    private Long orderId;
    private Long cartonId;
    private Integer measureSequence;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal actualWeight;
    private BigDecimal volume;
    private BigDecimal volumetricWeight;
    private BigDecimal chargeableWeight;
    private Boolean oversized;
    private Boolean overweight;
    private String measuredBy;
    private LocalDateTime measuredTime;
    private Boolean isFinal;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
