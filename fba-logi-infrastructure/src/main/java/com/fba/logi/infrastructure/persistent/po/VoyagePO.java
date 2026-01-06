package com.fba.logi.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 航次持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("voyage")
public class VoyagePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long voyageId;
    private String voyageNo;
    private String vesselName;
    private String carrier;
    private String channelCode;
    private String channelName;
    private String departurePort;
    private String arrivalPort;
    private LocalDateTime estimatedDeparture;
    private LocalDateTime actualDeparture;
    private LocalDateTime estimatedArrival;
    private LocalDateTime actualArrival;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
