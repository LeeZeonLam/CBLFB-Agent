package com.fba.logi.chat.controller;

import com.fba.logi.common.response.Response;
import com.fba.logi.domain.order.model.entity.ShipmentOrder;
import com.fba.logi.domain.order.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "订单接口", description = "订单管理接口")
public class OrderController {

    private final IOrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    @Operation(summary = "创建订单", description = "创建新的发货订单")
    public Response<Long> createOrder(@RequestBody ShipmentOrder order) {
        Long orderId = orderService.createOrder(order);
        return Response.success(orderId);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "查询订单详情", description = "根据订单 ID 查询订单详情")
    public Response<ShipmentOrder> getOrderDetail(@PathVariable Long orderId) {
        ShipmentOrder order = orderService.getOrderDetail(orderId);
        return Response.success(order);
    }

    /**
     * 根据订单号查询订单
     */
    @GetMapping("/no/{orderNo}")
    @Operation(summary = "根据订单号查询", description = "根据订单号查询订单详情")
    public Response<ShipmentOrder> getOrderByNo(@PathVariable String orderNo) {
        ShipmentOrder order = orderService.getOrderByNo(orderNo);
        return Response.success(order);
    }

    /**
     * 查询客户订单
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "查询客户订单", description = "查询指定客户的所有订单")
    public Response<List<ShipmentOrder>> getCustomerOrders(@PathVariable Long customerId) {
        List<ShipmentOrder> orders = orderService.getCustomerOrders(customerId);
        return Response.success(orders);
    }

    /**
     * 查询指定状态的订单
     */
    @GetMapping("/state/{state}")
    @Operation(summary = "按状态查询订单", description = "查询指定状态的订单列表")
    public Response<List<ShipmentOrder>> getOrdersByState(@PathVariable String state) {
        List<ShipmentOrder> orders = orderService.getOrdersByState(state);
        return Response.success(orders);
    }

    /**
     * 提交订单
     */
    @PostMapping("/{orderId}/submit")
    @Operation(summary = "提交订单", description = "提交订单进入审核流程")
    public Response<Void> submitOrder(@PathVariable Long orderId) {
        orderService.submitOrder(orderId);
        return Response.success();
    }

    /**
     * 审核订单
     */
    @PostMapping("/{orderId}/audit")
    @Operation(summary = "审核订单", description = "审核订单（通过或拒绝）")
    public Response<Void> auditOrder(
            @PathVariable Long orderId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String reason) {
        orderService.auditOrder(orderId, approved, reason);
        return Response.success();
    }

    /**
     * 货物入仓
     */
    @PostMapping("/{orderId}/receive")
    @Operation(summary = "货物入仓", description = "确认订单货物已入仓")
    public Response<Void> receiveOrder(@PathVariable Long orderId) {
        orderService.receiveOrder(orderId);
        return Response.success();
    }

    /**
     * 完成材积录入
     */
    @PostMapping("/{orderId}/dimension-recorded")
    @Operation(summary = "完成材积录入", description = "确认订单材积录入完成")
    public Response<Void> completeDimensionRecord(@PathVariable Long orderId) {
        orderService.completeDimensionRecord(orderId);
        return Response.success();
    }

    /**
     * 开始派送
     */
    @PostMapping("/{orderId}/start-delivery")
    @Operation(summary = "开始派送", description = "订单开始派送")
    public Response<Void> startDelivery(
            @PathVariable Long orderId,
            @RequestParam String deliveryMethod,
            @RequestParam(required = false) String trackingNo) {
        orderService.startDelivery(orderId, deliveryMethod, trackingNo);
        return Response.success();
    }

    /**
     * 完成订单
     */
    @PostMapping("/{orderId}/complete")
    @Operation(summary = "完成订单", description = "确认订单已签收完成")
    public Response<Void> completeOrder(
            @PathVariable Long orderId,
            @RequestParam String signedBy) {
        orderService.completeOrder(orderId, signedBy);
        return Response.success();
    }
}
