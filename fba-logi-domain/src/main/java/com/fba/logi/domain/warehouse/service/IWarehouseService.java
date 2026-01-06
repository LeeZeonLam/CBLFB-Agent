package com.fba.logi.domain.warehouse.service;

import com.fba.logi.domain.warehouse.model.entity.Carton;
import com.fba.logi.domain.warehouse.model.entity.Pallet;
import com.fba.logi.domain.warehouse.model.entity.WarehouseLocation;

import java.util.List;

/**
 * 仓储服务接口
 */
public interface IWarehouseService {

    // ==================== 托盘操作 ====================

    /**
     * 创建托盘
     *
     * @param orderId    订单 ID
     * @param palletType 托盘类型
     * @return 托盘 ID
     */
    Long createPallet(Long orderId, String palletType);

    /**
     * 查询托盘详情（包含纸箱）
     *
     * @param palletId 托盘 ID
     * @return 托盘实体
     */
    Pallet getPalletDetail(Long palletId);

    /**
     * 查询订单的托盘列表
     *
     * @param orderId 订单 ID
     * @return 托盘列表
     */
    List<Pallet> getOrderPallets(Long orderId);

    /**
     * 更新托盘尺寸
     *
     * @param palletId 托盘 ID
     * @param length   长度
     * @param width    宽度
     * @param height   高度
     */
    void updatePalletDimensions(Long palletId, java.math.BigDecimal length,
                                 java.math.BigDecimal width, java.math.BigDecimal height);

    // ==================== 纸箱操作 ====================

    /**
     * 创建纸箱
     *
     * @param carton 纸箱信息
     * @return 纸箱 ID
     */
    Long createCarton(Carton carton);

    /**
     * 批量创建纸箱
     *
     * @param cartons 纸箱列表
     * @return 创建的纸箱 ID 列表
     */
    List<Long> createCartons(List<Carton> cartons);

    /**
     * 查询订单的纸箱列表
     *
     * @param orderId 订单 ID
     * @return 纸箱列表
     */
    List<Carton> getOrderCartons(Long orderId);

    /**
     * 分配纸箱到托盘
     *
     * @param cartonIds 纸箱 ID 列表
     * @param palletId  托盘 ID
     */
    void assignCartonsToPallet(List<Long> cartonIds, Long palletId);

    /**
     * 从托盘移除纸箱
     *
     * @param cartonId 纸箱 ID
     */
    void removeCartonFromPallet(Long cartonId);

    /**
     * 校验纸箱尺寸（是否符合 FBA 标准）
     *
     * @param cartonId 纸箱 ID
     * @return 校验结果和问题描述
     */
    CartonValidationResult validateCarton(Long cartonId);

    // ==================== 入库出库 ====================

    /**
     * 纸箱入库
     *
     * @param cartonIds 纸箱 ID 列表
     */
    void receiveCartons(List<Long> cartonIds);

    /**
     * 托盘入库
     *
     * @param palletId   托盘 ID
     * @param locationId 库位 ID
     */
    void storePallet(Long palletId, Long locationId);

    /**
     * 托盘出库
     *
     * @param palletId 托盘 ID
     */
    void releasePallet(Long palletId);

    /**
     * 分配库位（自动分配）
     *
     * @param palletId      托盘 ID
     * @param warehouseCode 仓库代码
     * @return 分配的库位
     */
    WarehouseLocation assignLocation(Long palletId, String warehouseCode);

    // ==================== 查询统计 ====================

    /**
     * 查询仓库可用库位
     *
     * @param warehouseCode 仓库代码
     * @return 可用库位列表
     */
    List<WarehouseLocation> getAvailableLocations(String warehouseCode);

    /**
     * 获取仓库概览
     *
     * @param warehouseCode 仓库代码
     * @return 仓库统计信息
     */
    WarehouseOverview getWarehouseOverview(String warehouseCode);

    // ==================== 内部类 ====================

    /**
     * 纸箱校验结果
     */
    record CartonValidationResult(
            boolean valid,
            boolean oversized,
            boolean overweight,
            String message
    ) {}

    /**
     * 仓库概览
     */
    record WarehouseOverview(
            String warehouseCode,
            int totalLocations,
            int availableLocations,
            int occupiedLocations,
            int totalPallets,
            int totalCartons
    ) {}

}
