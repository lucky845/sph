package com.atguigu.controller;

import com.atguigu.result.RetVal;
import com.atguigu.search.Product;
import com.atguigu.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lucky845
 * @date 2022年04月15日
 */
@Api(tags = "es搜索模块")
@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private ElasticsearchRestTemplate esRestTemplate;

    @Resource
    private SearchService searchService;

    /**
     * 创建索引与映射
     */
    @ApiOperation("创建索引与映射")
    @GetMapping("/createIndex")
    public RetVal<Object> createIndex() {
        // 创建索引
        esRestTemplate.createIndex(Product.class);
        // 添加映射
        esRestTemplate.putMapping(Product.class);
        return RetVal.ok();
    }

    /**
     * 商品的上架
     *
     * @param skuId 商品销售属性id
     */
    @ApiOperation("商品的上架")
    @GetMapping("/onSale/{skuId}")
    public RetVal<Object> onSale(
            @ApiParam(name = "商品销售属性id", value = "skuId", required = true)
            @PathVariable Long skuId
    ) {
        searchService.onSale(skuId);
        return RetVal.ok();
    }

    /**
     * 商品的下架
     *
     * @param skuId 商品销售属性id
     */
    @ApiOperation("商品的下架")
    @GetMapping("/offSale/{skuId}")
    public RetVal<Object> offSale(
            @ApiParam(name = "商品销售属性id", value = "skuId", required = true)
            @PathVariable Long skuId
    ) {
        searchService.offSale(skuId);
        return RetVal.ok();
    }

}
