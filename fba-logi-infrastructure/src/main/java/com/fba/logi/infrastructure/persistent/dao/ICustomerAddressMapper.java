package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.CustomerAddressPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 客户地址 Mapper
 */
@Mapper
public interface ICustomerAddressMapper extends BaseMapper<CustomerAddressPO> {

    /**
     * 取消客户指定类型的所有默认地址
     */
    @Update("UPDATE customer_address SET is_default = FALSE, update_time = NOW() " +
            "WHERE customer_id = #{customerId} AND address_type = #{addressType} AND is_default = TRUE")
    int clearDefaultAddress(@Param("customerId") Long customerId, @Param("addressType") String addressType);

    /**
     * 设置默认地址
     */
    @Update("UPDATE customer_address SET is_default = TRUE, update_time = NOW() WHERE address_id = #{addressId}")
    int setDefaultAddress(@Param("addressId") Long addressId);
}
