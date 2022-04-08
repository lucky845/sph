package com.atguigu.fallback;

import com.atguigu.client.ProductFeignClient;
import com.atguigu.entity.BaseCategoryView;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lucky845
 * @date 2022年04月06日
 */
@Component
public class ProductFallback implements ProductFeignClient {


    /**
     * 根据skuId查询商品的基本信息
     *
     * @param skuId 商品skuId
     */
    @Override
    public SkuInfo getSkuInfo(long skuId) {
        return new SkuInfo();
    }

    /**
     * 根据三级分类id获取商品的分类信息
     *
     * @param category3Id 三级分类id
     */
    @Override
    public BaseCategoryView getCategoryView(long category3Id) {
        return new BaseCategoryView();
    }

    /**
     * 根据skuId获取商品的实时价格
     *
     * @param skuId 商品skuId
     */
    @Override
    public BigDecimal getSkuPrice(long skuId) {
        return new BigDecimal(0);
    }

    /**
     * 获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)
     *
     * @param productId 商品id
     * @param skuId     商品skuId
     */
    @Override
    public List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(long productId, long skuId) {
        return new ArrayList<>();
    }

    /**
     * 获取skuId与销售属性组合的一个映射关系
     *
     * @param productId 商品id
     */
    @Override
    public Map<Object, Object> getSalePropertyAndSkuIdMapping(long productId) {
        return new HashMap<>();
    }
}
