package com.fba.logi.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fba.logi.domain.order.model.entity.Product;
import com.fba.logi.domain.order.repository.IProductRepository;
import com.fba.logi.infrastructure.persistent.dao.IOrderProductMapper;
import com.fba.logi.infrastructure.persistent.dao.IProductMapper;
import com.fba.logi.infrastructure.persistent.po.OrderProductPO;
import com.fba.logi.infrastructure.persistent.po.ProductPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品仓储实现
 */
@Repository
public class ProductRepository implements IProductRepository {

    @Resource
    private IProductMapper productMapper;

    @Resource
    private IOrderProductMapper orderProductMapper;

    @Override
    public Product queryById(Long productId) {
        LambdaQueryWrapper<ProductPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductPO::getProductId, productId);
        ProductPO po = productMapper.selectOne(wrapper);
        return convertToEntity(po);
    }

    @Override
    public Product queryBySku(String sku) {
        ProductPO po = productMapper.selectBySku(sku);
        return convertToEntity(po);
    }

    @Override
    public List<Product> queryByAsin(String asin) {
        return productMapper.selectByAsin(asin).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Product queryByFnsku(String fnsku) {
        ProductPO po = productMapper.selectByFnsku(fnsku);
        return convertToEntity(po);
    }

    @Override
    public List<Product> queryActiveProducts() {
        LambdaQueryWrapper<ProductPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductPO::getStatus, "active");
        return productMapper.selectList(wrapper).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> searchProducts(String keyword, int limit) {
        return productMapper.searchProducts(keyword, limit).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Product product) {
        ProductPO po = convertToPO(product);
        po.setCreateTime(LocalDateTime.now());
        po.setUpdateTime(LocalDateTime.now());
        productMapper.insert(po);
    }

    @Override
    public void update(Product product) {
        LambdaQueryWrapper<ProductPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductPO::getProductId, product.getProductId());
        ProductPO po = convertToPO(product);
        po.setUpdateTime(LocalDateTime.now());
        productMapper.update(po, wrapper);
    }

    @Override
    public void updateStatus(Long productId, String status) {
        productMapper.updateStatus(productId, status);
    }

    @Override
    public void delete(Long productId) {
        LambdaQueryWrapper<ProductPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductPO::getProductId, productId);
        productMapper.delete(wrapper);
    }

    @Override
    public Long generateProductId() {
        return productMapper.generateProductId();
    }

    // ==================== 订单产品关联操作 ====================

    @Override
    public List<Product> queryByOrderId(Long orderId) {
        return orderProductMapper.selectByOrderId(orderId).stream()
                .map(this::convertOrderProductToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveOrderProducts(Long orderId, List<Product> products) {
        for (Product product : products) {
            OrderProductPO po = convertToOrderProductPO(orderId, product);
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
            orderProductMapper.insert(po);
        }
    }

    @Override
    public void deleteOrderProducts(Long orderId) {
        orderProductMapper.deleteByOrderId(orderId);
    }

    @Override
    public Integer countOrderProducts(Long orderId) {
        return orderProductMapper.countProductsByOrderId(orderId);
    }

    // ==================== 转换方法 ====================

    private Product convertToEntity(ProductPO po) {
        if (po == null) {
            return null;
        }
        return Product.builder()
                .productId(po.getProductId())
                .sku(po.getSku())
                .asin(po.getAsin())
                .fnsku(po.getFnsku())
                .productName(po.getProductName())
                .productNameEn(po.getProductNameEn())
                .description(po.getDescription())
                .hsCode(po.getHsCode())
                .unitWeight(po.getUnitWeight())
                .length(po.getLength())
                .width(po.getWidth())
                .height(po.getHeight())
                .unitPrice(po.getUnitPrice())
                .currency(po.getCurrency())
                .hasBattery(po.getHasBattery())
                .batteryType(po.getBatteryType())
                .isLiquid(po.getIsLiquid())
                .isPowder(po.getIsPowder())
                .isMagnetic(po.getIsMagnetic())
                .isDangerous(po.getIsDangerous())
                .originCountry(po.getOriginCountry())
                .build();
    }

    private ProductPO convertToPO(Product entity) {
        if (entity == null) {
            return null;
        }
        return ProductPO.builder()
                .productId(entity.getProductId())
                .sku(entity.getSku())
                .asin(entity.getAsin())
                .fnsku(entity.getFnsku())
                .productName(entity.getProductName())
                .productNameEn(entity.getProductNameEn())
                .description(entity.getDescription())
                .hsCode(entity.getHsCode())
                .unitWeight(entity.getUnitWeight())
                .length(entity.getLength())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .unitPrice(entity.getUnitPrice())
                .currency(entity.getCurrency())
                .hasBattery(entity.getHasBattery())
                .batteryType(entity.getBatteryType())
                .isLiquid(entity.getIsLiquid())
                .isPowder(entity.getIsPowder())
                .isMagnetic(entity.getIsMagnetic())
                .isDangerous(entity.getIsDangerous())
                .originCountry(entity.getOriginCountry())
                .status("active")
                .build();
    }

    private Product convertOrderProductToEntity(OrderProductPO po) {
        if (po == null) {
            return null;
        }
        return Product.builder()
                .productId(po.getProductId())
                .sku(po.getSku())
                .productName(po.getProductName())
                .quantity(po.getQuantity())
                .unitPrice(po.getUnitPrice())
                .currency(po.getCurrency())
                .unitWeight(po.getUnitWeight())
                .length(po.getLength())
                .width(po.getWidth())
                .height(po.getHeight())
                .hasBattery(po.getHasBattery())
                .isLiquid(po.getIsLiquid())
                .isPowder(po.getIsPowder())
                .isMagnetic(po.getIsMagnetic())
                .hsCode(po.getHsCode())
                .build();
    }

    private OrderProductPO convertToOrderProductPO(Long orderId, Product entity) {
        if (entity == null) {
            return null;
        }
        return OrderProductPO.builder()
                .orderId(orderId)
                .productId(entity.getProductId())
                .sku(entity.getSku())
                .productName(entity.getProductName())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .currency(entity.getCurrency())
                .unitWeight(entity.getUnitWeight())
                .length(entity.getLength())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .hasBattery(entity.getHasBattery())
                .isLiquid(entity.getIsLiquid())
                .isPowder(entity.getIsPowder())
                .isMagnetic(entity.getIsMagnetic())
                .hsCode(entity.getHsCode())
                .build();
    }
}
