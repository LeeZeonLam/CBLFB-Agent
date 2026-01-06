package com.fba.logi.domain.basedata.service.impl;

import com.fba.logi.common.constants.Constants;
import com.fba.logi.common.exception.BusinessException;
import com.fba.logi.domain.basedata.model.entity.DeliveryWarehouse;
import com.fba.logi.domain.basedata.model.entity.FbaWarehouse;
import com.fba.logi.domain.basedata.model.entity.ShippingChannel;
import com.fba.logi.domain.basedata.model.entity.ThirdPartyWarehouse;
import com.fba.logi.domain.basedata.repository.IBaseDataRepository;
import com.fba.logi.domain.basedata.service.IBaseDataService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础数据服务实现
 */
public class BaseDataServiceImpl implements IBaseDataService {

    private final IBaseDataRepository baseDataRepository;

    public BaseDataServiceImpl(IBaseDataRepository baseDataRepository) {
        this.baseDataRepository = baseDataRepository;
    }

    // ==================== FBA 仓库 ====================

    @Override
    public FbaWarehouse getFbaWarehouseByCode(String warehouseCode) {
        FbaWarehouse warehouse = baseDataRepository.queryFbaWarehouseByCode(warehouseCode);
        if (warehouse == null) {
            throw new BusinessException("FBA 仓库不存在: " + warehouseCode);
        }
        return warehouse;
    }

    @Override
    public List<FbaWarehouse> getFbaWarehousesByCountry(String country) {
        return baseDataRepository.queryFbaWarehousesByCountry(country).stream()
                .filter(w -> Constants.WarehouseType.FBA.equals(w.getWarehouseType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FbaWarehouse> getAvailableFbaWarehouses() {
        return baseDataRepository.queryAvailableFbaWarehouses().stream()
                .filter(w -> Constants.WarehouseType.FBA.equals(w.getWarehouseType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FbaWarehouse> getAwdWarehouses(String country) {
        if (country == null || country.isEmpty()) {
            return baseDataRepository.queryFbaWarehousesByType(Constants.WarehouseType.AWD);
        }
        return baseDataRepository.queryFbaWarehousesByCountry(country).stream()
                .filter(w -> Constants.WarehouseType.AWD.equals(w.getWarehouseType()))
                .collect(Collectors.toList());
    }

    // ==================== 第三方海外仓 ====================

    @Override
    public ThirdPartyWarehouse getThirdPartyWarehouseByCode(String warehouseCode) {
        ThirdPartyWarehouse warehouse = baseDataRepository.queryThirdPartyWarehouseByCode(warehouseCode);
        if (warehouse == null) {
            throw new BusinessException("第三方仓不存在: " + warehouseCode);
        }
        return warehouse;
    }

    @Override
    public List<ThirdPartyWarehouse> getThirdPartyWarehousesByCountry(String country) {
        return baseDataRepository.queryThirdPartyWarehousesByCountry(country);
    }

    @Override
    public List<ThirdPartyWarehouse> getAvailableThirdPartyWarehouses() {
        return baseDataRepository.queryAvailableThirdPartyWarehouses();
    }

    // ==================== 交货仓库 ====================

    @Override
    public DeliveryWarehouse getDeliveryWarehouseByCode(String warehouseCode) {
        DeliveryWarehouse warehouse = baseDataRepository.queryDeliveryWarehouseByCode(warehouseCode);
        if (warehouse == null) {
            throw new BusinessException("交货仓库不存在: " + warehouseCode);
        }
        return warehouse;
    }

    @Override
    public List<DeliveryWarehouse> getAvailableDeliveryWarehouses() {
        return baseDataRepository.queryAvailableDeliveryWarehouses();
    }

    @Override
    public List<DeliveryWarehouse> getDeliveryWarehousesByChannel(String channelCode) {
        return baseDataRepository.queryDeliveryWarehousesByChannel(channelCode);
    }

    // ==================== 渠道 ====================

    @Override
    public ShippingChannel getChannelByCode(String channelCode) {
        ShippingChannel channel = baseDataRepository.queryChannelByCode(channelCode);
        if (channel == null) {
            throw new BusinessException("渠道不存在: " + channelCode);
        }
        return channel;
    }

    @Override
    public List<ShippingChannel> getAvailableChannels() {
        return baseDataRepository.queryAvailableChannels();
    }

    @Override
    public List<ShippingChannel> getChannelsByDestCountry(String destCountry) {
        // 根据目的国筛选支持的渠道
        return baseDataRepository.queryAvailableChannels().stream()
                .filter(channel -> {
                    List<String> ports = channel.getArrivalPorts();
                    if (ports == null || ports.isEmpty()) {
                        return false;
                    }
                    // 判断目的港是否在目的国
                    // 美国港口：洛杉矶(LAX)、长滩(LGB)、萨凡纳(SAV) 等
                    if ("US".equalsIgnoreCase(destCountry)) {
                        return ports.stream().anyMatch(port ->
                                port.contains("LAX") || port.contains("LGB") ||
                                        port.contains("SAV") || port.contains("NYC") ||
                                        port.contains("美国") || port.contains("洛杉矶") ||
                                        port.contains("长滩"));
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public String getChannelDisplayName(String channelCode) {
        ShippingChannel channel = baseDataRepository.queryChannelByCode(channelCode);
        if (channel == null) {
            return channelCode;
        }
        return channel.getChannelName();
    }
}
