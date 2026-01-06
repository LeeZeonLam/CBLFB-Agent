package com.fba.logi.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fba.logi.domain.basedata.model.entity.DeliveryWarehouse;
import com.fba.logi.domain.basedata.model.entity.FbaWarehouse;
import com.fba.logi.domain.basedata.model.entity.ShippingChannel;
import com.fba.logi.domain.basedata.model.entity.ThirdPartyWarehouse;
import com.fba.logi.domain.basedata.repository.IBaseDataRepository;
import com.fba.logi.infrastructure.persistent.dao.*;
import com.fba.logi.infrastructure.persistent.po.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础数据仓储实现
 */
@Repository
@RequiredArgsConstructor
public class BaseDataRepository implements IBaseDataRepository {

    private final IFbaWarehouseMapper fbaWarehouseMapper;
    private final IThirdPartyWarehouseMapper thirdPartyWarehouseMapper;
    private final IDeliveryWarehouseMapper deliveryWarehouseMapper;
    private final IShippingChannelMapper channelMapper;

    // ==================== FBA 仓库操作 ====================

    @Override
    public FbaWarehouse queryFbaWarehouseById(Long warehouseId) {
        LambdaQueryWrapper<FbaWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbaWarehousePO::getWarehouseId, warehouseId);
        FbaWarehousePO po = fbaWarehouseMapper.selectOne(wrapper);
        return po != null ? convertFbaToEntity(po) : null;
    }

    @Override
    public FbaWarehouse queryFbaWarehouseByCode(String warehouseCode) {
        LambdaQueryWrapper<FbaWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbaWarehousePO::getWarehouseCode, warehouseCode);
        FbaWarehousePO po = fbaWarehouseMapper.selectOne(wrapper);
        return po != null ? convertFbaToEntity(po) : null;
    }

    @Override
    public List<FbaWarehouse> queryFbaWarehousesByCountry(String country) {
        LambdaQueryWrapper<FbaWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbaWarehousePO::getCountry, country);
        wrapper.eq(FbaWarehousePO::getEnabled, true);
        return fbaWarehouseMapper.selectList(wrapper).stream()
                .map(this::convertFbaToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<FbaWarehouse> queryFbaWarehousesByType(String warehouseType) {
        LambdaQueryWrapper<FbaWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbaWarehousePO::getWarehouseType, warehouseType);
        wrapper.eq(FbaWarehousePO::getEnabled, true);
        return fbaWarehouseMapper.selectList(wrapper).stream()
                .map(this::convertFbaToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<FbaWarehouse> queryAvailableFbaWarehouses() {
        LambdaQueryWrapper<FbaWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbaWarehousePO::getEnabled, true);
        wrapper.eq(FbaWarehousePO::getAcceptingShipments, true);
        return fbaWarehouseMapper.selectList(wrapper).stream()
                .map(this::convertFbaToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveFbaWarehouse(FbaWarehouse warehouse) {
        FbaWarehousePO po = convertFbaToPO(warehouse);
        if (po.getId() == null) {
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            fbaWarehouseMapper.insert(po);
        } else {
            po.setUpdateTime(LocalDateTime.now());
            fbaWarehouseMapper.updateById(po);
        }
    }

    // ==================== 第三方海外仓操作 ====================

    @Override
    public ThirdPartyWarehouse queryThirdPartyWarehouseById(Long warehouseId) {
        LambdaQueryWrapper<ThirdPartyWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdPartyWarehousePO::getWarehouseId, warehouseId);
        ThirdPartyWarehousePO po = thirdPartyWarehouseMapper.selectOne(wrapper);
        return po != null ? convertThirdPartyToEntity(po) : null;
    }

    @Override
    public ThirdPartyWarehouse queryThirdPartyWarehouseByCode(String warehouseCode) {
        LambdaQueryWrapper<ThirdPartyWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdPartyWarehousePO::getWarehouseCode, warehouseCode);
        ThirdPartyWarehousePO po = thirdPartyWarehouseMapper.selectOne(wrapper);
        return po != null ? convertThirdPartyToEntity(po) : null;
    }

    @Override
    public List<ThirdPartyWarehouse> queryThirdPartyWarehousesByCountry(String country) {
        LambdaQueryWrapper<ThirdPartyWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdPartyWarehousePO::getCountry, country);
        wrapper.eq(ThirdPartyWarehousePO::getEnabled, true);
        return thirdPartyWarehouseMapper.selectList(wrapper).stream()
                .map(this::convertThirdPartyToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThirdPartyWarehouse> queryAvailableThirdPartyWarehouses() {
        LambdaQueryWrapper<ThirdPartyWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdPartyWarehousePO::getEnabled, true);
        return thirdPartyWarehouseMapper.selectList(wrapper).stream()
                .map(this::convertThirdPartyToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveThirdPartyWarehouse(ThirdPartyWarehouse warehouse) {
        ThirdPartyWarehousePO po = convertThirdPartyToPO(warehouse);
        if (po.getId() == null) {
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            thirdPartyWarehouseMapper.insert(po);
        } else {
            po.setUpdateTime(LocalDateTime.now());
            thirdPartyWarehouseMapper.updateById(po);
        }
    }

    // ==================== 交货仓库操作 ====================

    @Override
    public DeliveryWarehouse queryDeliveryWarehouseById(Long warehouseId) {
        LambdaQueryWrapper<DeliveryWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryWarehousePO::getWarehouseId, warehouseId);
        DeliveryWarehousePO po = deliveryWarehouseMapper.selectOne(wrapper);
        return po != null ? convertDeliveryToEntity(po) : null;
    }

    @Override
    public DeliveryWarehouse queryDeliveryWarehouseByCode(String warehouseCode) {
        LambdaQueryWrapper<DeliveryWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryWarehousePO::getWarehouseCode, warehouseCode);
        DeliveryWarehousePO po = deliveryWarehouseMapper.selectOne(wrapper);
        return po != null ? convertDeliveryToEntity(po) : null;
    }

    @Override
    public List<DeliveryWarehouse> queryAvailableDeliveryWarehouses() {
        LambdaQueryWrapper<DeliveryWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryWarehousePO::getEnabled, true);
        return deliveryWarehouseMapper.selectList(wrapper).stream()
                .map(this::convertDeliveryToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryWarehouse> queryDeliveryWarehousesByChannel(String channelCode) {
        LambdaQueryWrapper<DeliveryWarehousePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryWarehousePO::getEnabled, true);
        wrapper.like(DeliveryWarehousePO::getSupportedChannels, channelCode);
        return deliveryWarehouseMapper.selectList(wrapper).stream()
                .map(this::convertDeliveryToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveDeliveryWarehouse(DeliveryWarehouse warehouse) {
        DeliveryWarehousePO po = convertDeliveryToPO(warehouse);
        if (po.getId() == null) {
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            deliveryWarehouseMapper.insert(po);
        } else {
            po.setUpdateTime(LocalDateTime.now());
            deliveryWarehouseMapper.updateById(po);
        }
    }

    // ==================== 渠道操作 ====================

    @Override
    public ShippingChannel queryChannelById(Long channelId) {
        LambdaQueryWrapper<ShippingChannelPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingChannelPO::getChannelId, channelId);
        ShippingChannelPO po = channelMapper.selectOne(wrapper);
        return po != null ? convertChannelToEntity(po) : null;
    }

    @Override
    public ShippingChannel queryChannelByCode(String channelCode) {
        LambdaQueryWrapper<ShippingChannelPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingChannelPO::getChannelCode, channelCode);
        ShippingChannelPO po = channelMapper.selectOne(wrapper);
        return po != null ? convertChannelToEntity(po) : null;
    }

    @Override
    public List<ShippingChannel> queryAvailableChannels() {
        LambdaQueryWrapper<ShippingChannelPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingChannelPO::getEnabled, true);
        return channelMapper.selectList(wrapper).stream()
                .map(this::convertChannelToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShippingChannel> queryChannelsByArrivalPort(String arrivalPort) {
        LambdaQueryWrapper<ShippingChannelPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShippingChannelPO::getEnabled, true);
        wrapper.like(ShippingChannelPO::getArrivalPorts, arrivalPort);
        return channelMapper.selectList(wrapper).stream()
                .map(this::convertChannelToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveChannel(ShippingChannel channel) {
        ShippingChannelPO po = convertChannelToPO(channel);
        if (po.getId() == null) {
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            channelMapper.insert(po);
        } else {
            po.setUpdateTime(LocalDateTime.now());
            channelMapper.updateById(po);
        }
    }

    // ==================== 转换方法 ====================

    private FbaWarehouse convertFbaToEntity(FbaWarehousePO po) {
        FbaWarehouse entity = new FbaWarehouse();
        entity.setWarehouseId(po.getWarehouseId());
        entity.setWarehouseCode(po.getWarehouseCode());
        entity.setWarehouseName(po.getWarehouseName());
        entity.setWarehouseType(po.getWarehouseType());
        entity.setCountry(po.getCountry());
        entity.setState(po.getState());
        entity.setCity(po.getCity());
        entity.setAddressLine1(po.getAddressLine1());
        entity.setAddressLine2(po.getAddressLine2());
        entity.setZipCode(po.getZipCode());
        entity.setLatitude(po.getLatitude());
        entity.setLongitude(po.getLongitude());
        entity.setAcceptingShipments(po.getAcceptingShipments());
        entity.setEnabled(po.getEnabled());
        return entity;
    }

    private FbaWarehousePO convertFbaToPO(FbaWarehouse entity) {
        return FbaWarehousePO.builder()
                .warehouseId(entity.getWarehouseId())
                .warehouseCode(entity.getWarehouseCode())
                .warehouseName(entity.getWarehouseName())
                .warehouseType(entity.getWarehouseType())
                .country(entity.getCountry())
                .state(entity.getState())
                .city(entity.getCity())
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .zipCode(entity.getZipCode())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .acceptingShipments(entity.getAcceptingShipments())
                .enabled(entity.getEnabled())
                .build();
    }

    private ThirdPartyWarehouse convertThirdPartyToEntity(ThirdPartyWarehousePO po) {
        ThirdPartyWarehouse entity = new ThirdPartyWarehouse();
        entity.setWarehouseId(po.getWarehouseId());
        entity.setWarehouseCode(po.getWarehouseCode());
        entity.setWarehouseName(po.getWarehouseName());
        entity.setProviderName(po.getProviderName());
        entity.setCountry(po.getCountry());
        entity.setState(po.getState());
        entity.setCity(po.getCity());
        entity.setAddress(po.getAddress());
        entity.setContactName(po.getContactName());
        entity.setContactPhone(po.getContactPhone());
        if (po.getServiceTypes() != null) {
            entity.setServiceTypes(Arrays.asList(po.getServiceTypes().split(",")));
        }
        entity.setEnabled(po.getEnabled());
        return entity;
    }

    private ThirdPartyWarehousePO convertThirdPartyToPO(ThirdPartyWarehouse entity) {
        return ThirdPartyWarehousePO.builder()
                .warehouseId(entity.getWarehouseId())
                .warehouseCode(entity.getWarehouseCode())
                .warehouseName(entity.getWarehouseName())
                .providerName(entity.getProviderName())
                .country(entity.getCountry())
                .state(entity.getState())
                .city(entity.getCity())
                .address(entity.getAddress())
                .contactName(entity.getContactName())
                .contactPhone(entity.getContactPhone())
                .serviceTypes(entity.getServiceTypes() != null ? String.join(",", entity.getServiceTypes()) : null)
                .enabled(entity.getEnabled())
                .build();
    }

    private DeliveryWarehouse convertDeliveryToEntity(DeliveryWarehousePO po) {
        DeliveryWarehouse entity = new DeliveryWarehouse();
        entity.setWarehouseId(po.getWarehouseId());
        entity.setWarehouseCode(po.getWarehouseCode());
        entity.setWarehouseName(po.getWarehouseName());
        entity.setProvince(po.getProvince());
        entity.setCity(po.getCity());
        entity.setDistrict(po.getDistrict());
        entity.setAddress(po.getAddress());
        entity.setContactName(po.getContactName());
        entity.setContactPhone(po.getContactPhone());
        entity.setWorkingHours(po.getWorkingHours());
        if (po.getSupportedChannels() != null) {
            entity.setSupportedChannels(Arrays.asList(po.getSupportedChannels().split(",")));
        }
        entity.setEnabled(po.getEnabled());
        return entity;
    }

    private DeliveryWarehousePO convertDeliveryToPO(DeliveryWarehouse entity) {
        return DeliveryWarehousePO.builder()
                .warehouseId(entity.getWarehouseId())
                .warehouseCode(entity.getWarehouseCode())
                .warehouseName(entity.getWarehouseName())
                .province(entity.getProvince())
                .city(entity.getCity())
                .district(entity.getDistrict())
                .address(entity.getAddress())
                .contactName(entity.getContactName())
                .contactPhone(entity.getContactPhone())
                .workingHours(entity.getWorkingHours())
                .supportedChannels(entity.getSupportedChannels() != null ? String.join(",", entity.getSupportedChannels()) : null)
                .enabled(entity.getEnabled())
                .build();
    }

    private ShippingChannel convertChannelToEntity(ShippingChannelPO po) {
        ShippingChannel entity = new ShippingChannel();
        entity.setChannelId(po.getChannelId());
        entity.setChannelCode(po.getChannelCode());
        entity.setChannelName(po.getChannelName());
        entity.setCarrier(po.getCarrier());
        entity.setTransportType(po.getTransportType());
        entity.setDeparturePort(po.getDeparturePort());
        if (po.getArrivalPorts() != null) {
            entity.setArrivalPorts(Arrays.asList(po.getArrivalPorts().split(",")));
        }
        entity.setTransitDays(po.getTransitDays());
        entity.setPricePerCbm(po.getPricePerCbm());
        entity.setPricePerKg(po.getPricePerKg());
        entity.setMinWeight(po.getMinWeight());
        entity.setMaxWeight(po.getMaxWeight());
        if (po.getSupportedContainerTypes() != null) {
            entity.setSupportedContainerTypes(Arrays.asList(po.getSupportedContainerTypes().split(",")));
        }
        entity.setEnabled(po.getEnabled());
        return entity;
    }

    private ShippingChannelPO convertChannelToPO(ShippingChannel entity) {
        return ShippingChannelPO.builder()
                .channelId(entity.getChannelId())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .carrier(entity.getCarrier())
                .transportType(entity.getTransportType())
                .departurePort(entity.getDeparturePort())
                .arrivalPorts(entity.getArrivalPorts() != null ? String.join(",", entity.getArrivalPorts()) : null)
                .transitDays(entity.getTransitDays())
                .pricePerCbm(entity.getPricePerCbm())
                .pricePerKg(entity.getPricePerKg())
                .minWeight(entity.getMinWeight())
                .maxWeight(entity.getMaxWeight())
                .supportedContainerTypes(entity.getSupportedContainerTypes() != null ? String.join(",", entity.getSupportedContainerTypes()) : null)
                .enabled(entity.getEnabled())
                .build();
    }
}
