package com.fba.logi.domain.order.repository;

import com.fba.logi.domain.order.model.entity.Product;

import java.util.List;

/**
 * 产品仓储接口
 */
public interface IProductRepository {

    /**
     * 根据 ID 查询产品
     */
    Product queryById(Long productId);

    /**
     * 根据 SKU 查询产品
     */
    Product queryBySku(String sku);

    /**
     * 根据 ASIN 查询产品列表
     */
    List<Product> queryByAsin(String asin);

    /**
     * 根据 FNSKU 查询产品
     */
    Product queryByFnsku(String fnsku);

    /**
     * 查询所有活跃产品
     */
    List<Product> queryActiveProducts();

    /**
     * 搜索产品（按SKU或名称）
     */
    List<Product> searchProducts(String keyword, int limit);

    /**
     * 保存产品
     */
    void save(Product product);

    /**
     * 更新产品
     */
    void update(Product product);

    /**
     * 更新产品状态
     */
    void updateStatus(Long productId, String status);

    /**
     * 删除产品
     */
    void delete(Long productId);

    /**
     * 生成产品ID
     */
    Long generateProductId();

    // ==================== 订单产品关联操作 ====================

    /**
     * 查询订单的产品列表
     */
    List<Product> queryByOrderId(Long orderId);

    /**
     * 保存订单产品关联
     */
    void saveOrderProducts(Long orderId, List<Product> products);

    /**
     * 删除订单的所有产品关联
     */
    void deleteOrderProducts(Long orderId);

    /**
     * 统计订单产品数量
     */
    Integer countOrderProducts(Long orderId);
}
