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
 * 报关单持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("customs_declaration")
public class CustomsDeclarationPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long declarationId;
    private String declarationNo;

    // 关联信息
    private Long containerId;
    private String containerNo;
    private Long voyageId;

    // 报关信息
    private String declarationType;
    private String customsPort;
    private String brokerName;
    private String brokerContact;
    private String brokerPhone;

    // 状态
    private String status;

    // 申报信息
    private BigDecimal declaredValue;
    private String currency;
    private BigDecimal declaredWeight;
    private Integer declaredPieces;
    private String hsCodes;
    private String goodsDescription;

    // 文件和备注
    private String documentUrls;
    private String inspectionReason;
    private String rejectReason;
    private String remark;

    // 时间节点
    private LocalDateTime declaredTime;
    private LocalDateTime clearedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
