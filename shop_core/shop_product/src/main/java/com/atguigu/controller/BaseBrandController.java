package com.atguigu.controller;


import com.atguigu.entity.BaseBrand;
import com.atguigu.result.RetVal;
import com.atguigu.service.BaseBrandService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 品牌表 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
@Api(tags = "品牌信息")
@RestController
@RequestMapping("/product")
public class BaseBrandController {

    @Resource
    private BaseBrandService baseBrandService;

    /**
     * 查询品牌信息分页记录
     *
     * @param currentPageNum 当前页码
     * @param pageSize       每页记录数
     */
    @ApiOperation("查询品牌信息分页记录")
    @GetMapping("/queryBrandByPage/{currentPageNum}/{pageSize}")
    public RetVal<Page<BaseBrand>> queryBrandByPage(
            @ApiParam(name = "currentPageNum", value = "当前页码", required = true)
            @PathVariable Long currentPageNum,

            @ApiParam(name = "pageSize", value = "每页记录数", required = true)
            @PathVariable Long pageSize
    ) {
        Page<BaseBrand> page = new Page<>(currentPageNum, pageSize);
        baseBrandService.page(page, null);
        return RetVal.ok(page);
    }




}

