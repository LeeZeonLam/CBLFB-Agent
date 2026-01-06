package com.fba.logi.domain.basedata.service;

import com.fba.logi.domain.basedata.model.entity.DeliveryWarehouse;
import com.fba.logi.domain.basedata.model.entity.FbaWarehouse;
import com.fba.logi.domain.basedata.model.entity.ShippingChannel;
import com.fba.logi.domain.basedata.model.entity.ThirdPartyWarehouse;

import java.util.List;

/**
 * 基础数据服务接口
 */
public interface IBaseDataService {

    // ==================== FBA 仓库 ====================

    /**
     * 根据代码查询 FBA 仓库
     */
    FbaWarehouse getFbaWarehouseByCode(String warehouseCode);

    /**
     * 查询指定国家的 FBA 仓库
     */
    List<FbaWarehouse> getFbaWarehousesByCountry(String country);

    /**
     * 查询所有可用的 FBA 仓库
     */
    List<FbaWarehouse> getAvailableFbaWarehouses();

    /**
     * 查询 AWD 仓库
     */
    List<FbaWarehouse> getAwdWarehouses(String country);

    // ==================== 第三方海外仓 ====================

    /**
     * 根据代码查询第三方仓
     */
    ThirdPartyWarehouse getThirdPartyWarehouseByCode(String warehouseCode);

    /**
     * 查询指定国家的第三方仓
     */
    List<ThirdPartyWarehouse> getThirdPartyWarehousesByCountry(String country);

    /**
     * 查询所有可用的第三方仓
     */
    List<ThirdPartyWarehouse> getAvailableThirdPartyWarehouses();

    // ==================== 交货仓库 ====================

    /**
     * 根据代码查询交货仓库
     */
    DeliveryWarehouse getDeliveryWarehouseByCode(String warehouseCode);

    /**
     * 查询所有可用的交货仓库
     */
    List<DeliveryWarehouse> getAvailableDeliveryWarehouses();

    /**
     * 查询支持指定渠道的交货仓库
     */
    List<DeliveryWarehouse> getDeliveryWarehousesByChannel(String channelCode);

    // ==================== 渠道 ====================

    /**
     * 根据代码查询渠道
     */
    ShippingChannel getChannelByCode(String channelCode);

    /**
     * 查询所有可用渠道
     */
    List<ShippingChannel> getAvailableChannels();

    /**
     * 查询支持指定目的国的渠道
     */
    List<ShippingChannel> getChannelsByDestCountry(String destCountry);

    /**
     * 获取渠道显示名称
     */
    String getChannelDisplayName(String channelCode);
}
