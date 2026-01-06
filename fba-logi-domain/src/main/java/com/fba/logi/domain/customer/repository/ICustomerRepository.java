package com.fba.logi.domain.customer.repository;

import com.fba.logi.domain.customer.model.entity.Customer;
import com.fba.logi.domain.customer.model.entity.CustomerAddress;

import java.util.List;

/**
 * 客户仓储接口
 */
public interface ICustomerRepository {

    // ==================== 客户操作 ====================

    /**
     * 根据 ID 查询客户
     */
    Customer queryById(Long customerId);

    /**
     * 根据编号查询客户
     */
    Customer queryByCustomerNo(String customerNo);

    /**
     * 查询所有活跃客户
     */
    List<Customer> queryActiveCustomers();

    /**
     * 查询所有客户
     */
    List<Customer> queryAll();

    /**
     * 保存客户
     */
    void saveCustomer(Customer customer);

    /**
     * 更新客户状态
     */
    void updateCustomerStatus(Long customerId, String status);

    /**
     * 删除客户
     */
    void deleteCustomer(Long customerId);

    /**
     * 生成客户编号
     */
    String generateCustomerNo();

    // ==================== 客户地址操作 ====================

    /**
     * 根据 ID 查询地址
     */
    CustomerAddress queryAddressById(Long addressId);

    /**
     * 查询客户的所有地址
     */
    List<CustomerAddress> queryAddressesByCustomerId(Long customerId);

    /**
     * 查询客户指定类型的地址
     */
    List<CustomerAddress> queryAddressesByType(Long customerId, String addressType);

    /**
     * 查询客户的默认地址
     */
    CustomerAddress queryDefaultAddress(Long customerId, String addressType);

    /**
     * 保存地址
     */
    void saveAddress(CustomerAddress address);

    /**
     * 设置默认地址（同时取消其他默认）
     */
    void setDefaultAddress(Long customerId, Long addressId);

    /**
     * 删除地址
     */
    void deleteAddress(Long addressId);
}
