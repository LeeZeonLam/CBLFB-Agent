package com.fba.logi.chat.controller;

import com.fba.logi.common.response.Response;
import com.fba.logi.domain.basedata.model.entity.DeliveryWarehouse;
import com.fba.logi.domain.basedata.model.entity.FbaWarehouse;
import com.fba.logi.domain.basedata.model.entity.ShippingChannel;
import com.fba.logi.domain.basedata.model.entity.ThirdPartyWarehouse;
import com.fba.logi.domain.basedata.service.IBaseDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基础数据控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/basedata")
@RequiredArgsConstructor
@Tag(name = "基础数据接口", description = "仓库和渠道基础数据管理接口")
public class BaseDataController {

    private final IBaseDataService baseDataService;

    // ==================== FBA 仓库 ====================

    /**
     * 根据代码查询 FBA 仓库
     */
    @GetMapping("/fba-warehouses/{warehouseCode}")
    @Operation(summary = "查询 FBA 仓库", description = "根据仓库代码查询 FBA 仓库详情")
    public Response<FbaWarehouse> getFbaWarehouseByCode(@PathVariable String warehouseCode) {
        FbaWarehouse warehouse = baseDataService.getFbaWarehouseByCode(warehouseCode);
        return Response.success(warehouse);
    }

    /**
     * 按国家查询 FBA 仓库
     */
    @GetMapping("/fba-warehouses/country/{country}")
    @Operation(summary = "按国家查询 FBA 仓库", description = "查询指定国家的 FBA 仓库列表")
    public Response<List<FbaWarehouse>> getFbaWarehousesByCountry(@PathVariable String country) {
        List<FbaWarehouse> warehouses = baseDataService.getFbaWarehousesByCountry(country);
        return Response.success(warehouses);
    }

    /**
     * 查询所有可用 FBA 仓库
     */
    @GetMapping("/fba-warehouses")
    @Operation(summary = "查询所有 FBA 仓库", description = "查询所有可用的 FBA 仓库")
    public Response<List<FbaWarehouse>> getAvailableFbaWarehouses() {
        List<FbaWarehouse> warehouses = baseDataService.getAvailableFbaWarehouses();
        return Response.success(warehouses);
    }

    /**
     * 查询 AWD 仓库
     */
    @GetMapping("/awd-warehouses")
    @Operation(summary = "查询 AWD 仓库", description = "查询 AWD 仓库列表")
    public Response<List<FbaWarehouse>> getAwdWarehouses(
            @RequestParam(required = false) String country) {
        List<FbaWarehouse> warehouses = baseDataService.getAwdWarehouses(country);
        return Response.success(warehouses);
    }

    // ==================== 第三方海外仓 ====================

    /**
     * 根据代码查询第三方仓
     */
    @GetMapping("/third-party-warehouses/{warehouseCode}")
    @Operation(summary = "查询第三方仓", description = "根据仓库代码查询第三方海外仓详情")
    public Response<ThirdPartyWarehouse> getThirdPartyWarehouseByCode(@PathVariable String warehouseCode) {
        ThirdPartyWarehouse warehouse = baseDataService.getThirdPartyWarehouseByCode(warehouseCode);
        return Response.success(warehouse);
    }

    /**
     * 按国家查询第三方仓
     */
    @GetMapping("/third-party-warehouses/country/{country}")
    @Operation(summary = "按国家查询第三方仓", description = "查询指定国家的第三方海外仓列表")
    public Response<List<ThirdPartyWarehouse>> getThirdPartyWarehousesByCountry(@PathVariable String country) {
        List<ThirdPartyWarehouse> warehouses = baseDataService.getThirdPartyWarehousesByCountry(country);
        return Response.success(warehouses);
    }

    /**
     * 查询所有可用第三方仓
     */
    @GetMapping("/third-party-warehouses")
    @Operation(summary = "查询所有第三方仓", description = "查询所有可用的第三方海外仓")
    public Response<List<ThirdPartyWarehouse>> getAvailableThirdPartyWarehouses() {
        List<ThirdPartyWarehouse> warehouses = baseDataService.getAvailableThirdPartyWarehouses();
        return Response.success(warehouses);
    }

    // ==================== 交货仓库 ====================

    /**
     * 根据代码查询交货仓库
     */
    @GetMapping("/delivery-warehouses/{warehouseCode}")
    @Operation(summary = "查询交货仓库", description = "根据仓库代码查询交货仓库详情")
    public Response<DeliveryWarehouse> getDeliveryWarehouseByCode(@PathVariable String warehouseCode) {
        DeliveryWarehouse warehouse = baseDataService.getDeliveryWarehouseByCode(warehouseCode);
        return Response.success(warehouse);
    }

    /**
     * 查询所有可用交货仓库
     */
    @GetMapping("/delivery-warehouses")
    @Operation(summary = "查询所有交货仓库", description = "查询所有可用的交货仓库")
    public Response<List<DeliveryWarehouse>> getAvailableDeliveryWarehouses() {
        List<DeliveryWarehouse> warehouses = baseDataService.getAvailableDeliveryWarehouses();
        return Response.success(warehouses);
    }

    /**
     * 按渠道查询交货仓库
     */
    @GetMapping("/delivery-warehouses/channel/{channelCode}")
    @Operation(summary = "按渠道查询交货仓库", description = "查询支持指定渠道的交货仓库")
    public Response<List<DeliveryWarehouse>> getDeliveryWarehousesByChannel(@PathVariable String channelCode) {
        List<DeliveryWarehouse> warehouses = baseDataService.getDeliveryWarehousesByChannel(channelCode);
        return Response.success(warehouses);
    }

    // ==================== 渠道 ====================

    /**
     * 根据代码查询渠道
     */
    @GetMapping("/channels/{channelCode}")
    @Operation(summary = "查询渠道", description = "根据渠道代码查询渠道详情")
    public Response<ShippingChannel> getChannelByCode(@PathVariable String channelCode) {
        ShippingChannel channel = baseDataService.getChannelByCode(channelCode);
        return Response.success(channel);
    }

    /**
     * 查询所有可用渠道
     */
    @GetMapping("/channels")
    @Operation(summary = "查询所有渠道", description = "查询所有可用的物流渠道")
    public Response<List<ShippingChannel>> getAvailableChannels() {
        List<ShippingChannel> channels = baseDataService.getAvailableChannels();
        return Response.success(channels);
    }

    /**
     * 按目的国查询渠道
     */
    @GetMapping("/channels/dest/{destCountry}")
    @Operation(summary = "按目的国查询渠道", description = "查询支持指定目的国的物流渠道")
    public Response<List<ShippingChannel>> getChannelsByDestCountry(@PathVariable String destCountry) {
        List<ShippingChannel> channels = baseDataService.getChannelsByDestCountry(destCountry);
        return Response.success(channels);
    }

    /**
     * 获取渠道显示名称
     */
    @GetMapping("/channels/{channelCode}/display-name")
    @Operation(summary = "获取渠道显示名称", description = "获取渠道的显示名称")
    public Response<String> getChannelDisplayName(@PathVariable String channelCode) {
        String displayName = baseDataService.getChannelDisplayName(channelCode);
        return Response.success(displayName);
    }
}
