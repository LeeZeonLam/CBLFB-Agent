package com.fba.logi.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fba.logi.domain.warehouse.model.entity.Carton;
import com.fba.logi.domain.warehouse.model.entity.Pallet;
import com.fba.logi.domain.warehouse.model.entity.WarehouseLocation;
import com.fba.logi.domain.warehouse.repository.IWarehouseRepository;
import com.fba.logi.infrastructure.persistent.dao.ICartonMapper;
import com.fba.logi.infrastructure.persistent.dao.IPalletMapper;
import com.fba.logi.infrastructure.persistent.dao.IWarehouseLocationMapper;
import com.fba.logi.infrastructure.persistent.po.CartonPO;
import com.fba.logi.infrastructure.persistent.po.PalletPO;
import com.fba.logi.infrastructure.persistent.po.WarehouseLocationPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 仓储仓储实现
 */
@Repository
@RequiredArgsConstructor
public class WarehouseRepository implements IWarehouseRepository {

    private final IPalletMapper palletMapper;
    private final ICartonMapper cartonMapper;
    private final IWarehouseLocationMapper locationMapper;

    private static final AtomicLong palletSequence = new AtomicLong(1);
    private static final AtomicLong cartonSequence = new AtomicLong(1);

    // ==================== 托盘操作 ====================

    @Override
    public Pallet queryPalletById(Long palletId) {
        LambdaQueryWrapper<PalletPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PalletPO::getPalletId, palletId.toString());
        PalletPO po = palletMapper.selectOne(wrapper);
        return po != null ? convertToPalletEntity(po) : null;
    }

    @Override
    public Pallet queryPalletByNo(String palletNo) {
        LambdaQueryWrapper<PalletPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PalletPO::getPalletId, palletNo);
        PalletPO po = palletMapper.selectOne(wrapper);
        return po != null ? convertToPalletEntity(po) : null;
    }

    @Override
    public List<Pallet> queryPalletsByOrderId(Long orderId) {
        List<PalletPO> poList = palletMapper.selectByOrderId(orderId.toString());
        return poList.stream()
                .map(this::convertToPalletEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void savePallet(Pallet pallet) {
        PalletPO po = convertToPalletPO(pallet);
        if (po.getId() == null) {
            palletMapper.insert(po);
        } else {
            palletMapper.updateById(po);
        }
    }

    @Override
    public void updatePalletStatus(Long palletId, String status) {
        palletMapper.updateState(palletId.toString(), status);
    }

    @Override
    public void deletePallet(Long palletId) {
        LambdaQueryWrapper<PalletPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PalletPO::getPalletId, palletId.toString());
        palletMapper.delete(wrapper);
    }

    @Override
    public String generatePalletNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("PLT%s%06d", dateStr, palletSequence.getAndIncrement());
    }

    // ==================== 纸箱操作 ====================

    @Override
    public Carton queryCartonById(Long cartonId) {
        LambdaQueryWrapper<CartonPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartonPO::getCartonId, cartonId);
        CartonPO po = cartonMapper.selectOne(wrapper);
        return po != null ? convertToCartonEntity(po) : null;
    }

    @Override
    public Carton queryCartonByNo(String cartonNo) {
        CartonPO po = cartonMapper.selectByCartonNo(cartonNo);
        return po != null ? convertToCartonEntity(po) : null;
    }

    @Override
    public List<Carton> queryCartonsByOrderId(Long orderId) {
        List<CartonPO> poList = cartonMapper.selectByOrderId(orderId);
        return poList.stream()
                .map(this::convertToCartonEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Carton> queryCartonsByPalletId(Long palletId) {
        List<CartonPO> poList = cartonMapper.selectByPalletId(palletId);
        return poList.stream()
                .map(this::convertToCartonEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveCarton(Carton carton) {
        CartonPO po = convertToCartonPO(carton);
        if (po.getId() == null) {
            cartonMapper.insert(po);
        } else {
            cartonMapper.updateById(po);
        }
    }

    @Override
    public void saveCartons(List<Carton> cartons) {
        for (Carton carton : cartons) {
            saveCarton(carton);
        }
    }

    @Override
    public void updateCartonStatus(Long cartonId, String status) {
        cartonMapper.updateStatus(cartonId, status);
    }

    @Override
    public void assignCartonToPallet(Long cartonId, Long palletId) {
        cartonMapper.assignToPallet(cartonId, palletId);
    }

    @Override
    public String generateCartonNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("CTN%s%06d", dateStr, cartonSequence.getAndIncrement());
    }

    // ==================== 库位操作 ====================

    @Override
    public WarehouseLocation queryLocationById(Long locationId) {
        LambdaQueryWrapper<WarehouseLocationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarehouseLocationPO::getLocationId, locationId);
        WarehouseLocationPO po = locationMapper.selectOne(wrapper);
        return po != null ? convertToLocationEntity(po) : null;
    }

    @Override
    public WarehouseLocation queryLocationByCode(String locationCode) {
        WarehouseLocationPO po = locationMapper.selectByLocationCode(locationCode);
        return po != null ? convertToLocationEntity(po) : null;
    }

    @Override
    public List<WarehouseLocation> queryAvailableLocations(String warehouseCode, String locationType) {
        List<WarehouseLocationPO> poList = locationMapper.selectAvailableLocations(warehouseCode, locationType);
        return poList.stream()
                .map(this::convertToLocationEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveLocation(WarehouseLocation location) {
        WarehouseLocationPO po = convertToLocationPO(location);
        if (po.getId() == null) {
            locationMapper.insert(po);
        } else {
            locationMapper.updateById(po);
        }
    }

    @Override
    public void updateLocationStatus(Long locationId, String status) {
        locationMapper.updateStatus(locationId, status);
    }

    @Override
    public void assignPalletToLocation(Long palletId, Long locationId) {
        locationMapper.assignPallet(locationId, palletId);
    }

    // ==================== 转换方法 ====================

    private Pallet convertToPalletEntity(PalletPO po) {
        return Pallet.builder()
                .palletId(Long.parseLong(po.getPalletId()))
                .palletNo(po.getPalletId())
                .orderId(Long.parseLong(po.getOrderId()))
                .length(po.getLength())
                .width(po.getWidth())
                .height(po.getHeight())
                .totalWeight(po.getWeight())
                .status(po.getState())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private PalletPO convertToPalletPO(Pallet entity) {
        return PalletPO.builder()
                .palletId(entity.getPalletNo())
                .orderId(entity.getOrderId() != null ? entity.getOrderId().toString() : null)
                .length(entity.getLength())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .weight(entity.getTotalWeight())
                .state(entity.getStatus())
                .build();
    }

    private Carton convertToCartonEntity(CartonPO po) {
        return Carton.builder()
                .cartonId(po.getCartonId())
                .cartonNo(po.getCartonNo())
                .orderId(po.getOrderId())
                .palletId(po.getPalletId())
                .fbaLabel(po.getFbaLabel())
                .length(po.getLength())
                .width(po.getWidth())
                .height(po.getHeight())
                .weight(po.getWeight())
                .sku(po.getSku())
                .quantity(po.getQuantity())
                .status(po.getStatus())
                .hasSensitive(po.getHasSensitive())
                .needSpecialHandle(po.getNeedSpecialHandle())
                .specialHandleNote(po.getSpecialHandleNote())
                .remark(po.getRemark())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private CartonPO convertToCartonPO(Carton entity) {
        return CartonPO.builder()
                .cartonId(entity.getCartonId())
                .cartonNo(entity.getCartonNo())
                .orderId(entity.getOrderId())
                .palletId(entity.getPalletId())
                .fbaLabel(entity.getFbaLabel())
                .length(entity.getLength())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .weight(entity.getWeight())
                .sku(entity.getSku())
                .quantity(entity.getQuantity())
                .status(entity.getStatus())
                .hasSensitive(entity.getHasSensitive())
                .needSpecialHandle(entity.getNeedSpecialHandle())
                .specialHandleNote(entity.getSpecialHandleNote())
                .remark(entity.getRemark())
                .build();
    }

    private WarehouseLocation convertToLocationEntity(WarehouseLocationPO po) {
        return WarehouseLocation.builder()
                .locationId(po.getLocationId())
                .locationCode(po.getLocationCode())
                .warehouseCode(po.getWarehouseCode())
                .zone(po.getZone())
                .row(po.getRow())
                .col(po.getCol())
                .level(po.getLevel())
                .locationType(po.getLocationType())
                .status(po.getStatus())
                .currentPalletId(po.getCurrentPalletId())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private WarehouseLocationPO convertToLocationPO(WarehouseLocation entity) {
        return WarehouseLocationPO.builder()
                .locationId(entity.getLocationId())
                .locationCode(entity.getLocationCode())
                .warehouseCode(entity.getWarehouseCode())
                .zone(entity.getZone())
                .row(entity.getRow())
                .col(entity.getCol())
                .level(entity.getLevel())
                .locationType(entity.getLocationType())
                .status(entity.getStatus())
                .currentPalletId(entity.getCurrentPalletId())
                .build();
    }

}
