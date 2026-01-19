package com.fba.logi.domain.employee.repository;

import com.fba.logi.domain.employee.model.entity.Employee;

import java.util.List;

/**
 * 员工仓储接口
 */
public interface IEmployeeRepository {

    /**
     * 根据 ID 查询员工
     */
    Employee queryById(Long employeeId);

    /**
     * 根据编号查询员工
     */
    Employee queryByEmployeeNo(String employeeNo);

    /**
     * 查询所有活跃员工
     */
    List<Employee> queryActiveEmployees();

    /**
     * 根据角色查询员工
     */
    List<Employee> queryByRole(String role);

    /**
     * 根据部门查询员工
     */
    List<Employee> queryByDepartment(String department);

    /**
     * 查询所有员工
     */
    List<Employee> queryAll();

    /**
     * 保存员工
     */
    void saveEmployee(Employee employee);

    /**
     * 更新员工信息
     */
    void updateEmployee(Employee employee);

    /**
     * 更新员工状态
     */
    void updateEmployeeStatus(Long employeeId, String status);

    /**
     * 删除员工
     */
    void deleteEmployee(Long employeeId);

    /**
     * 生成员工编号
     */
    String generateEmployeeNo();

    /**
     * 查询所有客服
     */
    List<Employee> queryCustomerServiceStaff();

    /**
     * 查询所有操作员
     */
    List<Employee> queryOperators();

    /**
     * 查询所有财务人员
     */
    List<Employee> queryFinanceStaff();

    /**
     * 查询所有仓库员工
     */
    List<Employee> queryWarehouseStaff();
}
