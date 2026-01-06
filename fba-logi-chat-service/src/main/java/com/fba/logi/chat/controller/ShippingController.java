package com.fba.logi.chat.controller;

import com.fba.logi.common.response.Response;
import com.fba.logi.domain.shipping.model.entity.Container;
import com.fba.logi.domain.shipping.model.entity.Voyage;
import com.fba.logi.domain.shipping.service.IShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 船运控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Tag(name = "船运接口", description = "柜子和航次管理接口")
public class ShippingController {

    private final IShippingService shippingService;

    // ==================== 柜子操作 ====================

    /**
     * 创建柜子
     */
    @PostMapping("/containers")
    @Operation(summary = "创建柜子", description = "创建新的集装箱")
    public Response<Long> createContainer(
            @RequestParam String containerType,
            @RequestParam(required = false) Long voyageId) {
        Long containerId = shippingService.createContainer(containerType, voyageId);
        return Response.success(containerId);
    }

    /**
     * 查询柜子详情
     */
    @GetMapping("/containers/{containerId}")
    @Operation(summary = "查询柜子详情", description = "根据柜子 ID 查询柜子详情")
    public Response<Container> getContainerDetail(@PathVariable Long containerId) {
        Container container = shippingService.getContainerDetail(containerId);
        return Response.success(container);
    }

    /**
     * 根据柜号查询柜子
     */
    @GetMapping("/containers/no/{containerNo}")
    @Operation(summary = "根据柜号查询", description = "根据柜号查询柜子详情")
    public Response<Container> getContainerByNo(@PathVariable String containerNo) {
        Container container = shippingService.getContainerByNo(containerNo);
        return Response.success(container);
    }

    /**
     * 查询可用柜子
     */
    @GetMapping("/containers/available")
    @Operation(summary = "查询可用柜子", description = "查询可用于排柜的柜子列表")
    public Response<List<Container>> getAvailableContainers(
            @RequestParam(required = false) String containerType) {
        List<Container> containers = shippingService.getAvailableContainers(containerType);
        return Response.success(containers);
    }

    /**
     * 排柜
     */
    @PostMapping("/containers/{containerId}/assign")
    @Operation(summary = "排柜", description = "将订单分配到柜子")
    public Response<Void> assignOrderToContainer(
            @PathVariable Long containerId,
            @RequestParam Long orderId,
            @RequestParam BigDecimal volume,
            @RequestParam BigDecimal weight) {
        shippingService.assignOrderToContainer(orderId, containerId, volume, weight);
        return Response.success();
    }

    /**
     * 装柜确认
     */
    @PostMapping("/containers/{containerId}/load")
    @Operation(summary = "装柜确认", description = "确认订单已装入柜子")
    public Response<Void> loadOrderToContainer(
            @PathVariable Long containerId,
            @RequestParam Long orderId) {
        shippingService.loadOrderToContainer(orderId, containerId);
        return Response.success();
    }

    /**
     * 完成装柜
     */
    @PostMapping("/containers/{containerId}/finish-loading")
    @Operation(summary = "完成装柜", description = "确认柜子装柜完成")
    public Response<Void> finishContainerLoading(@PathVariable Long containerId) {
        shippingService.finishContainerLoading(containerId);
        return Response.success();
    }

    /**
     * 提柜
     */
    @PostMapping("/containers/{containerId}/pick")
    @Operation(summary = "提柜", description = "确认柜子已提柜")
    public Response<Integer> pickContainer(@PathVariable Long containerId) {
        int count = shippingService.pickContainer(containerId);
        return Response.success(count);
    }

    /**
     * 查询柜内订单
     */
    @GetMapping("/containers/{containerId}/orders")
    @Operation(summary = "查询柜内订单", description = "查询柜子内的订单 ID 列表")
    public Response<List<Long>> getOrdersInContainer(@PathVariable Long containerId) {
        List<Long> orderIds = shippingService.getOrdersInContainer(containerId);
        return Response.success(orderIds);
    }

    // ==================== 航次操作 ====================

    /**
     * 创建航次
     */
    @PostMapping("/voyages")
    @Operation(summary = "创建航次", description = "创建新的航次")
    public Response<Long> createVoyage(@RequestBody Voyage voyage) {
        Long voyageId = shippingService.createVoyage(voyage);
        return Response.success(voyageId);
    }

    /**
     * 查询航次详情
     */
    @GetMapping("/voyages/{voyageId}")
    @Operation(summary = "查询航次详情", description = "根据航次 ID 查询航次详情")
    public Response<Voyage> getVoyageDetail(@PathVariable Long voyageId) {
        Voyage voyage = shippingService.getVoyageDetail(voyageId);
        return Response.success(voyage);
    }

    /**
     * 根据航次编号查询
     */
    @GetMapping("/voyages/no/{voyageNo}")
    @Operation(summary = "根据航次编号查询", description = "根据航次编号查询航次详情")
    public Response<Voyage> getVoyageByNo(@PathVariable String voyageNo) {
        Voyage voyage = shippingService.getVoyageByNo(voyageNo);
        return Response.success(voyage);
    }

    /**
     * 分配柜子到航次
     */
    @PostMapping("/voyages/{voyageId}/containers/{containerId}")
    @Operation(summary = "分配柜子到航次", description = "将柜子分配到指定航次")
    public Response<Void> assignContainerToVoyage(
            @PathVariable Long voyageId,
            @PathVariable Long containerId) {
        shippingService.assignContainerToVoyage(containerId, voyageId);
        return Response.success();
    }

    /**
     * 开船
     */
    @PostMapping("/voyages/{voyageId}/depart")
    @Operation(summary = "开船", description = "确认航次开船")
    public Response<Integer> departVoyage(
            @PathVariable Long voyageId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime actualDeparture) {
        int count = shippingService.departVoyage(voyageId, actualDeparture);
        return Response.success(count);
    }

    /**
     * 到港
     */
    @PostMapping("/voyages/{voyageId}/arrive")
    @Operation(summary = "到港", description = "确认航次到港")
    public Response<Integer> arrivePort(
            @PathVariable Long voyageId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime actualArrival) {
        int count = shippingService.arrivePort(voyageId, actualArrival);
        return Response.success(count);
    }

    /**
     * 查询即将开船的航次
     */
    @GetMapping("/voyages/upcoming")
    @Operation(summary = "查询即将开船航次", description = "查询未来指定天数内即将开船的航次")
    public Response<List<Voyage>> getUpcomingVoyages(@RequestParam(defaultValue = "7") int days) {
        List<Voyage> voyages = shippingService.getUpcomingVoyages(days);
        return Response.success(voyages);
    }

    /**
     * 按状态查询航次
     */
    @GetMapping("/voyages/status/{status}")
    @Operation(summary = "按状态查询航次", description = "查询指定状态的航次列表")
    public Response<List<Voyage>> getVoyagesByStatus(@PathVariable String status) {
        List<Voyage> voyages = shippingService.getVoyagesByStatus(status);
        return Response.success(voyages);
    }

    /**
     * 获取航次统计
     */
    @GetMapping("/voyages/{voyageId}/statistics")
    @Operation(summary = "获取航次统计", description = "获取航次的统计信息")
    public Response<IShippingService.VoyageStatistics> getVoyageStatistics(@PathVariable Long voyageId) {
        IShippingService.VoyageStatistics stats = shippingService.getVoyageStatistics(voyageId);
        return Response.success(stats);
    }
}
