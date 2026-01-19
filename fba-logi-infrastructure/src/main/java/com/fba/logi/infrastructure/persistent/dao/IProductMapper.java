package com.fba.logi.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fba.logi.infrastructure.persistent.po.ProductPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 产品 Mapper
 */
@Mapper
public interface IProductMapper extends BaseMapper<ProductPO> {

    /**
     * 根据SKU查询产品
     */
    @Select("SELECT * FROM product WHERE sku = #{sku}")
    ProductPO selectBySku(@Param("sku") String sku);

    /**
     * 根据ASIN查询产品
     */
    @Select("SELECT * FROM product WHERE asin = #{asin}")
    List<ProductPO> selectByAsin(@Param("asin") String asin);

    /**
     * 根据FNSKU查询产品
     */
    @Select("SELECT * FROM product WHERE fnsku = #{fnsku}")
    ProductPO selectByFnsku(@Param("fnsku") String fnsku);

    /**
     * 更新产品状态
     */
    @Update("UPDATE product SET status = #{status}, update_time = NOW() WHERE product_id = #{productId}")
    int updateStatus(@Param("productId") Long productId, @Param("status") String status);

    /**
     * 生成产品ID
     */
    @Select("SELECT COALESCE(MAX(product_id), 0) + 1 FROM product")
    Long generateProductId();

    /**
     * 搜索产品（按SKU或名称模糊匹配）
     */
    @Select("SELECT * FROM product WHERE status = 'active' AND (sku ILIKE CONCAT('%', #{keyword}, '%') OR product_name ILIKE CONCAT('%', #{keyword}, '%')) LIMIT #{limit}")
    List<ProductPO> searchProducts(@Param("keyword") String keyword, @Param("limit") int limit);
}
