package com.fba.logi.domain.marketing.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 奖品实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Award {

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

}
