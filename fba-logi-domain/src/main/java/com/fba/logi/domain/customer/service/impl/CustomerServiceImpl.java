package com.fba.logi.domain.customer.service.impl;

import com.fba.logi.common.constants.Constants;
import com.fba.logi.common.exception.BusinessException;
import com.fba.logi.domain.customer.model.entity.Customer;
import com.fba.logi.domain.customer.model.entity.CustomerAddress;
import com.fba.logi.domain.customer.repository.ICustomerRepository;
import com.fba.logi.domain.customer.service.ICustomerService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户服务实现
 */
public class CustomerServiceImpl implements ICustomerService {

    private final ICustomerRepository customerRepository;

    public CustomerServiceImpl(ICustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // ==================== 客户管理 ====================

    @Override
    public Customer getCustomerById(Long customerId) {
        Customer customer = customerRepository.queryById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在: " + customerId);
        }
        return customer;
    }

    @Override
    public Customer getCustomerByNo(String customerNo) {
        Customer customer = customerRepository.queryByCustomerNo(customerNo);
        if (customer == null) {
            throw new BusinessException("客户不存在: " + customerNo);
        }
        return customer;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.queryAll();
    }

    @Override
    public List<Customer> getActiveCustomers() {
        return customerRepository.queryActiveCustomers();
    }

    @Override
    public Long createCustomer(Customer customer) {
        // 生成客户编号
        customer.setCustomerNo(customerRepository.generateCustomerNo());
        customer.setStatus(Constants.CustomerStatus.ACTIVE);
        customer.setCreateTime(LocalDateTime.now());
        customer.setUpdateTime(LocalDateTime.now());

        customerRepository.saveCustomer(customer);
        return customer.getCustomerId();
    }

    @Override
    public void updateCustomer(Customer customer) {
        Customer existing = customerRepository.queryById(customer.getCustomerId());
        if (existing == null) {
            throw new BusinessException("客户不存在: " + customer.getCustomerId());
        }

        customer.setUpdateTime(LocalDateTime.now());
        customerRepository.saveCustomer(customer);
    }

    @Override
    public void activateCustomer(Long customerId) {
        Customer customer = customerRepository.queryById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在: " + customerId);
        }

        customerRepository.updateCustomerStatus(customerId, Constants.CustomerStatus.ACTIVE);
    }

    @Override
    public void deactivateCustomer(Long customerId) {
        Customer customer = customerRepository.queryById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在: " + customerId);
        }

        customerRepository.updateCustomerStatus(customerId, Constants.CustomerStatus.INACTIVE);
    }

    // ==================== 地址管理 ====================

    @Override
    public List<CustomerAddress> getCustomerAddresses(Long customerId) {
        // 验证客户存在
        Customer customer = customerRepository.queryById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在: " + customerId);
        }

        return customerRepository.queryAddressesByCustomerId(customerId);
    }

    @Override
    public List<CustomerAddress> getCustomerAddressesByType(Long customerId, String addressType) {
        // 验证客户存在
        Customer customer = customerRepository.queryById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在: " + customerId);
        }

        return customerRepository.queryAddressesByType(customerId, addressType);
    }

    @Override
    public CustomerAddress getDefaultAddress(Long customerId, String addressType) {
        // 验证客户存在
        Customer customer = customerRepository.queryById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在: " + customerId);
        }

        return customerRepository.queryDefaultAddress(customerId, addressType);
    }

    @Override
    public Long addAddress(CustomerAddress address) {
        // 验证客户存在
        Customer customer = customerRepository.queryById(address.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户不存在: " + address.getCustomerId());
        }

        // 验证地址类型
        if (!Constants.AddressType.COMMERCIAL.equals(address.getAddressType()) &&
                !Constants.AddressType.RESIDENTIAL.equals(address.getAddressType())) {
            throw new BusinessException("无效的地址类型: " + address.getAddressType());
        }

        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        // 如果是第一个地址，设为默认
        List<CustomerAddress> existingAddresses = customerRepository
                .queryAddressesByType(address.getCustomerId(), address.getAddressType());
        if (existingAddresses.isEmpty()) {
            address.setIsDefault(true);
        }

        customerRepository.saveAddress(address);
        return address.getAddressId();
    }

    @Override
    public void updateAddress(CustomerAddress address) {
        CustomerAddress existing = customerRepository.queryAddressById(address.getAddressId());
        if (existing == null) {
            throw new BusinessException("地址不存在: " + address.getAddressId());
        }

        // 确保客户 ID 和地址类型不变
        address.setCustomerId(existing.getCustomerId());
        address.setAddressType(existing.getAddressType());
        address.setUpdateTime(LocalDateTime.now());

        customerRepository.saveAddress(address);
    }

    @Override
    public void setDefaultAddress(Long customerId, Long addressId) {
        // 验证客户存在
        Customer customer = customerRepository.queryById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在: " + customerId);
        }

        // 验证地址存在且属于该客户
        CustomerAddress address = customerRepository.queryAddressById(addressId);
        if (address == null) {
            throw new BusinessException("地址不存在: " + addressId);
        }
        if (!customerId.equals(address.getCustomerId())) {
            throw new BusinessException("地址不属于该客户");
        }

        // 设置默认地址（同时取消其他默认）
        customerRepository.setDefaultAddress(customerId, addressId);
    }

    @Override
    public void deleteAddress(Long addressId) {
        CustomerAddress address = customerRepository.queryAddressById(addressId);
        if (address == null) {
            throw new BusinessException("地址不存在: " + addressId);
        }

        // 如果删除的是默认地址，需要将下一个地址设为默认
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            List<CustomerAddress> otherAddresses = customerRepository
                    .queryAddressesByType(address.getCustomerId(), address.getAddressType());

            // 删除当前地址
            customerRepository.deleteAddress(addressId);

            // 找到其他地址并设为默认
            for (CustomerAddress other : otherAddresses) {
                if (!addressId.equals(other.getAddressId())) {
                    customerRepository.setDefaultAddress(address.getCustomerId(), other.getAddressId());
                    break;
                }
            }
        } else {
            customerRepository.deleteAddress(addressId);
        }
    }
}
