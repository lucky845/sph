package com.atguigu.controller;

import com.atguigu.result.RetVal;
import com.atguigu.search.Product;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
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

}
