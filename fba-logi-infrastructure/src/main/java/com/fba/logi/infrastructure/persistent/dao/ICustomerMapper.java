package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.CustomerPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 客户 Mapper
 */
@Mapper
public interface ICustomerMapper extends BaseMapper<CustomerPO> {

    /**
     * 更新客户状态
     */
    @Update("UPDATE customer SET status = #{status}, update_time = NOW() WHERE customer_id = #{customerId}")
    int updateStatus(@Param("customerId") Long customerId, @Param("status") String status);

    /**
     * 生成客户编号
     */
    @Select("SELECT CONCAT('C', TO_CHAR(NOW(), 'YYYYMMDD'), LPAD(COALESCE(MAX(CAST(SUBSTRING(customer_no, 9) AS INTEGER)), 0) + 1, 4, '0')) " +
            "FROM customer WHERE customer_no LIKE CONCAT('C', TO_CHAR(NOW(), 'YYYYMMDD'), '%')")
    String generateCustomerNo();
}
