package com.fba.logi.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.customer.model.entity.Customer;
import com.fba.logi.domain.customer.model.entity.CustomerAddress;
import com.fba.logi.domain.customer.repository.ICustomerRepository;
import com.fba.logi.infrastructure.persistent.dao.ICustomerAddressMapper;
import com.fba.logi.infrastructure.persistent.dao.ICustomerMapper;
import com.fba.logi.infrastructure.persistent.po.CustomerAddressPO;
import com.fba.logi.infrastructure.persistent.po.CustomerPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class CustomerRepository implements ICustomerRepository {

    private final ICustomerMapper customerMapper;
    private final ICustomerAddressMapper addressMapper;

    // ==================== 客户操作 ====================

    @Override
    public Customer queryById(Long customerId) {
        LambdaQueryWrapper<CustomerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerPO::getCustomerId, customerId);
        CustomerPO po = customerMapper.selectOne(wrapper);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public Customer queryByCustomerNo(String customerNo) {
        LambdaQueryWrapper<CustomerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerPO::getCustomerNo, customerNo);
        CustomerPO po = customerMapper.selectOne(wrapper);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<Customer> queryActiveCustomers() {
        LambdaQueryWrapper<CustomerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerPO::getStatus, Constants.CustomerStatus.ACTIVE);
        wrapper.orderByDesc(CustomerPO::getCreateTime);
        return customerMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> queryAll() {
        LambdaQueryWrapper<CustomerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CustomerPO::getCreateTime);
        return customerMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveCustomer(Customer customer) {
        CustomerPO po = convertToPO(customer);
        if (po.getId() == null) {
            if (po.getCustomerId() == null) {
                po.setCustomerId(System.currentTimeMillis());
            }
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            customerMapper.insert(po);
            customer.setCustomerId(po.getCustomerId());
        } else {
            po.setUpdateTime(LocalDateTime.now());
            customerMapper.updateById(po);
        }
    }

    @Override
    public void updateCustomerStatus(Long customerId, String status) {
        customerMapper.updateStatus(customerId, status);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        LambdaQueryWrapper<CustomerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerPO::getCustomerId, customerId);
        customerMapper.delete(wrapper);
    }

    @Override
    public String generateCustomerNo() {
        String no = customerMapper.generateCustomerNo();
        if (no == null || no.isEmpty()) {
            // 如果没有记录，生成第一个编号
            return "C" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "0001";
        }
        return no;
    }

    // ==================== 客户地址操作 ====================

    @Override
    public CustomerAddress queryAddressById(Long addressId) {
        LambdaQueryWrapper<CustomerAddressPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerAddressPO::getAddressId, addressId);
        CustomerAddressPO po = addressMapper.selectOne(wrapper);
        return po != null ? convertAddressToEntity(po) : null;
    }

    @Override
    public List<CustomerAddress> queryAddressesByCustomerId(Long customerId) {
        LambdaQueryWrapper<CustomerAddressPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerAddressPO::getCustomerId, customerId);
        wrapper.orderByDesc(CustomerAddressPO::getIsDefault);
        wrapper.orderByDesc(CustomerAddressPO::getCreateTime);
        return addressMapper.selectList(wrapper).stream()
                .map(this::convertAddressToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerAddress> queryAddressesByType(Long customerId, String addressType) {
        LambdaQueryWrapper<CustomerAddressPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerAddressPO::getCustomerId, customerId);
        wrapper.eq(CustomerAddressPO::getAddressType, addressType);
        wrapper.orderByDesc(CustomerAddressPO::getIsDefault);
        wrapper.orderByDesc(CustomerAddressPO::getCreateTime);
        return addressMapper.selectList(wrapper).stream()
                .map(this::convertAddressToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerAddress queryDefaultAddress(Long customerId, String addressType) {
        LambdaQueryWrapper<CustomerAddressPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerAddressPO::getCustomerId, customerId);
        wrapper.eq(CustomerAddressPO::getAddressType, addressType);
        wrapper.eq(CustomerAddressPO::getIsDefault, true);
        CustomerAddressPO po = addressMapper.selectOne(wrapper);
        return po != null ? convertAddressToEntity(po) : null;
    }

    @Override
    public void saveAddress(CustomerAddress address) {
        CustomerAddressPO po = convertAddressToPO(address);
        if (po.getId() == null) {
            if (po.getAddressId() == null) {
                po.setAddressId(System.currentTimeMillis());
            }
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            addressMapper.insert(po);
            address.setAddressId(po.getAddressId());
        } else {
            po.setUpdateTime(LocalDateTime.now());
            addressMapper.updateById(po);
        }
    }

    @Override
    public void setDefaultAddress(Long customerId, Long addressId) {
        // 先获取地址类型
        CustomerAddress address = queryAddressById(addressId);
        if (address != null) {
            // 取消该类型的其他默认地址
            addressMapper.clearDefaultAddress(customerId, address.getAddressType());
            // 设置新的默认地址
            addressMapper.setDefaultAddress(addressId);
        }
    }

    @Override
    public void deleteAddress(Long addressId) {
        LambdaQueryWrapper<CustomerAddressPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerAddressPO::getAddressId, addressId);
        addressMapper.delete(wrapper);
    }

    // ==================== 转换方法 ====================

    private Customer convertToEntity(CustomerPO po) {
        Customer customer = new Customer();
        customer.setCustomerId(po.getCustomerId());
        customer.setCustomerNo(po.getCustomerNo());
        customer.setCompanyName(po.getCompanyName());
        customer.setContactName(po.getContactName());
        customer.setContactPhone(po.getContactPhone());
        customer.setEmail(po.getEmail());
        customer.setStatus(po.getStatus());
        customer.setCreateTime(po.getCreateTime());
        customer.setUpdateTime(po.getUpdateTime());
        return customer;
    }

    private CustomerPO convertToPO(Customer entity) {
        Long id = null;
        if (entity.getCustomerId() != null) {
            LambdaQueryWrapper<CustomerPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerPO::getCustomerId, entity.getCustomerId());
            CustomerPO existing = customerMapper.selectOne(wrapper);
            if (existing != null) {
                id = existing.getId();
            }
        }

        return CustomerPO.builder()
                .id(id)
                .customerId(entity.getCustomerId())
                .customerNo(entity.getCustomerNo())
                .companyName(entity.getCompanyName())
                .contactName(entity.getContactName())
                .contactPhone(entity.getContactPhone())
                .email(entity.getEmail())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    private CustomerAddress convertAddressToEntity(CustomerAddressPO po) {
        CustomerAddress address = new CustomerAddress();
        address.setAddressId(po.getAddressId());
        address.setCustomerId(po.getCustomerId());
        address.setAddressType(po.getAddressType());
        address.setAddressName(po.getAddressName());
        address.setRecipientName(po.getRecipientName());
        address.setCompanyName(po.getCompanyName());
        address.setAddressLine1(po.getAddressLine1());
        address.setAddressLine2(po.getAddressLine2());
        address.setCity(po.getCity());
        address.setState(po.getState());
        address.setZipCode(po.getZipCode());
        address.setCountryCode(po.getCountryCode());
        address.setPhone(po.getPhone());
        address.setIsDefault(po.getIsDefault());
        address.setCreateTime(po.getCreateTime());
        address.setUpdateTime(po.getUpdateTime());
        return address;
    }

    private CustomerAddressPO convertAddressToPO(CustomerAddress entity) {
        Long id = null;
        if (entity.getAddressId() != null) {
            LambdaQueryWrapper<CustomerAddressPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerAddressPO::getAddressId, entity.getAddressId());
            CustomerAddressPO existing = addressMapper.selectOne(wrapper);
            if (existing != null) {
                id = existing.getId();
            }
        }

        return CustomerAddressPO.builder()
                .id(id)
                .addressId(entity.getAddressId())
                .customerId(entity.getCustomerId())
                .addressType(entity.getAddressType())
                .addressName(entity.getAddressName())
                .recipientName(entity.getRecipientName())
                .companyName(entity.getCompanyName())
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .city(entity.getCity())
                .state(entity.getState())
                .zipCode(entity.getZipCode())
                .countryCode(entity.getCountryCode())
                .phone(entity.getPhone())
                .isDefault(entity.getIsDefault())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
