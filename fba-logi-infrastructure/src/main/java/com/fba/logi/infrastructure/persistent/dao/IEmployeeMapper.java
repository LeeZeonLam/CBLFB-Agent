package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.EmployeePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 员工 Mapper
 */
@Mapper
public interface IEmployeeMapper extends BaseMapper<EmployeePO> {

    /**
     * 更新员工状态
     */
    @Update("UPDATE employee SET status = #{status}, update_time = NOW() WHERE employee_id = #{employeeId}")
    int updateStatus(@Param("employeeId") Long employeeId, @Param("status") String status);

    /**
     * 生成员工编号
     */
    @Select("SELECT CONCAT('E', TO_CHAR(NOW(), 'YYYYMMDD'), LPAD(COALESCE(MAX(CAST(SUBSTRING(employee_no, 9) AS INTEGER)), 0) + 1, 4, '0')) " +
            "FROM employee WHERE employee_no LIKE CONCAT('E', TO_CHAR(NOW(), 'YYYYMMDD'), '%')")
    String generateEmployeeNo();
}
