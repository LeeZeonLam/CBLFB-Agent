package com.fba.logi.domain.customer.service;

import com.fba.logi.domain.customer.model.entity.Customer;
import com.fba.logi.domain.customer.model.entity.CustomerAddress;

import java.util.List;

/**
 * 客户服务接口
 */
public interface ICustomerService {

    // ==================== 客户管理 ====================

    /**
     * 根据 ID 查询客户
     */
    Customer getCustomerById(Long customerId);

    /**
     * 根据编号查询客户
     */
    Customer getCustomerByNo(String customerNo);

    /**
     * 查询所有客户
     */
    List<Customer> getAllCustomers();

    /**
     * 查询所有活跃客户
     */
    List<Customer> getActiveCustomers();

    /**
     * 创建客户
     *
     * @return 客户 ID
     */
    Long createCustomer(Customer customer);

    /**
     * 更新客户信息
     */
    void updateCustomer(Customer customer);

    /**
     * 激活客户
     */
    void activateCustomer(Long customerId);

    /**
     * 停用客户
     */
    void deactivateCustomer(Long customerId);

    // ==================== 地址管理 ====================

    /**
     * 查询客户的所有地址
     */
    List<CustomerAddress> getCustomerAddresses(Long customerId);

    /**
     * 查询客户指定类型的地址
     *
     * @param addressType COMMERCIAL/RESIDENTIAL
     */
    List<CustomerAddress> getCustomerAddressesByType(Long customerId, String addressType);

    /**
     * 查询客户的默认地址
     */
    CustomerAddress getDefaultAddress(Long customerId, String addressType);

    /**
     * 添加客户地址
     *
     * @return 地址 ID
     */
    Long addAddress(CustomerAddress address);

    /**
     * 更新客户地址
     */
    void updateAddress(CustomerAddress address);

    /**
     * 设置默认地址
     */
    void setDefaultAddress(Long customerId, Long addressId);

    /**
     * 删除地址
     */
    void deleteAddress(Long addressId);
}
