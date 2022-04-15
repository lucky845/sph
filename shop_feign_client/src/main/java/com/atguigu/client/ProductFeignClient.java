package com.atguigu.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.entity.*;
import com.atguigu.fallback.ProductFallback;
import com.atguigu.result.RetVal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author lucky845
 * @date 2022年04月06日
 */
@FeignClient(value = "shop-product", fallback = ProductFallback.class)
public interface ProductFeignClient {

    /**
     * 根据skuId查询商品的基本信息
     *
     * @param skuId 商品skuId
     */
    @GetMapping("/sku/getSkuInfo/{skuId}")
    SkuInfo getSkuInfo(@PathVariable long skuId);

    /**
     * 根据三级分类id获取商品的分类信息
     *
     * @param category3Id 三级分类id
     */
    @GetMapping("/sku/getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable long category3Id);

    /**
     * 根据skuId获取商品的实时价格
     *
     * @param skuId 商品skuId
     */
    @GetMapping("/sku/getSkuPrice/{skuId}")
    BigDecimal getSkuPrice(@PathVariable long skuId);

    /**
     * 获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)
     *
     * @param productId 商品id
     * @param skuId     商品skuId
     */
    @GetMapping("/sku/getSpuSalePropertyAndSelected/{productId}/{skuId}")
    List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(@PathVariable long productId, @PathVariable long skuId);

    /**
     * 获取skuId与销售属性组合的一个映射关系
     *
     * @param productId 商品id
     */
    @GetMapping("/sku/getSalePropertyAndSkuIdMapping/{productId}")
    Map<Object, Object> getSalePropertyAndSkuIdMapping(@PathVariable long productId);

    /**
     * 查询首页分类信息
     */
    @GetMapping("/product/getIndexCategory")
    public RetVal<List<JSONObject>> getIndexCategory();

    /**
     * 根据brandId查询品牌信息
     *
     * @param brandId 品牌id
     */
    @GetMapping("/product/brand/getBrandByBrandId/{brandId}")
    public BaseBrand getBrandByBrandId(@PathVariable Long brandId);

    /**
     * 根据商品skuId查询商品的平台信息
     *
     * @param skuId 商品skuId
     */
    @GetMapping("/product/getPlatformPropertyBySkuId/{skuId}")
    public List<PlatformPropertyKey> getPlatformPropertyBySkuId(@PathVariable Long skuId);

}
