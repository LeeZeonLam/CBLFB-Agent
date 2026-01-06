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
 * 集装箱持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("container")
public class ContainerPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long containerId;
    private String containerNo;
    private String containerType;
    private String sealNo;
    private BigDecimal maxCapacity;
    private BigDecimal maxWeight;
    private BigDecimal currentCapacity;
    private BigDecimal currentWeight;
    private Long voyageId;
    private String voyageNo;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
