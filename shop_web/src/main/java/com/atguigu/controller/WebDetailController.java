package com.atguigu.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.client.ProductFeignClient;
import com.atguigu.entity.BaseCategoryView;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lucky845
 * @date 2022年04月07日 13:37
 */
@Controller
public class WebDetailController {

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private ThreadPoolExecutor myExecutor;

    /**
     * 商品详情信息(异步执行)
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("商品详情信息")
    @RequestMapping("{skuId}.html")
    public String getSkuDetail(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId,

            Model model
    ) {
        // 封装返回的参数
        Map<String, Object> dataMap = new HashMap<>();

        // 发起一个异步请求
        CompletableFuture<Void> priceFuture = CompletableFuture.runAsync(() -> {
            // 3. 根据skuId获取商品的实时价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            dataMap.put("price", skuPrice);
        }, myExecutor);

        // 1. 根据skuId查询商品的基本信息 发起一个异步请求 有返回值
        CompletableFuture<SkuInfo> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            dataMap.put("skuInfo", skuInfo);
            return skuInfo;
        }, myExecutor);

        // 2. 根据三级分类id获取商品的分类信息
        CompletableFuture<Void> categoryViewFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            Long category3Id = skuInfo.getCategory3Id();
            BaseCategoryView categoryView = productFeignClient.getCategoryView(category3Id);
            dataMap.put("categoryView", categoryView);
        }, myExecutor);

        // 4. 获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)
        CompletableFuture<Void> salePropertyFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            Long productId = skuInfo.getProductId();
            List<ProductSalePropertyKey> spuSalePropertyList = productFeignClient.getSpuSalePropertyAndSelected(productId, skuId);
            dataMap.put("spuSalePropertyList", spuSalePropertyList);
        }, myExecutor);

        CompletableFuture<Void> salePropertyValueIdJsonFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            Long productId = skuInfo.getProductId();
            //5. 获取skuId与销售属性组合的一个映射关系
            Map<Object, Object> salePropertyValueIdJson = productFeignClient.getSalePropertyAndSkuIdMapping(productId);
            dataMap.put("salePropertyValueIdJson", JSON.toJSONString(salePropertyValueIdJson));
        }, myExecutor);

        // 所有任务都执行完了 才返回商品详情页面
        CompletableFuture.allOf(
                priceFuture,
                skuInfoFuture,
                categoryViewFuture,
                salePropertyFuture,
                salePropertyValueIdJsonFuture
        ).join();

        model.addAllAttributes(dataMap);
        return "detail/index";
    }

    /**
     * 商品详情信息(同步执行)
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("商品详情信息")
    //@RequestMapping("{skuId}.html")
    public String getSkuDetail1(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId,

            Model model
    ) {
        // 1. 根据skuId查询商品的基本信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 2. 根据skuId查询商品实时价格
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        // 3. 根据三级分类id获取商品的分类信息
        Long category3Id = skuInfo.getCategory3Id();
        BaseCategoryView categoryView = productFeignClient.getCategoryView(category3Id);
        // 4. 获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)
        Long productId = skuInfo.getProductId();
        List<ProductSalePropertyKey> spuSalePropertyList = productFeignClient.getSpuSalePropertyAndSelected(productId, skuId);
        // 5.获取skuId与销售属性组合的一个映射关系
        Map<Object, Object> salePropertyValueIdJson = productFeignClient.getSalePropertyAndSkuIdMapping(productId);
        // 填充参数
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("skuInfo", skuInfo);
        dataMap.put("price", skuPrice);
        dataMap.put("categoryView", categoryView);
        dataMap.put("spuSalePropertyList", spuSalePropertyList);
        dataMap.put("salePropertyValueIdJson", JSON.toJSONString(salePropertyValueIdJson));
        model.addAllAttributes(dataMap);
        return "detail/index";
    }
}
