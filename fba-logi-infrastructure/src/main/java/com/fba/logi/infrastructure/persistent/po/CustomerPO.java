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
 * 客户持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("customer")
public class CustomerPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;
    private String customerNo;
    private String companyName;
    private String contactName;
    private String contactPhone;
    private String email;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
