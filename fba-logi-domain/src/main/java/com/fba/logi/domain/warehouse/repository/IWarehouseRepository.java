package com.fba.logi.domain.warehouse.repository;

import com.fba.logi.domain.warehouse.model.entity.Carton;
import com.fba.logi.domain.warehouse.model.entity.Pallet;
import com.fba.logi.domain.warehouse.model.entity.WarehouseLocation;

import java.util.List;

/**
 * 仓储仓储接口
 */
public interface IWarehouseRepository {

    // ==================== 托盘操作 ====================

    /**
     * 根据托盘 ID 查询托盘
     *
     * @param palletId 托盘 ID
     * @return 托盘实体
     */
    Pallet queryPalletById(Long palletId);

    /**
     * 根据托盘编号查询托盘
     *
     * @param palletNo 托盘编号
     * @return 托盘实体
     */
    Pallet queryPalletByNo(String palletNo);

    /**
     * 根据订单 ID 查询托盘列表
     *
     * @param orderId 订单 ID
     * @return 托盘列表
     */
    List<Pallet> queryPalletsByOrderId(Long orderId);

    /**
     * 保存托盘
     *
     * @param pallet 托盘实体
     */
    void savePallet(Pallet pallet);

    /**
     * 更新托盘状态
     *
     * @param palletId 托盘 ID
     * @param status   新状态
     */
    void updatePalletStatus(Long palletId, String status);

    /**
     * 删除托盘
     *
     * @param palletId 托盘 ID
     */
    void deletePallet(Long palletId);

    /**
     * 生成托盘编号
     *
     * @return 托盘编号
     */
    String generatePalletNo();

    // ==================== 纸箱操作 ====================

    /**
     * 根据纸箱 ID 查询纸箱
     *
     * @param cartonId 纸箱 ID
     * @return 纸箱实体
     */
    Carton queryCartonById(Long cartonId);

    /**
     * 根据纸箱编号查询纸箱
     *
     * @param cartonNo 纸箱编号
     * @return 纸箱实体
     */
    Carton queryCartonByNo(String cartonNo);

    /**
     * 根据订单 ID 查询纸箱列表
     *
     * @param orderId 订单 ID
     * @return 纸箱列表
     */
    List<Carton> queryCartonsByOrderId(Long orderId);

    /**
     * 根据托盘 ID 查询纸箱列表
     *
     * @param palletId 托盘 ID
     * @return 纸箱列表
     */
    List<Carton> queryCartonsByPalletId(Long palletId);

    /**
     * 保存纸箱
     *
     * @param carton 纸箱实体
     */
    void saveCarton(Carton carton);

    /**
     * 批量保存纸箱
     *
     * @param cartons 纸箱列表
     */
    void saveCartons(List<Carton> cartons);

    /**
     * 更新纸箱状态
     *
     * @param cartonId 纸箱 ID
     * @param status   新状态
     */
    void updateCartonStatus(Long cartonId, String status);

    /**
     * 分配纸箱到托盘
     *
     * @param cartonId 纸箱 ID
     * @param palletId 托盘 ID
     */
    void assignCartonToPallet(Long cartonId, Long palletId);

    /**
     * 生成纸箱编号
     *
     * @return 纸箱编号
     */
    String generateCartonNo();

    // ==================== 库位操作 ====================

    /**
     * 根据库位 ID 查询库位
     *
     * @param locationId 库位 ID
     * @return 库位实体
     */
    WarehouseLocation queryLocationById(Long locationId);

    /**
     * 根据库位编号查询库位
     *
     * @param locationCode 库位编号
     * @return 库位实体
     */
    WarehouseLocation queryLocationByCode(String locationCode);

    /**
     * 查询可用库位列表
     *
     * @param warehouseCode 仓库代码
     * @param locationType  库位类型
     * @return 可用库位列表
     */
    List<WarehouseLocation> queryAvailableLocations(String warehouseCode, String locationType);

    /**
     * 保存库位
     *
     * @param location 库位实体
     */
    void saveLocation(WarehouseLocation location);

    /**
     * 更新库位状态
     *
     * @param locationId 库位 ID
     * @param status     新状态
     */
    void updateLocationStatus(Long locationId, String status);

    /**
     * 分配托盘到库位
     *
     * @param palletId   托盘 ID
     * @param locationId 库位 ID
     */
    void assignPalletToLocation(Long palletId, Long locationId);

}
