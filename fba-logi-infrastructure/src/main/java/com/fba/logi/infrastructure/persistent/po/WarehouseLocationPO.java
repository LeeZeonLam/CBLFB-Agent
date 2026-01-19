package com.fba.logi.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 库位持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("warehouse_location")
public class WarehouseLocationPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 库位 ID
     */
    private Long locationId;

    /**
     * 库位编号
     */
    private String locationCode;

    /**
     * 仓库代码
     */
    private String warehouseCode;

    /**
     * 库区
     */
    private String zone;

    /**
     * 排
     */
    private String row;

    /**
     * 列
     */
    private String col;

    /**
     * 层
     */
    private String level;

    /**
     * 库位类型：PALLET/CARTON/BULK
     */
    private String locationType;

    /**
     * 状态：AVAILABLE/OCCUPIED/RESERVED/DISABLED
     */
    private String status;

    /**
     * 当前托盘 ID
     */
    private Long currentPalletId;

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
