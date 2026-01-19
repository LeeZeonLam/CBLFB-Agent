package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.CustomsDeclarationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 报关单 Mapper
 */
@Mapper
public interface ICustomsDeclarationMapper extends BaseMapper<CustomsDeclarationPO> {

    /**
     * 更新报关单状态
     */
    @Update("UPDATE customs_declaration SET status = #{status}, update_time = NOW() WHERE declaration_id = #{declarationId}")
    int updateStatus(@Param("declarationId") Long declarationId, @Param("status") String status);

    /**
     * 生成报关单号
     */
    @Select("SELECT CONCAT('CD', TO_CHAR(NOW(), 'YYYYMMDD'), LPAD(COALESCE(MAX(CAST(SUBSTRING(declaration_no, 11) AS INTEGER)), 0) + 1, 4, '0')) " +
            "FROM customs_declaration WHERE declaration_no LIKE CONCAT('CD', TO_CHAR(NOW(), 'YYYYMMDD'), '%')")
    String generateDeclarationNo();

    /**
     * 更新放行时间
     */
    @Update("UPDATE customs_declaration SET status = 'cleared', cleared_time = NOW(), update_time = NOW() WHERE declaration_id = #{declarationId}")
    int clearDeclaration(@Param("declarationId") Long declarationId);
}
