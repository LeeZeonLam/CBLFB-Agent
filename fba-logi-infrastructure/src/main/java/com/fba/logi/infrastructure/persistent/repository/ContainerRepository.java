package com.fba.logi.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.shipping.model.entity.Container;
import com.fba.logi.domain.shipping.repository.IContainerRepository;
import com.fba.logi.infrastructure.persistent.dao.IContainerMapper;
import com.fba.logi.infrastructure.persistent.po.ContainerPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 集装箱仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ContainerRepository implements IContainerRepository {

    private final IContainerMapper containerMapper;

    @Override
    public Container queryById(Long containerId) {
        LambdaQueryWrapper<ContainerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContainerPO::getContainerId, containerId);
        ContainerPO po = containerMapper.selectOne(wrapper);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public Container queryByContainerNo(String containerNo) {
        LambdaQueryWrapper<ContainerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContainerPO::getContainerNo, containerNo);
        ContainerPO po = containerMapper.selectOne(wrapper);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<Container> queryByVoyageId(Long voyageId) {
        LambdaQueryWrapper<ContainerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContainerPO::getVoyageId, voyageId);
        return containerMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Container> queryAvailableContainers(String containerType) {
        LambdaQueryWrapper<ContainerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ContainerPO::getStatus, Constants.ContainerState.EMPTY, Constants.ContainerState.ASSIGNING);
        if (containerType != null && !containerType.isEmpty()) {
            wrapper.eq(ContainerPO::getContainerType, containerType);
        }
        return containerMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> queryOrderIds(Long containerId) {
        // 从订单表查询柜子关联的订单 ID（这里返回空列表，实际需要关联查询）
        // 实际实现应该调用 OrderRepository 或者做关联查询
        return new ArrayList<>();
    }

    @Override
    public void save(Container container) {
        ContainerPO po = convertToPO(container);
        if (po.getId() == null) {
            // 生成 containerId
            if (po.getContainerId() == null) {
                po.setContainerId(System.currentTimeMillis());
            }
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            containerMapper.insert(po);
            container.setContainerId(po.getContainerId());
        } else {
            po.setUpdateTime(LocalDateTime.now());
            containerMapper.updateById(po);
        }
    }

    @Override
    public void batchUpdateStatus(List<Long> containerIds, String status) {
        if (containerIds != null && !containerIds.isEmpty()) {
            containerMapper.batchUpdateStatus(containerIds, status);
        }
    }

    @Override
    public void updateCapacity(Long containerId, BigDecimal currentCapacity, BigDecimal currentWeight) {
        containerMapper.updateCapacity(containerId, currentCapacity, currentWeight);
    }

    /**
     * PO 转换为实体
     */
    private Container convertToEntity(ContainerPO po) {
        Container container = new Container();
        container.setContainerId(po.getContainerId());
        container.setContainerNo(po.getContainerNo());
        container.setContainerType(po.getContainerType());
        container.setSealNo(po.getSealNo());
        container.setMaxCapacity(po.getMaxCapacity());
        container.setMaxWeight(po.getMaxWeight());
        container.setCurrentCapacity(po.getCurrentCapacity());
        container.setCurrentWeight(po.getCurrentWeight());
        container.setVoyageId(po.getVoyageId());
        container.setVoyageNo(po.getVoyageNo());
        container.setStatus(po.getStatus());
        container.setCreateTime(po.getCreateTime());
        container.setUpdateTime(po.getUpdateTime());
        return container;
    }

    /**
     * 实体转换为 PO
     */
    private ContainerPO convertToPO(Container entity) {
        // 查询是否已存在
        Long id = null;
        if (entity.getContainerId() != null) {
            LambdaQueryWrapper<ContainerPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ContainerPO::getContainerId, entity.getContainerId());
            ContainerPO existing = containerMapper.selectOne(wrapper);
            if (existing != null) {
                id = existing.getId();
            }
        }

        return ContainerPO.builder()
                .id(id)
                .containerId(entity.getContainerId())
                .containerNo(entity.getContainerNo())
                .containerType(entity.getContainerType())
                .sealNo(entity.getSealNo())
                .maxCapacity(entity.getMaxCapacity())
                .maxWeight(entity.getMaxWeight())
                .currentCapacity(entity.getCurrentCapacity())
                .currentWeight(entity.getCurrentWeight())
                .voyageId(entity.getVoyageId())
                .voyageNo(entity.getVoyageNo())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
