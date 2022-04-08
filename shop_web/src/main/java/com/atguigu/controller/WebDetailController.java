package com.atguigu.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author lucky845
 * @date 2022年04月07日 13:37
 */
@Controller
public class WebDetailController {

    /**
     * 商品详情信息
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("商品详情信息")
    @RequestMapping("{skuId}.html")
    public String getSkuDetail(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId
    ) {
        return "detail/index";
    }
}
