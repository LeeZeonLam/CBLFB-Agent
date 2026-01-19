package com.fba.logi.domain.shipping.repository;

import com.fba.logi.domain.shipping.model.entity.CustomsDeclaration;

import java.util.List;

/**
 * 报关单仓储接口
 */
public interface ICustomsDeclarationRepository {

    /**
     * 根据 ID 查询报关单
     */
    CustomsDeclaration queryById(Long declarationId);

    /**
     * 根据报关单号查询
     */
    CustomsDeclaration queryByDeclarationNo(String declarationNo);

    /**
     * 根据柜子 ID 查询报关单
     */
    CustomsDeclaration queryByContainerId(Long containerId);

    /**
     * 根据航次 ID 查询报关单列表
     */
    List<CustomsDeclaration> queryByVoyageId(Long voyageId);

    /**
     * 根据状态查询报关单列表
     */
    List<CustomsDeclaration> queryByStatus(String status);

    /**
     * 查询待处理的报关单
     */
    List<CustomsDeclaration> queryPendingDeclarations();

    /**
     * 查询查验中的报关单
     */
    List<CustomsDeclaration> queryInspectingDeclarations();

    /**
     * 保存报关单
     */
    void save(CustomsDeclaration declaration);

    /**
     * 更新报关单
     */
    void update(CustomsDeclaration declaration);

    /**
     * 更新报关单状态
     */
    void updateStatus(Long declarationId, String status);

    /**
     * 删除报关单
     */
    void delete(Long declarationId);

    /**
     * 生成报关单号
     */
    String generateDeclarationNo();
}
