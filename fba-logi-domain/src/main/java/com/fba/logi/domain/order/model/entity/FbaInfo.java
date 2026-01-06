package com.fba.logi.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * FBA 信息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbaInfo {

    /**
     * FBA 信息 ID
     */
    private Long fbaInfoId;

    /**
     * 关联订单 ID
     */
    private Long orderId;

    /**
     * FBA Shipment ID
     */
    private String shipmentId;

    /**
     * FBA 仓库代码
     */
    private String warehouseCode;

    /**
     * FBA 仓库名称
     */
    private String warehouseName;

    /**
     * FBA 仓库地址
     */
    private String warehouseAddress;

    /**
     * FBA 仓库城市
     */
    private String warehouseCity;

    /**
     * FBA 仓库州/省
     */
    private String warehouseState;

    /**
     * FBA 仓库邮编
     */
    private String warehouseZipCode;

    /**
     * FBA 仓库国家
     */
    private String warehouseCountry;

    /**
     * 卖家账号
     */
    private String sellerAccount;

    /**
     * 预约送货日期
     */
    private LocalDate appointmentDate;

    /**
     * 送货窗口（上午/下午）
     */
    private String deliveryWindow;

    /**
     * 参考号
     */
    private String referenceId;

    /**
     * PO 号
     */
    private String poNumber;

    /**
     * PRO 号
     */
    private String proNumber;

    /**
     * BOL 号
     */
    private String bolNumber;

    /**
     * 是否需要预约
     */
    private Boolean needAppointment;

    /**
     * 预约状态
     */
    private String appointmentStatus;

    /**
     * 是否已创建 Shipment
     */
    private Boolean shipmentCreated;

    /**
     * 标签类型：原厂标签/转标签
     */
    private String labelType;

    /**
     * 验证 FBA 信息完整性
     */
    public boolean isComplete() {
        return shipmentId != null && !shipmentId.isBlank()
                && warehouseCode != null && !warehouseCode.isBlank()
                && warehouseAddress != null && !warehouseAddress.isBlank();
    }

    /**
     * 判断是否需要预约
     */
    public boolean requiresAppointment() {
        return Boolean.TRUE.equals(needAppointment);
    }

    /**
     * 获取完整仓库地址
     */
    public String getFullWarehouseAddress() {
        StringBuilder sb = new StringBuilder();
        if (warehouseAddress != null) {
            sb.append(warehouseAddress);
        }
        if (warehouseCity != null) {
            sb.append(", ").append(warehouseCity);
        }
        if (warehouseState != null) {
            sb.append(", ").append(warehouseState);
        }
        if (warehouseZipCode != null) {
            sb.append(" ").append(warehouseZipCode);
        }
        if (warehouseCountry != null) {
            sb.append(", ").append(warehouseCountry);
        }
        return sb.toString();
    }

}
