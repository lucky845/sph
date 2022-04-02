package com.atguigu.controller;


import com.atguigu.result.RetVal;
import com.atguigu.service.PlatformPropertyKeyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 属性表 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
@Api(tags = "平台属性")
@CrossOrigin
@RestController
@RequestMapping("/product")
public class PlatformPropertyController {

    @Resource
    private PlatformPropertyKeyService platformPropertyKeyService;

    @GetMapping("/getPlatformPropertyByCategoryId/{category1Id}/{category2Id}/{category3Id}")
    public RetVal getPlatformPropertyByCategoryId(
            @ApiParam(name = "category1Id", value = "一级分类id")
            @PathVariable Long category1Id,

            @ApiParam(name = "category2Id", value = "二级分类id")
            @PathVariable Long category2Id,

            @ApiParam(name = "category3Id", value = "三级分类id")
            @PathVariable Long category3Id
    ) {

        return RetVal.ok();
    }

}

