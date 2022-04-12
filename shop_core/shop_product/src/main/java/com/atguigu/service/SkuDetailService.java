package com.atguigu.service;

import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;

import java.util.List;

/**
 * @author lucky845
 * @date 2022年04月08日
 */
public interface SkuDetailService {

    /**
     * 根据skuId查询商品基本信息
     *
     * @param skuId 商品skuId
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)
     *
     * @param productId 商品Id
     * @param skuId     商品skuId
     */
    List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(Long productId, Long skuId);
}
