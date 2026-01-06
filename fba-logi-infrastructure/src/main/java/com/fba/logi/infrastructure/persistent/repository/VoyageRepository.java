package com.fba.logi.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fba.logi.domain.shipping.model.entity.Voyage;
import com.fba.logi.domain.shipping.repository.IVoyageRepository;
import com.fba.logi.infrastructure.persistent.dao.IVoyageMapper;
import com.fba.logi.infrastructure.persistent.po.VoyagePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 航次仓储实现
 */
@Repository
@RequiredArgsConstructor
public class VoyageRepository implements IVoyageRepository {

    private final IVoyageMapper voyageMapper;

    @Override
    public Voyage queryById(Long voyageId) {
        LambdaQueryWrapper<VoyagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoyagePO::getVoyageId, voyageId);
        VoyagePO po = voyageMapper.selectOne(wrapper);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public Voyage queryByVoyageNo(String voyageNo) {
        LambdaQueryWrapper<VoyagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoyagePO::getVoyageNo, voyageNo);
        VoyagePO po = voyageMapper.selectOne(wrapper);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<Voyage> queryByStatus(String status) {
        LambdaQueryWrapper<VoyagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoyagePO::getStatus, status);
        wrapper.orderByDesc(VoyagePO::getEstimatedDeparture);
        return voyageMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Voyage> queryUpcoming(LocalDateTime beforeTime) {
        LambdaQueryWrapper<VoyagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(VoyagePO::getEstimatedDeparture, beforeTime);
        wrapper.ge(VoyagePO::getEstimatedDeparture, LocalDateTime.now());
        wrapper.eq(VoyagePO::getStatus, "SCHEDULED");
        wrapper.orderByAsc(VoyagePO::getEstimatedDeparture);
        return voyageMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Voyage voyage) {
        VoyagePO po = convertToPO(voyage);
        if (po.getId() == null) {
            if (po.getVoyageId() == null) {
                po.setVoyageId(System.currentTimeMillis());
            }
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            voyageMapper.insert(po);
            voyage.setVoyageId(po.getVoyageId());
        } else {
            po.setUpdateTime(LocalDateTime.now());
            voyageMapper.updateById(po);
        }
    }

    @Override
    public void updateDepartureTime(Long voyageId, LocalDateTime actualDeparture, String status) {
        voyageMapper.updateDeparture(voyageId, actualDeparture, status);
    }

    @Override
    public void updateArrivalTime(Long voyageId, LocalDateTime actualArrival, String status) {
        voyageMapper.updateArrival(voyageId, actualArrival, status);
    }

    /**
     * PO 转换为实体
     */
    private Voyage convertToEntity(VoyagePO po) {
        Voyage voyage = new Voyage();
        voyage.setVoyageId(po.getVoyageId());
        voyage.setVoyageNo(po.getVoyageNo());
        voyage.setVesselName(po.getVesselName());
        voyage.setCarrier(po.getCarrier());
        voyage.setChannelCode(po.getChannelCode());
        voyage.setChannelName(po.getChannelName());
        voyage.setDeparturePort(po.getDeparturePort());
        voyage.setArrivalPort(po.getArrivalPort());
        voyage.setEstimatedDeparture(po.getEstimatedDeparture());
        voyage.setActualDeparture(po.getActualDeparture());
        voyage.setEstimatedArrival(po.getEstimatedArrival());
        voyage.setActualArrival(po.getActualArrival());
        voyage.setStatus(po.getStatus());
        voyage.setContainerIds(new ArrayList<>()); // 需要从 container 表查询
        voyage.setCreateTime(po.getCreateTime());
        voyage.setUpdateTime(po.getUpdateTime());
        return voyage;
    }

    /**
     * 实体转换为 PO
     */
    private VoyagePO convertToPO(Voyage entity) {
        Long id = null;
        if (entity.getVoyageId() != null) {
            LambdaQueryWrapper<VoyagePO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(VoyagePO::getVoyageId, entity.getVoyageId());
            VoyagePO existing = voyageMapper.selectOne(wrapper);
            if (existing != null) {
                id = existing.getId();
            }
        }

        return VoyagePO.builder()
                .id(id)
                .voyageId(entity.getVoyageId())
                .voyageNo(entity.getVoyageNo())
                .vesselName(entity.getVesselName())
                .carrier(entity.getCarrier())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .departurePort(entity.getDeparturePort())
                .arrivalPort(entity.getArrivalPort())
                .estimatedDeparture(entity.getEstimatedDeparture())
                .actualDeparture(entity.getActualDeparture())
                .estimatedArrival(entity.getEstimatedArrival())
                .actualArrival(entity.getActualArrival())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
