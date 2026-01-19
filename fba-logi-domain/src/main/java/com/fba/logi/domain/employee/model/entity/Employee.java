package com.fba.logi.domain.employee.model.entity;

import com.fba.logi.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 员工实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    /**
     * 员工 ID
     */
    private Long employeeId;

    /**
     * 员工编号
     */
    private String employeeNo;

    /**
     * 员工姓名
     */
    private String name;

    /**
     * 员工角色：CUSTOMER_SERVICE/OPERATOR/FINANCE/WAREHOUSE_STAFF/MANAGER
     */
    private String role;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态：ACTIVE/INACTIVE
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    // ==================== 业务方法 ====================

    /**
     * 判断员工是否活跃
     */
    public boolean isActive() {
        return Constants.EmployeeStatus.ACTIVE.equals(status);
    }

    /**
     * 激活员工
     */
    public void activate() {
        this.status = Constants.EmployeeStatus.ACTIVE;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 停用员工
     */
    public void deactivate() {
        this.status = Constants.EmployeeStatus.INACTIVE;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断是否为客服
     */
    public boolean isCustomerService() {
        return Constants.EmployeeRole.CUSTOMER_SERVICE.equals(role);
    }

    /**
     * 判断是否为操作员
     */
    public boolean isOperator() {
        return Constants.EmployeeRole.OPERATOR.equals(role);
    }

    /**
     * 判断是否为财务
     */
    public boolean isFinance() {
        return Constants.EmployeeRole.FINANCE.equals(role);
    }

    /**
     * 判断是否为仓库员工
     */
    public boolean isWarehouseStaff() {
        return Constants.EmployeeRole.WAREHOUSE_STAFF.equals(role);
    }

    /**
     * 判断是否为管理员
     */
    public boolean isManager() {
        return Constants.EmployeeRole.MANAGER.equals(role);
    }

    /**
     * 获取角色中文描述
     */
    public String getRoleDescription() {
        return switch (role) {
            case "customer_service" -> "客服";
            case "operator" -> "操作员";
            case "finance" -> "财务";
            case "warehouse_staff" -> "仓库员工";
            case "manager" -> "管理员";
            default -> role;
        };
    }

    /**
     * 获取显示名称（姓名 + 角色）
     */
    public String getDisplayName() {
        return name + " (" + getRoleDescription() + ")";
    }
}
