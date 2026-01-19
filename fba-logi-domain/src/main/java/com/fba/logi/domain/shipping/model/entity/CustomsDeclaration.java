package com.fba.logi.domain.shipping.model.entity;

import com.fba.logi.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 报关单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomsDeclaration {

    /**
     * 报关单 ID
     */
    private Long declarationId;

    /**
     * 报关单号
     */
    private String declarationNo;

    /**
     * 关联柜子 ID
     */
    private Long containerId;

    /**
     * 关联柜子号
     */
    private String containerNo;

    /**
     * 关联航次 ID
     */
    private Long voyageId;

    /**
     * 报关类型：EXPORT(出口)/IMPORT(进口)
     */
    private String declarationType;

    /**
     * 报关口岸
     */
    private String customsPort;

    /**
     * 报关行名称
     */
    private String brokerName;

    /**
     * 报关行联系人
     */
    private String brokerContact;

    /**
     * 报关行电话
     */
    private String brokerPhone;

    /**
     * 报关状态：PENDING/PROCESSING/CLEARED/INSPECTING/REJECTED
     */
    private String status;

    /**
     * 申报总金额（USD）
     */
    private BigDecimal declaredValue;

    /**
     * 申报币种
     */
    private String currency;

    /**
     * 申报总重量（KG）
     */
    private BigDecimal declaredWeight;

    /**
     * 申报总件数
     */
    private Integer declaredPieces;

    /**
     * HS编码（多个用逗号分隔）
     */
    private String hsCodes;

    /**
     * 商品描述
     */
    private String goodsDescription;

    /**
     * 报关文件URL（JSON格式存储多个文件）
     */
    private String documentUrls;

    /**
     * 查验原因（如果被查验）
     */
    private String inspectionReason;

    /**
     * 退回原因（如果被退回）
     */
    private String rejectReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 申报时间
     */
    private LocalDateTime declaredTime;

    /**
     * 放行时间
     */
    private LocalDateTime clearedTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ==================== 状态转换规则 ====================

    private static final Map<String, Set<String>> STATE_TRANSITIONS = Map.ofEntries(
            Map.entry(Constants.CustomsStatus.PENDING, Set.of(Constants.CustomsStatus.PROCESSING)),
            Map.entry(Constants.CustomsStatus.PROCESSING, Set.of(Constants.CustomsStatus.CLEARED, Constants.CustomsStatus.INSPECTING, Constants.CustomsStatus.REJECTED)),
            Map.entry(Constants.CustomsStatus.INSPECTING, Set.of(Constants.CustomsStatus.CLEARED, Constants.CustomsStatus.REJECTED))
    );

    // ==================== 业务方法 ====================

    /**
     * 检查状态转换是否合法
     */
    public boolean canTransitionTo(String targetState) {
        Set<String> allowedStates = STATE_TRANSITIONS.get(this.status);
        return allowedStates != null && allowedStates.contains(targetState);
    }

    /**
     * 执行状态转换
     */
    public void transitionTo(String targetState) {
        if (!canTransitionTo(targetState)) {
            throw new IllegalStateException(
                    String.format("不允许从状态 [%s] 转换到 [%s]", this.status, targetState));
        }
        this.status = targetState;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 开始报关（PENDING → PROCESSING）
     */
    public void startProcessing() {
        transitionTo(Constants.CustomsStatus.PROCESSING);
        this.declaredTime = LocalDateTime.now();
    }

    /**
     * 报关放行（PROCESSING/INSPECTING → CLEARED）
     */
    public void clear() {
        transitionTo(Constants.CustomsStatus.CLEARED);
        this.clearedTime = LocalDateTime.now();
    }

    /**
     * 进入查验（PROCESSING → INSPECTING）
     */
    public void inspect(String reason) {
        transitionTo(Constants.CustomsStatus.INSPECTING);
        this.inspectionReason = reason;
    }

    /**
     * 报关退回（PROCESSING/INSPECTING → REJECTED）
     */
    public void reject(String reason) {
        transitionTo(Constants.CustomsStatus.REJECTED);
        this.rejectReason = reason;
    }

    /**
     * 判断是否已放行
     */
    public boolean isCleared() {
        return Constants.CustomsStatus.CLEARED.equals(status);
    }

    /**
     * 判断是否在查验中
     */
    public boolean isInspecting() {
        return Constants.CustomsStatus.INSPECTING.equals(status);
    }

    /**
     * 判断是否被退回
     */
    public boolean isRejected() {
        return Constants.CustomsStatus.REJECTED.equals(status);
    }

    /**
     * 获取状态中文描述
     */
    public String getStatusDescription() {
        return switch (status) {
            case "pending" -> "待报关";
            case "processing" -> "报关中";
            case "cleared" -> "已放行";
            case "inspecting" -> "查验中";
            case "rejected" -> "已退回";
            default -> status;
        };
    }
}
