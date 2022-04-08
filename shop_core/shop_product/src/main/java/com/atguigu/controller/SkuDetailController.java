package com.atguigu.controller;

import com.atguigu.entity.SkuInfo;
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

    // 根据商品三级分类id查询商品分类信息

    // 根据skuId查询商品实时价格

    // 获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)

    // 获取skuId与销售属性组合的一个映射关系


}
