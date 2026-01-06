package com.fba.logi.domain.warehouse.service.impl;

import com.fba.logi.domain.warehouse.model.entity.Carton;
import com.fba.logi.domain.warehouse.model.entity.Pallet;
import com.fba.logi.domain.warehouse.model.entity.WarehouseLocation;
import com.fba.logi.domain.warehouse.repository.IWarehouseRepository;
import com.fba.logi.domain.warehouse.service.IWarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 仓储服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements IWarehouseService {

    private final IWarehouseRepository warehouseRepository;

    // ==================== 托盘操作 ====================

    @Override
    @Transactional
    public Long createPallet(Long orderId, String palletType) {
        String palletNo = warehouseRepository.generatePalletNo();

        Pallet pallet = Pallet.builder()
                .palletNo(palletNo)
                .orderId(orderId)
                .palletType(palletType)
                .palletSize("standard".equals(palletType) ? "48x40" : "48x48")
                .status("pending")
                .totalWeight(BigDecimal.ZERO)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        warehouseRepository.savePallet(pallet);
        log.info("托盘创建成功，托盘编号: {}, 订单 ID: {}", palletNo, orderId);
        return pallet.getPalletId();
    }

    @Override
    public Pallet getPalletDetail(Long palletId) {
        Pallet pallet = warehouseRepository.queryPalletById(palletId);
        if (pallet == null) {
            throw new IllegalArgumentException("托盘不存在: " + palletId);
        }

        // 加载纸箱列表
        List<Carton> cartons = warehouseRepository.queryCartonsByPalletId(palletId);
        pallet.setCartons(cartons);

        return pallet;
    }

    @Override
    public List<Pallet> getOrderPallets(Long orderId) {
        List<Pallet> pallets = warehouseRepository.queryPalletsByOrderId(orderId);
        // 加载每个托盘的纸箱
        for (Pallet pallet : pallets) {
            List<Carton> cartons = warehouseRepository.queryCartonsByPalletId(pallet.getPalletId());
            pallet.setCartons(cartons);
        }
        return pallets;
    }

    @Override
    @Transactional
    public void updatePalletDimensions(Long palletId, BigDecimal length,
                                        BigDecimal width, BigDecimal height) {
        Pallet pallet = warehouseRepository.queryPalletById(palletId);
        if (pallet == null) {
            throw new IllegalArgumentException("托盘不存在: " + palletId);
        }

        pallet.setLength(length);
        pallet.setWidth(width);
        pallet.setHeight(height);
        pallet.setUpdateTime(LocalDateTime.now());

        warehouseRepository.savePallet(pallet);
        log.info("托盘尺寸更新成功，托盘 ID: {}", palletId);
    }

    // ==================== 纸箱操作 ====================

    @Override
    @Transactional
    public Long createCarton(Carton carton) {
        String cartonNo = warehouseRepository.generateCartonNo();
        carton.setCartonNo(cartonNo);
        carton.setStatus("pending");
        carton.setCreateTime(LocalDateTime.now());
        carton.setUpdateTime(LocalDateTime.now());

        warehouseRepository.saveCarton(carton);
        log.info("纸箱创建成功，纸箱编号: {}", cartonNo);
        return carton.getCartonId();
    }

    @Override
    @Transactional
    public List<Long> createCartons(List<Carton> cartons) {
        List<Long> ids = new ArrayList<>();
        for (Carton carton : cartons) {
            Long id = createCarton(carton);
            ids.add(id);
        }
        return ids;
    }

    @Override
    public List<Carton> getOrderCartons(Long orderId) {
        return warehouseRepository.queryCartonsByOrderId(orderId);
    }

    @Override
    @Transactional
    public void assignCartonsToPallet(List<Long> cartonIds, Long palletId) {
        Pallet pallet = warehouseRepository.queryPalletById(palletId);
        if (pallet == null) {
            throw new IllegalArgumentException("托盘不存在: " + palletId);
        }

        for (Long cartonId : cartonIds) {
            warehouseRepository.assignCartonToPallet(cartonId, palletId);
        }

        // 重新计算托盘重量
        List<Carton> cartons = warehouseRepository.queryCartonsByPalletId(palletId);
        pallet.setCartons(cartons);
        pallet.recalculateWeight();
        pallet.setStatus("packed");
        pallet.setUpdateTime(LocalDateTime.now());

        warehouseRepository.savePallet(pallet);
        log.info("纸箱分配成功，托盘 ID: {}, 纸箱数量: {}", palletId, cartonIds.size());
    }

    @Override
    @Transactional
    public void removeCartonFromPallet(Long cartonId) {
        Carton carton = warehouseRepository.queryCartonById(cartonId);
        if (carton == null) {
            throw new IllegalArgumentException("纸箱不存在: " + cartonId);
        }

        Long palletId = carton.getPalletId();
        if (palletId == null) {
            return;
        }

        warehouseRepository.assignCartonToPallet(cartonId, null);

        // 重新计算托盘重量
        Pallet pallet = warehouseRepository.queryPalletById(palletId);
        List<Carton> cartons = warehouseRepository.queryCartonsByPalletId(palletId);
        pallet.setCartons(cartons);
        pallet.recalculateWeight();
        pallet.setUpdateTime(LocalDateTime.now());

        warehouseRepository.savePallet(pallet);
        log.info("纸箱移除成功，纸箱 ID: {}, 托盘 ID: {}", cartonId, palletId);
    }

    @Override
    public CartonValidationResult validateCarton(Long cartonId) {
        Carton carton = warehouseRepository.queryCartonById(cartonId);
        if (carton == null) {
            throw new IllegalArgumentException("纸箱不存在: " + cartonId);
        }

        boolean oversized = carton.isOversized();
        boolean overweight = carton.isOverweight();
        boolean valid = !oversized && !overweight;

        StringBuilder message = new StringBuilder();
        if (oversized) {
            message.append("尺寸超标（最大边超过 63.5cm）；");
        }
        if (overweight) {
            message.append("重量超标（超过 22.7kg）；");
        }
        if (valid) {
            message.append("符合 FBA 标准");
        }

        return new CartonValidationResult(valid, oversized, overweight, message.toString());
    }

    // ==================== 入库出库 ====================

    @Override
    @Transactional
    public void receiveCartons(List<Long> cartonIds) {
        for (Long cartonId : cartonIds) {
            warehouseRepository.updateCartonStatus(cartonId, "received");
        }
        log.info("纸箱入库成功，数量: {}", cartonIds.size());
    }

    @Override
    @Transactional
    public void storePallet(Long palletId, Long locationId) {
        Pallet pallet = warehouseRepository.queryPalletById(palletId);
        if (pallet == null) {
            throw new IllegalArgumentException("托盘不存在: " + palletId);
        }

        WarehouseLocation location = warehouseRepository.queryLocationById(locationId);
        if (location == null) {
            throw new IllegalArgumentException("库位不存在: " + locationId);
        }

        if (!location.canStore(pallet.getTotalWeight().intValue())) {
            throw new IllegalStateException("库位容量不足");
        }

        // 分配托盘到库位
        warehouseRepository.assignPalletToLocation(palletId, locationId);

        // 更新托盘状态
        warehouseRepository.updatePalletStatus(palletId, "stored");

        // 更新库位状态
        location.occupy(pallet.getTotalWeight().intValue());
        warehouseRepository.saveLocation(location);

        // 更新纸箱状态
        List<Carton> cartons = warehouseRepository.queryCartonsByPalletId(palletId);
        for (Carton carton : cartons) {
            warehouseRepository.updateCartonStatus(carton.getCartonId(), "stored");
        }

        log.info("托盘入库成功，托盘 ID: {}, 库位: {}", palletId, location.getLocationCode());
    }

    @Override
    @Transactional
    public void releasePallet(Long palletId) {
        Pallet pallet = getPalletDetail(palletId);
        if (pallet == null) {
            throw new IllegalArgumentException("托盘不存在: " + palletId);
        }

        if (pallet.getLocation() != null) {
            WarehouseLocation location = pallet.getLocation();
            location.release(pallet.getTotalWeight().intValue());
            warehouseRepository.saveLocation(location);
        }

        warehouseRepository.updatePalletStatus(palletId, "released");

        // 更新纸箱状态
        for (Carton carton : pallet.getCartons()) {
            warehouseRepository.updateCartonStatus(carton.getCartonId(), "released");
        }

        log.info("托盘出库成功，托盘 ID: {}", palletId);
    }

    @Override
    @Transactional
    public WarehouseLocation assignLocation(Long palletId, String warehouseCode) {
        Pallet pallet = warehouseRepository.queryPalletById(palletId);
        if (pallet == null) {
            throw new IllegalArgumentException("托盘不存在: " + palletId);
        }

        // 查询可用库位
        List<WarehouseLocation> availableLocations =
                warehouseRepository.queryAvailableLocations(warehouseCode, "pallet");

        // 选择第一个能容纳托盘的库位
        for (WarehouseLocation location : availableLocations) {
            if (location.canStore(pallet.getTotalWeight().intValue())) {
                storePallet(palletId, location.getLocationId());
                return location;
            }
        }

        throw new IllegalStateException("没有可用的库位");
    }

    // ==================== 查询统计 ====================

    @Override
    public List<WarehouseLocation> getAvailableLocations(String warehouseCode) {
        return warehouseRepository.queryAvailableLocations(warehouseCode, null);
    }

    @Override
    public WarehouseOverview getWarehouseOverview(String warehouseCode) {
        // 这里需要实际的统计查询，暂时返回模拟数据
        return new WarehouseOverview(
                warehouseCode,
                100,  // 总库位数
                60,   // 可用库位数
                40,   // 占用库位数
                30,   // 托盘数
                150   // 纸箱数
        );
    }

}
