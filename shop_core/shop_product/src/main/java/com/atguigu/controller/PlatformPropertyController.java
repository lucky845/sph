package com.atguigu.controller;


import com.atguigu.entity.PlatformPropertyKey;
import com.atguigu.entity.PlatformPropertyValue;
import com.atguigu.result.RetVal;
import com.atguigu.service.PlatformPropertyKeyService;
import com.atguigu.service.PlatformPropertyValueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 属性表 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
@Api(tags = "平台属性")
@RestController
@RequestMapping("/product")
public class PlatformPropertyController {

    @Resource
    private PlatformPropertyKeyService propertyKeyService;

    @Resource
    private PlatformPropertyValueService propertyValueService;

    /**
     * 根据一二三级分类id获取平台属性信息
     *
     * @param category1Id 一级分类id
     * @param category2Id 二级分类id
     * @param category3Id 三级分类id
     */
    @GetMapping("/getPlatformPropertyByCategoryId/{category1Id}/{category2Id}/{category3Id}")
    public RetVal<List<PlatformPropertyKey>> getPlatformPropertyByCategoryId(
            @ApiParam(name = "category1Id", value = "一级分类id")
            @PathVariable Long category1Id,

            @ApiParam(name = "category2Id", value = "二级分类id")
            @PathVariable Long category2Id,

            @ApiParam(name = "category3Id", value = "三级分类id")
            @PathVariable Long category3Id
    ) {
        List<PlatformPropertyKey> platformPropertyKeyList = propertyKeyService.getPlatformPropertyByCategoryId(category1Id, category2Id, category3Id);
        return RetVal.ok(platformPropertyKeyList);
    }

    /**
     * 根据平台属性key获取平台属性值集合
     *
     * @param propertyKeyId 平台属性key
     */
    @ApiOperation("根据平台属性key获取平台属性值集合")
    @GetMapping("/getPropertyValueByPropertyKeyId/{propertyKeyId}")
    public RetVal<List<PlatformPropertyValue>> getPropertyValueByPropertyKeyId(
            @ApiParam(name = "propertyKeyId", value = "平台属性key", required = true)
            @PathVariable Long propertyKeyId
    ) {
        List<PlatformPropertyValue> propertyValueList =
                propertyValueService.getPlatformPropertyValueByKeyId(propertyKeyId);
        return RetVal.ok(propertyValueList);
    }

    /**
     * 保存修改平台属性key与value
     *
     * @param platformPropertyKey 平台属性key对象
     */
    @ApiOperation("保存修改平台属性key与value")
    @PostMapping("/savePlatformProperty")
    public RetVal<Object> savePlatformProperty(
            @ApiParam(name = "platformPropertyKey", value = "平台属性key对象", required = true)
            @RequestBody PlatformPropertyKey platformPropertyKey
    ) {
        propertyKeyService.savePlatformProperty(platformPropertyKey);
        return RetVal.ok();
    }

    /**
     * 根据商品skuId查询商品的平台信息
     *
     * @param skuId 商品销售属性Id
     */
    @GetMapping("/product/getPlatformPropertyBySkuId/{skuId}")
    public List<PlatformPropertyKey> getPlatformPropertyBySkuId(
            @ApiParam(name = "skuId", value = "商品销售属性Id", required = true)
            @PathVariable Long skuId
    ) {
        return propertyKeyService.getPlatformPropertyBySkuId(skuId);
    }

}

