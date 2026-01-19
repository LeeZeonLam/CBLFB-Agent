package com.fba.logi.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.shipping.model.entity.CustomsDeclaration;
import com.fba.logi.domain.shipping.repository.ICustomsDeclarationRepository;
import com.fba.logi.infrastructure.persistent.dao.ICustomsDeclarationMapper;
import com.fba.logi.infrastructure.persistent.po.CustomsDeclarationPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报关单仓储实现
 */
@Repository
public class CustomsDeclarationRepository implements ICustomsDeclarationRepository {

    @Resource
    private ICustomsDeclarationMapper customsDeclarationMapper;

    @Override
    public CustomsDeclaration queryById(Long declarationId) {
        LambdaQueryWrapper<CustomsDeclarationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomsDeclarationPO::getDeclarationId, declarationId);
        CustomsDeclarationPO po = customsDeclarationMapper.selectOne(wrapper);
        return convertToEntity(po);
    }

    @Override
    public CustomsDeclaration queryByDeclarationNo(String declarationNo) {
        LambdaQueryWrapper<CustomsDeclarationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomsDeclarationPO::getDeclarationNo, declarationNo);
        CustomsDeclarationPO po = customsDeclarationMapper.selectOne(wrapper);
        return convertToEntity(po);
    }

    @Override
    public CustomsDeclaration queryByContainerId(Long containerId) {
        LambdaQueryWrapper<CustomsDeclarationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomsDeclarationPO::getContainerId, containerId);
        CustomsDeclarationPO po = customsDeclarationMapper.selectOne(wrapper);
        return convertToEntity(po);
    }

    @Override
    public List<CustomsDeclaration> queryByVoyageId(Long voyageId) {
        LambdaQueryWrapper<CustomsDeclarationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomsDeclarationPO::getVoyageId, voyageId);
        return customsDeclarationMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomsDeclaration> queryByStatus(String status) {
        LambdaQueryWrapper<CustomsDeclarationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomsDeclarationPO::getStatus, status);
        return customsDeclarationMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomsDeclaration> queryPendingDeclarations() {
        return queryByStatus(Constants.CustomsStatus.PENDING);
    }

    @Override
    public List<CustomsDeclaration> queryInspectingDeclarations() {
        return queryByStatus(Constants.CustomsStatus.INSPECTING);
    }

    @Override
    public void save(CustomsDeclaration declaration) {
        CustomsDeclarationPO po = convertToPO(declaration);
        po.setCreateTime(LocalDateTime.now());
        po.setUpdateTime(LocalDateTime.now());
        customsDeclarationMapper.insert(po);
    }

    @Override
    public void update(CustomsDeclaration declaration) {
        LambdaQueryWrapper<CustomsDeclarationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomsDeclarationPO::getDeclarationId, declaration.getDeclarationId());
        CustomsDeclarationPO po = convertToPO(declaration);
        po.setUpdateTime(LocalDateTime.now());
        customsDeclarationMapper.update(po, wrapper);
    }

    @Override
    public void updateStatus(Long declarationId, String status) {
        customsDeclarationMapper.updateStatus(declarationId, status);
    }

    @Override
    public void delete(Long declarationId) {
        LambdaQueryWrapper<CustomsDeclarationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomsDeclarationPO::getDeclarationId, declarationId);
        customsDeclarationMapper.delete(wrapper);
    }

    @Override
    public String generateDeclarationNo() {
        return customsDeclarationMapper.generateDeclarationNo();
    }

    // ==================== 转换方法 ====================

    private CustomsDeclaration convertToEntity(CustomsDeclarationPO po) {
        if (po == null) {
            return null;
        }
        return CustomsDeclaration.builder()
                .declarationId(po.getDeclarationId())
                .declarationNo(po.getDeclarationNo())
                .containerId(po.getContainerId())
                .containerNo(po.getContainerNo())
                .voyageId(po.getVoyageId())
                .declarationType(po.getDeclarationType())
                .customsPort(po.getCustomsPort())
                .brokerName(po.getBrokerName())
                .brokerContact(po.getBrokerContact())
                .brokerPhone(po.getBrokerPhone())
                .status(po.getStatus())
                .declaredValue(po.getDeclaredValue())
                .currency(po.getCurrency())
                .declaredWeight(po.getDeclaredWeight())
                .declaredPieces(po.getDeclaredPieces())
                .hsCodes(po.getHsCodes())
                .goodsDescription(po.getGoodsDescription())
                .documentUrls(po.getDocumentUrls())
                .inspectionReason(po.getInspectionReason())
                .rejectReason(po.getRejectReason())
                .remark(po.getRemark())
                .declaredTime(po.getDeclaredTime())
                .clearedTime(po.getClearedTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private CustomsDeclarationPO convertToPO(CustomsDeclaration entity) {
        if (entity == null) {
            return null;
        }
        return CustomsDeclarationPO.builder()
                .declarationId(entity.getDeclarationId())
                .declarationNo(entity.getDeclarationNo())
                .containerId(entity.getContainerId())
                .containerNo(entity.getContainerNo())
                .voyageId(entity.getVoyageId())
                .declarationType(entity.getDeclarationType())
                .customsPort(entity.getCustomsPort())
                .brokerName(entity.getBrokerName())
                .brokerContact(entity.getBrokerContact())
                .brokerPhone(entity.getBrokerPhone())
                .status(entity.getStatus())
                .declaredValue(entity.getDeclaredValue())
                .currency(entity.getCurrency())
                .declaredWeight(entity.getDeclaredWeight())
                .declaredPieces(entity.getDeclaredPieces())
                .hsCodes(entity.getHsCodes())
                .goodsDescription(entity.getGoodsDescription())
                .documentUrls(entity.getDocumentUrls())
                .inspectionReason(entity.getInspectionReason())
                .rejectReason(entity.getRejectReason())
                .remark(entity.getRemark())
                .declaredTime(entity.getDeclaredTime())
                .clearedTime(entity.getClearedTime())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
