package com.fba.logi.domain.basedata.repository;

import com.fba.logi.domain.basedata.model.entity.DeliveryWarehouse;
import com.fba.logi.domain.basedata.model.entity.FbaWarehouse;
import com.fba.logi.domain.basedata.model.entity.ShippingChannel;
import com.fba.logi.domain.basedata.model.entity.ThirdPartyWarehouse;

import java.util.List;

/**
 * 基础数据仓储接口
 */
public interface IBaseDataRepository {

    // ==================== FBA 仓库操作 ====================

    /**
     * 根据 ID 查询 FBA 仓库
     */
    FbaWarehouse queryFbaWarehouseById(Long warehouseId);

    /**
     * 根据代码查询 FBA 仓库
     */
    FbaWarehouse queryFbaWarehouseByCode(String warehouseCode);

    /**
     * 查询指定国家的 FBA 仓库
     */
    List<FbaWarehouse> queryFbaWarehousesByCountry(String country);

    /**
     * 查询指定类型的仓库（FBA/AWD）
     */
    List<FbaWarehouse> queryFbaWarehousesByType(String warehouseType);

    /**
     * 查询所有可用的 FBA 仓库
     */
    List<FbaWarehouse> queryAvailableFbaWarehouses();

    /**
     * 保存 FBA 仓库
     */
    void saveFbaWarehouse(FbaWarehouse warehouse);

    // ==================== 第三方海外仓操作 ====================

    /**
     * 根据 ID 查询第三方仓
     */
    ThirdPartyWarehouse queryThirdPartyWarehouseById(Long warehouseId);

    /**
     * 根据代码查询第三方仓
     */
    ThirdPartyWarehouse queryThirdPartyWarehouseByCode(String warehouseCode);

    /**
     * 查询指定国家的第三方仓
     */
    List<ThirdPartyWarehouse> queryThirdPartyWarehousesByCountry(String country);

    /**
     * 查询所有可用的第三方仓
     */
    List<ThirdPartyWarehouse> queryAvailableThirdPartyWarehouses();

    /**
     * 保存第三方仓
     */
    void saveThirdPartyWarehouse(ThirdPartyWarehouse warehouse);

    // ==================== 交货仓库操作 ====================

    /**
     * 根据 ID 查询交货仓库
     */
    DeliveryWarehouse queryDeliveryWarehouseById(Long warehouseId);

    /**
     * 根据代码查询交货仓库
     */
    DeliveryWarehouse queryDeliveryWarehouseByCode(String warehouseCode);

    /**
     * 查询所有可用的交货仓库
     */
    List<DeliveryWarehouse> queryAvailableDeliveryWarehouses();

    /**
     * 查询支持指定渠道的交货仓库
     */
    List<DeliveryWarehouse> queryDeliveryWarehousesByChannel(String channelCode);

    /**
     * 保存交货仓库
     */
    void saveDeliveryWarehouse(DeliveryWarehouse warehouse);

    // ==================== 渠道操作 ====================

    /**
     * 根据 ID 查询渠道
     */
    ShippingChannel queryChannelById(Long channelId);

    /**
     * 根据代码查询渠道
     */
    ShippingChannel queryChannelByCode(String channelCode);

    /**
     * 查询所有可用渠道
     */
    List<ShippingChannel> queryAvailableChannels();

    /**
     * 查询支持指定港口的渠道
     */
    List<ShippingChannel> queryChannelsByArrivalPort(String arrivalPort);

    /**
     * 保存渠道
     */
    void saveChannel(ShippingChannel channel);
}
