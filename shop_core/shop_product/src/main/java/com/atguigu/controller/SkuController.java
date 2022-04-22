package com.atguigu.controller;


import com.atguigu.client.SearchFeignClient;
import com.atguigu.constant.MqConst;
import com.atguigu.entity.ProductImage;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;
import com.atguigu.result.RetVal;
import com.atguigu.service.ProductImageService;
import com.atguigu.service.ProductSalePropertyKeyService;
import com.atguigu.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

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

    @Resource
    private ProductImageService productImageService;

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SearchFeignClient searchFeignClient;

    @Resource
    private RabbitTemplate rabbitTemplate;

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

    /**
     * 根据SPUId查询图片信息
     *
     * @param spuId 商品SPUId
     */
    @ApiOperation("根据SPUId查询图片信息")
    @GetMapping("/queryProductImageByProductId/{spuId}")
    public RetVal<List<ProductImage>> queryProductImageByProductId(
            @ApiParam(name = "spuId", value = "商品SPUId", required = true)
            @PathVariable Long spuId
    ) {
        QueryWrapper<ProductImage> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", spuId);
        List<ProductImage> productImageList = productImageService.list(wrapper);
        return RetVal.ok(productImageList);
    }

    /**
     * 保存商品SKU信息
     *
     * @param skuInfo 商品SKU对象
     */
    @ApiOperation("保存SKU信息")
    @PostMapping("/saveSkuInfo")
    public RetVal<Object> saveSkuInfo(
            @ApiParam(name = "skuInfo", value = "商品SKU对象", required = true)
            @RequestBody SkuInfo skuInfo
    ) {
        skuInfoService.saveSkuInfo(skuInfo);
        return RetVal.ok();
    }

    /**
     * 查询SKU信息分页记录
     *
     * @param currentPageNum 当前页码
     * @param pageSize       每页记录数
     */
    @ApiOperation("查询SKU信息分页记录")
    @GetMapping("/querySkuInfoByPage/{currentPageNum}/{pageSize}")
    public RetVal<Page<SkuInfo>> querySkuInfoByPage(
            @ApiParam(name = "currentPageNum", value = "当前页码", required = true)
            @PathVariable Long currentPageNum,

            @ApiParam(name = "pageSize", value = "每页记录数", required = true)
            @PathVariable Long pageSize
    ) {
        Page<SkuInfo> page = new Page<>(currentPageNum, pageSize);
        skuInfoService.page(page, null);
        return RetVal.ok(page);
    }

    /**
     * 商品SKU上架
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("商品SKU上架")
    @GetMapping("/onSale/{skuId}")
    public RetVal<Object> onSale(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId
    ) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        // (1: 是，0：否)
        skuInfo.setIsSale(1);
        skuInfoService.updateById(skuInfo);
        // es上架(使用RabbitMQ)
        rabbitTemplate.convertAndSend(MqConst.ON_OFF_SALE_EXCHANGE, MqConst.ON_SALE_ROUTING_KEY, skuId);
        //searchFeignClient.onSale(skuId);
        return RetVal.ok();
    }

    /**
     * 商品SKU下架
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("商品SKU下架")
    @GetMapping("/offSale/{skuId}")
    public RetVal<Object> offSale(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId
    ) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        // (1: 是，0：否)
        skuInfo.setIsSale(0);
        skuInfoService.updateById(skuInfo);
        // es下架
        rabbitTemplate.convertAndSend(MqConst.ON_OFF_SALE_EXCHANGE, MqConst.OFF_SALE_ROUTING_KEY, skuId);
//        searchFeignClient.offSale(skuId);
        return RetVal.ok();
    }

}

