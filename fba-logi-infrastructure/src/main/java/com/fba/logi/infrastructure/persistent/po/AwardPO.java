package com.fba.logi.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 奖品持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("award")
public class AwardPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 奖品 ID
     */
    private Integer awardId;

    /**
     * 奖品 Key（唯一标识）
     */
    private String awardKey;

    /**
     * 奖品配置（JSON）
     */
    private String awardConfig;

    /**
     * 奖品描述
     */
    private String awardDesc;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
