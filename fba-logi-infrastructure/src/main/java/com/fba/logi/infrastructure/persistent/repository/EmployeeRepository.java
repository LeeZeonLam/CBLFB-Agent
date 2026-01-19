package com.fba.logi.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fba.logi.common.constants.Constants;
import com.fba.logi.domain.employee.model.entity.Employee;
import com.fba.logi.domain.employee.repository.IEmployeeRepository;
import com.fba.logi.infrastructure.persistent.dao.IEmployeeMapper;
import com.fba.logi.infrastructure.persistent.po.EmployeePO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工仓储实现
 */
@Repository
public class EmployeeRepository implements IEmployeeRepository {

    @Resource
    private IEmployeeMapper employeeMapper;

    @Override
    public Employee queryById(Long employeeId) {
        LambdaQueryWrapper<EmployeePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeePO::getEmployeeId, employeeId);
        EmployeePO po = employeeMapper.selectOne(wrapper);
        return convertToEntity(po);
    }

    @Override
    public Employee queryByEmployeeNo(String employeeNo) {
        LambdaQueryWrapper<EmployeePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeePO::getEmployeeNo, employeeNo);
        EmployeePO po = employeeMapper.selectOne(wrapper);
        return convertToEntity(po);
    }

    @Override
    public List<Employee> queryActiveEmployees() {
        LambdaQueryWrapper<EmployeePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeePO::getStatus, Constants.EmployeeStatus.ACTIVE);
        return employeeMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> queryByRole(String role) {
        LambdaQueryWrapper<EmployeePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeePO::getRole, role)
               .eq(EmployeePO::getStatus, Constants.EmployeeStatus.ACTIVE);
        return employeeMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> queryByDepartment(String department) {
        LambdaQueryWrapper<EmployeePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeePO::getDepartment, department)
               .eq(EmployeePO::getStatus, Constants.EmployeeStatus.ACTIVE);
        return employeeMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> queryAll() {
        return employeeMapper.selectList(null).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveEmployee(Employee employee) {
        EmployeePO po = convertToPO(employee);
        po.setCreateTime(LocalDateTime.now());
        po.setUpdateTime(LocalDateTime.now());
        employeeMapper.insert(po);
    }

    @Override
    public void updateEmployee(Employee employee) {
        LambdaQueryWrapper<EmployeePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeePO::getEmployeeId, employee.getEmployeeId());
        EmployeePO po = convertToPO(employee);
        po.setUpdateTime(LocalDateTime.now());
        employeeMapper.update(po, wrapper);
    }

    @Override
    public void updateEmployeeStatus(Long employeeId, String status) {
        employeeMapper.updateStatus(employeeId, status);
    }

    @Override
    public void deleteEmployee(Long employeeId) {
        LambdaQueryWrapper<EmployeePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeePO::getEmployeeId, employeeId);
        employeeMapper.delete(wrapper);
    }

    @Override
    public String generateEmployeeNo() {
        return employeeMapper.generateEmployeeNo();
    }

    @Override
    public List<Employee> queryCustomerServiceStaff() {
        return queryByRole(Constants.EmployeeRole.CUSTOMER_SERVICE);
    }

    @Override
    public List<Employee> queryOperators() {
        return queryByRole(Constants.EmployeeRole.OPERATOR);
    }

    @Override
    public List<Employee> queryFinanceStaff() {
        return queryByRole(Constants.EmployeeRole.FINANCE);
    }

    @Override
    public List<Employee> queryWarehouseStaff() {
        return queryByRole(Constants.EmployeeRole.WAREHOUSE_STAFF);
    }

    // ==================== 转换方法 ====================

    private Employee convertToEntity(EmployeePO po) {
        if (po == null) {
            return null;
        }
        return Employee.builder()
                .employeeId(po.getEmployeeId())
                .employeeNo(po.getEmployeeNo())
                .name(po.getName())
                .role(po.getRole())
                .department(po.getDepartment())
                .phone(po.getPhone())
                .email(po.getEmail())
                .status(po.getStatus())
                .remark(po.getRemark())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private EmployeePO convertToPO(Employee entity) {
        if (entity == null) {
            return null;
        }
        return EmployeePO.builder()
                .employeeId(entity.getEmployeeId())
                .employeeNo(entity.getEmployeeNo())
                .name(entity.getName())
                .role(entity.getRole())
                .department(entity.getDepartment())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
