package com.fba.logi.domain.shipping.service.impl;

import com.fba.logi.common.constants.Constants;
import com.fba.logi.common.exception.BusinessException;
import com.fba.logi.domain.order.repository.IOrderRepository;
import com.fba.logi.domain.shipping.model.entity.Container;
import com.fba.logi.domain.shipping.model.entity.Voyage;
import com.fba.logi.domain.shipping.repository.IContainerRepository;
import com.fba.logi.domain.shipping.repository.IVoyageRepository;
import com.fba.logi.domain.shipping.service.IShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 船运服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements IShippingService {

    private final IContainerRepository containerRepository;
    private final IVoyageRepository voyageRepository;
    private final IOrderRepository orderRepository;

    // ==================== 柜子操作 ====================

    @Override
    public Long createContainer(String containerType, Long voyageId) {
        // 创建柜子
        Container container = new Container();
        container.setContainerNo(generateContainerNo());
        container.setContainerType(containerType);
        container.setStatus(Constants.ContainerState.EMPTY);

        // 设置容量限制（根据柜型）
        setContainerCapacity(container, containerType);

        // 关联航次
        if (voyageId != null) {
            Voyage voyage = voyageRepository.queryById(voyageId);
            if (voyage == null) {
                throw new BusinessException("航次不存在: " + voyageId);
            }
            container.setVoyageId(voyageId);
            container.setVoyageNo(voyage.getVoyageNo());
        }

        container.setCreateTime(LocalDateTime.now());
        container.setUpdateTime(LocalDateTime.now());

        containerRepository.save(container);
        return container.getContainerId();
    }

    @Override
    public Container getContainerDetail(Long containerId) {
        Container container = containerRepository.queryById(containerId);
        if (container == null) {
            throw new BusinessException("柜子不存在: " + containerId);
        }
        return container;
    }

    @Override
    public Container getContainerByNo(String containerNo) {
        Container container = containerRepository.queryByContainerNo(containerNo);
        if (container == null) {
            throw new BusinessException("柜子不存在: " + containerNo);
        }
        return container;
    }

    @Override
    public List<Container> getAvailableContainers(String containerType) {
        return containerRepository.queryAvailableContainers(containerType);
    }

    @Override
    public void assignOrderToContainer(Long orderId, Long containerId, BigDecimal volume, BigDecimal weight) {
        Container container = containerRepository.queryById(containerId);
        if (container == null) {
            throw new BusinessException("柜子不存在: " + containerId);
        }

        // 检查柜子状态
        String status = container.getStatus();
        if (!Constants.ContainerState.EMPTY.equals(status) &&
                !Constants.ContainerState.ASSIGNING.equals(status)) {
            throw new BusinessException("柜子状态不允许排柜: " + status);
        }

        // 检查容量
        if (!container.canLoad(volume, weight)) {
            throw new BusinessException("柜子容量不足");
        }

        // 更新柜子状态为排柜中
        if (Constants.ContainerState.EMPTY.equals(status)) {
            container.startAssigning();
        }

        // 添加货物
        container.addCargo(volume, weight);
        container.getOrderIds().add(orderId);
        container.setUpdateTime(LocalDateTime.now());

        containerRepository.save(container);

        // 更新订单状态
        orderRepository.updateContainerInfo(orderId, containerId, container.getContainerNo());
        orderRepository.updateState(orderId, Constants.OrderState.CONTAINER_ASSIGNED);
    }

    @Override
    public void loadOrderToContainer(Long orderId, Long containerId) {
        Container container = containerRepository.queryById(containerId);
        if (container == null) {
            throw new BusinessException("柜子不存在: " + containerId);
        }

        // 检查柜子状态
        String status = container.getStatus();
        if (!Constants.ContainerState.ASSIGNING.equals(status) &&
                !Constants.ContainerState.LOADING.equals(status)) {
            throw new BusinessException("柜子状态不允许装柜: " + status);
        }

        // 更新柜子状态为装柜中
        if (Constants.ContainerState.ASSIGNING.equals(status)) {
            container.startLoading();
            containerRepository.save(container);
        }

        // 更新订单状态
        orderRepository.updateState(orderId, Constants.OrderState.CONTAINER_LOADED);
        orderRepository.updateLoadedTime(orderId, LocalDateTime.now());
    }

    @Override
    public void finishContainerLoading(Long containerId) {
        Container container = containerRepository.queryById(containerId);
        if (container == null) {
            throw new BusinessException("柜子不存在: " + containerId);
        }

        if (!Constants.ContainerState.LOADING.equals(container.getStatus())) {
            throw new BusinessException("柜子状态不允许完成装柜: " + container.getStatus());
        }

        container.finishLoading();
        container.setUpdateTime(LocalDateTime.now());
        containerRepository.save(container);
    }

    @Override
    public List<Long> getOrdersInContainer(Long containerId) {
        Container container = containerRepository.queryById(containerId);
        if (container == null) {
            return new ArrayList<>();
        }
        return container.getOrderIds();
    }

    // ==================== 航次操作 ====================

    @Override
    public Long createVoyage(Voyage voyage) {
        voyage.setVoyageNo(generateVoyageNo());
        voyage.setStatus(Constants.VoyageState.SCHEDULED);
        voyage.setCreateTime(LocalDateTime.now());
        voyage.setUpdateTime(LocalDateTime.now());

        voyageRepository.save(voyage);
        return voyage.getVoyageId();
    }

    @Override
    public Voyage getVoyageDetail(Long voyageId) {
        Voyage voyage = voyageRepository.queryById(voyageId);
        if (voyage == null) {
            throw new BusinessException("航次不存在: " + voyageId);
        }
        return voyage;
    }

    @Override
    public Voyage getVoyageByNo(String voyageNo) {
        Voyage voyage = voyageRepository.queryByVoyageNo(voyageNo);
        if (voyage == null) {
            throw new BusinessException("航次不存在: " + voyageNo);
        }
        return voyage;
    }

    @Override
    public void assignContainerToVoyage(Long containerId, Long voyageId) {
        Container container = containerRepository.queryById(containerId);
        if (container == null) {
            throw new BusinessException("柜子不存在: " + containerId);
        }

        Voyage voyage = voyageRepository.queryById(voyageId);
        if (voyage == null) {
            throw new BusinessException("航次不存在: " + voyageId);
        }

        // 检查柜子是否已分配航次
        if (container.getVoyageId() != null) {
            throw new BusinessException("柜子已分配到其他航次");
        }

        // 更新柜子航次信息
        container.setVoyageId(voyageId);
        container.setVoyageNo(voyage.getVoyageNo());
        container.setUpdateTime(LocalDateTime.now());
        containerRepository.save(container);

        // 更新航次柜子列表
        voyage.addContainer(containerId);
        voyage.setUpdateTime(LocalDateTime.now());
        voyageRepository.save(voyage);
    }

    @Override
    public int departVoyage(Long voyageId, LocalDateTime actualDeparture) {
        Voyage voyage = voyageRepository.queryById(voyageId);
        if (voyage == null) {
            throw new BusinessException("航次不存在: " + voyageId);
        }

        if (!voyage.canDepart()) {
            throw new BusinessException("航次状态不允许开船: " + voyage.getStatus());
        }

        // 更新航次状态
        voyage.depart(actualDeparture);
        voyage.setUpdateTime(LocalDateTime.now());
        voyageRepository.save(voyage);

        // 批量更新柜子状态
        List<Long> containerIds = voyage.getContainerIds();
        containerRepository.batchUpdateStatus(containerIds, Constants.ContainerState.DEPARTED);

        // 批量更新订单状态
        int orderCount = 0;
        for (Long containerId : containerIds) {
            List<Long> orderIds = containerRepository.queryOrderIds(containerId);
            if (!orderIds.isEmpty()) {
                orderRepository.batchUpdateState(orderIds, Constants.OrderState.DEPARTED);
                orderRepository.batchUpdateDepartureTime(orderIds, actualDeparture);
                orderCount += orderIds.size();
            }
        }

        return orderCount;
    }

    @Override
    public int arrivePort(Long voyageId, LocalDateTime actualArrival) {
        Voyage voyage = voyageRepository.queryById(voyageId);
        if (voyage == null) {
            throw new BusinessException("航次不存在: " + voyageId);
        }

        if (!voyage.canArrive()) {
            throw new BusinessException("航次状态不允许到港: " + voyage.getStatus());
        }

        // 更新航次状态
        voyage.arrive(actualArrival);
        voyage.setUpdateTime(LocalDateTime.now());
        voyageRepository.save(voyage);

        // 批量更新柜子状态
        List<Long> containerIds = voyage.getContainerIds();
        containerRepository.batchUpdateStatus(containerIds, Constants.ContainerState.ARRIVED);

        // 批量更新订单状态
        int orderCount = 0;
        for (Long containerId : containerIds) {
            List<Long> orderIds = containerRepository.queryOrderIds(containerId);
            if (!orderIds.isEmpty()) {
                orderRepository.batchUpdateState(orderIds, Constants.OrderState.ARRIVED);
                orderRepository.batchUpdateArrivalTime(orderIds, actualArrival);
                orderCount += orderIds.size();
            }
        }

        return orderCount;
    }

    @Override
    public int pickContainer(Long containerId) {
        Container container = containerRepository.queryById(containerId);
        if (container == null) {
            throw new BusinessException("柜子不存在: " + containerId);
        }

        if (!Constants.ContainerState.ARRIVED.equals(container.getStatus())) {
            throw new BusinessException("柜子状态不允许提柜: " + container.getStatus());
        }

        // 更新柜子状态
        container.pick();
        container.setUpdateTime(LocalDateTime.now());
        containerRepository.save(container);

        // 批量更新订单状态
        List<Long> orderIds = container.getOrderIds();
        if (!orderIds.isEmpty()) {
            orderRepository.batchUpdateState(orderIds, Constants.OrderState.CONTAINER_PICKED);
            orderRepository.batchUpdatePickedTime(orderIds, LocalDateTime.now());
        }

        return orderIds.size();
    }

    // ==================== 查询统计 ====================

    @Override
    public List<Voyage> getUpcomingVoyages(int days) {
        LocalDateTime endTime = LocalDateTime.now().plusDays(days);
        return voyageRepository.queryUpcoming(endTime);
    }

    @Override
    public List<Voyage> getVoyagesByStatus(String status) {
        return voyageRepository.queryByStatus(status);
    }

    @Override
    public VoyageStatistics getVoyageStatistics(Long voyageId) {
        Voyage voyage = voyageRepository.queryById(voyageId);
        if (voyage == null) {
            throw new BusinessException("航次不存在: " + voyageId);
        }

        List<Long> containerIds = voyage.getContainerIds();
        int containerCount = containerIds.size();
        int orderCount = 0;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;

        for (Long containerId : containerIds) {
            Container container = containerRepository.queryById(containerId);
            if (container != null) {
                orderCount += container.getOrderIds().size();
                totalWeight = totalWeight.add(container.getCurrentWeight());
                totalVolume = totalVolume.add(container.getCurrentCapacity());
            }
        }

        return new VoyageStatistics(
                voyageId,
                voyage.getVoyageNo(),
                containerCount,
                orderCount,
                totalWeight,
                totalVolume
        );
    }

    // ==================== 私有方法 ====================

    /**
     * 生成柜子编号
     */
    private String generateContainerNo() {
        // 格式：CTN + 年月日 + 4位序号
        String dateStr = java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 实际实现应从数据库获取当日序号
        int seq = (int) (Math.random() * 9000) + 1000;
        return "CTN" + dateStr + seq;
    }

    /**
     * 生成航次编号
     */
    private String generateVoyageNo() {
        // 格式：VOY + 年月日 + 4位序号
        String dateStr = java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = (int) (Math.random() * 9000) + 1000;
        return "VOY" + dateStr + seq;
    }

    /**
     * 根据柜型设置容量
     */
    private void setContainerCapacity(Container container, String containerType) {
        switch (containerType) {
            case Constants.ContainerType.GP20:
                container.setMaxCapacity(new BigDecimal("33"));    // 33 CBM
                container.setMaxWeight(new BigDecimal("22000"));   // 22 吨
                break;
            case Constants.ContainerType.GP40:
                container.setMaxCapacity(new BigDecimal("67"));    // 67 CBM
                container.setMaxWeight(new BigDecimal("26000"));   // 26 吨
                break;
            case Constants.ContainerType.HQ40:
                container.setMaxCapacity(new BigDecimal("76"));    // 76 CBM
                container.setMaxWeight(new BigDecimal("26000"));   // 26 吨
                break;
            case Constants.ContainerType.HQ45:
                container.setMaxCapacity(new BigDecimal("86"));    // 86 CBM
                container.setMaxWeight(new BigDecimal("26000"));   // 26 吨
                break;
            default:
                throw new BusinessException("不支持的柜型: " + containerType);
        }
        container.setCurrentCapacity(BigDecimal.ZERO);
        container.setCurrentWeight(BigDecimal.ZERO);
    }
}
