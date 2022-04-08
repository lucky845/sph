package com.atguigu.controller;

import com.atguigu.entity.BaseCategoryView;
import com.atguigu.entity.SkuInfo;
import com.atguigu.service.BaseCategoryViewService;
import com.atguigu.service.SkuDetailService;
import com.atguigu.service.SkuInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author lucky845
 * @date 2022年04月08日
 */
@Api(tags = "商品详情")
@RequestMapping("/sku")
@RestController
public class SkuDetailController {

    @Resource
    private SkuDetailService skuDetailService;

    @Resource
    private BaseCategoryViewService categoryViewService;

    @Resource
    private SkuInfoService skuInfoService;

    /**
     * 根据skuId查询商品基本信息
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("根据skuId查询商品基本信息")
    @GetMapping("/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId
    ) {
        return skuDetailService.getSkuInfo(skuId);
    }

    /**
     * 根据商品三级分类id查询商品分类信息
     *
     * @param category3Id 商品三级分类id
     */
    @ApiOperation("根据三级分类id查询商品分类信息")
    @GetMapping("/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(
            @ApiParam(name = "category3Id", value = "商品三级分类id", required = true)
            @PathVariable Long category3Id
    ) {
        return categoryViewService.getById(category3Id);
    }

    /**
     * 根据skuId查询商品实时价格
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("根据skuId查询商品实时价格")
    @GetMapping("/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId
    ) {
        SkuInfo skuInfo = skuInfoService.getById(skuId);
        return skuInfo.getPrice();
    }

    // 获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)

    // 获取skuId与销售属性组合的一个映射关系


}
