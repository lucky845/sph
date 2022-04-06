package com.atguigu.controller;


import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.result.RetVal;
import com.atguigu.service.ProductSalePropertyKeyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 库存单元表 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
@Api(tags = "商品SKU信息")
@RestController
@RequestMapping("/product")
public class SkuController {

    @Resource
    private ProductSalePropertyKeyService salePropertyKeyService;

    /**
     * 根据SPUId查询销售属性
     *
     * @param spuId 商品SPUId
     */
    @ApiOperation("根据SPUId查询销售属性")
    @GetMapping("/querySalePropertyByProductId/{spuId}")
    public RetVal<List<ProductSalePropertyKey>> querySalePropertyByProductId(
            @ApiParam(name = "spuId", value = "商品id", required = true)
            @PathVariable Long spuId
    ) {
        List<ProductSalePropertyKey> salePropertyKeyList = salePropertyKeyService.querySalePropertyByProductId(spuId);
        return RetVal.ok(salePropertyKeyList);
    }

}

