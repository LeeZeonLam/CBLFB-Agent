package com.fba.logi.chat.controller;

import com.fba.logi.common.response.Response;
import com.fba.logi.domain.customer.model.entity.Customer;
import com.fba.logi.domain.customer.model.entity.CustomerAddress;
import com.fba.logi.domain.customer.service.ICustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "客户接口", description = "客户和地址管理接口")
public class CustomerController {

    private final ICustomerService customerService;

    // ==================== 客户管理 ====================

    /**
     * 创建客户
     */
    @PostMapping
    @Operation(summary = "创建客户", description = "创建新客户")
    public Response<Long> createCustomer(@RequestBody Customer customer) {
        Long customerId = customerService.createCustomer(customer);
        return Response.success(customerId);
    }

    /**
     * 查询客户详情
     */
    @GetMapping("/{customerId}")
    @Operation(summary = "查询客户详情", description = "根据客户 ID 查询客户详情")
    public Response<Customer> getCustomerById(@PathVariable Long customerId) {
        Customer customer = customerService.getCustomerById(customerId);
        return Response.success(customer);
    }

    /**
     * 根据客户编号查询
     */
    @GetMapping("/no/{customerNo}")
    @Operation(summary = "根据编号查询", description = "根据客户编号查询客户详情")
    public Response<Customer> getCustomerByNo(@PathVariable String customerNo) {
        Customer customer = customerService.getCustomerByNo(customerNo);
        return Response.success(customer);
    }

    /**
     * 查询所有客户
     */
    @GetMapping
    @Operation(summary = "查询所有客户", description = "查询所有客户列表")
    public Response<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return Response.success(customers);
    }

    /**
     * 查询活跃客户
     */
    @GetMapping("/active")
    @Operation(summary = "查询活跃客户", description = "查询所有活跃状态的客户")
    public Response<List<Customer>> getActiveCustomers() {
        List<Customer> customers = customerService.getActiveCustomers();
        return Response.success(customers);
    }

    /**
     * 更新客户信息
     */
    @PutMapping("/{customerId}")
    @Operation(summary = "更新客户", description = "更新客户信息")
    public Response<Void> updateCustomer(@PathVariable Long customerId, @RequestBody Customer customer) {
        customer.setCustomerId(customerId);
        customerService.updateCustomer(customer);
        return Response.success();
    }

    /**
     * 激活客户
     */
    @PostMapping("/{customerId}/activate")
    @Operation(summary = "激活客户", description = "激活客户状态")
    public Response<Void> activateCustomer(@PathVariable Long customerId) {
        customerService.activateCustomer(customerId);
        return Response.success();
    }

    /**
     * 停用客户
     */
    @PostMapping("/{customerId}/deactivate")
    @Operation(summary = "停用客户", description = "停用客户状态")
    public Response<Void> deactivateCustomer(@PathVariable Long customerId) {
        customerService.deactivateCustomer(customerId);
        return Response.success();
    }

    // ==================== 地址管理 ====================

    /**
     * 添加客户地址
     */
    @PostMapping("/{customerId}/addresses")
    @Operation(summary = "添加地址", description = "为客户添加新地址")
    public Response<Long> addAddress(@PathVariable Long customerId, @RequestBody CustomerAddress address) {
        address.setCustomerId(customerId);
        Long addressId = customerService.addAddress(address);
        return Response.success(addressId);
    }

    /**
     * 查询客户地址
     */
    @GetMapping("/{customerId}/addresses")
    @Operation(summary = "查询客户地址", description = "查询客户的所有地址")
    public Response<List<CustomerAddress>> getCustomerAddresses(@PathVariable Long customerId) {
        List<CustomerAddress> addresses = customerService.getCustomerAddresses(customerId);
        return Response.success(addresses);
    }

    /**
     * 按类型查询地址
     */
    @GetMapping("/{customerId}/addresses/type/{addressType}")
    @Operation(summary = "按类型查询地址", description = "查询客户指定类型的地址")
    public Response<List<CustomerAddress>> getAddressesByType(
            @PathVariable Long customerId,
            @PathVariable String addressType) {
        List<CustomerAddress> addresses = customerService.getCustomerAddressesByType(customerId, addressType);
        return Response.success(addresses);
    }

    /**
     * 查询默认地址
     */
    @GetMapping("/{customerId}/addresses/default/{addressType}")
    @Operation(summary = "查询默认地址", description = "查询客户指定类型的默认地址")
    public Response<CustomerAddress> getDefaultAddress(
            @PathVariable Long customerId,
            @PathVariable String addressType) {
        CustomerAddress address = customerService.getDefaultAddress(customerId, addressType);
        return Response.success(address);
    }

    /**
     * 更新地址
     */
    @PutMapping("/{customerId}/addresses/{addressId}")
    @Operation(summary = "更新地址", description = "更新客户地址信息")
    public Response<Void> updateAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId,
            @RequestBody CustomerAddress address) {
        address.setAddressId(addressId);
        address.setCustomerId(customerId);
        customerService.updateAddress(address);
        return Response.success();
    }

    /**
     * 设置默认地址
     */
    @PostMapping("/{customerId}/addresses/{addressId}/set-default")
    @Operation(summary = "设置默认地址", description = "将地址设为默认")
    public Response<Void> setDefaultAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId) {
        customerService.setDefaultAddress(customerId, addressId);
        return Response.success();
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{customerId}/addresses/{addressId}")
    @Operation(summary = "删除地址", description = "删除客户地址")
    public Response<Void> deleteAddress(@PathVariable Long addressId) {
        customerService.deleteAddress(addressId);
        return Response.success();
    }
}
