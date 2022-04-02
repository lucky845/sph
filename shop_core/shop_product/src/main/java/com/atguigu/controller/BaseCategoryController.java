package com.atguigu.controller;


import com.atguigu.entity.BaseCategory1;
import com.atguigu.entity.BaseCategory2;
import com.atguigu.entity.BaseCategory3;
import com.atguigu.result.RetVal;
import com.atguigu.service.BaseCategory1Service;
import com.atguigu.service.BaseCategory2Service;
import com.atguigu.service.BaseCategory3Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 一二三级分类表 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
@Api(tags = "一二三级分类表")
@CrossOrigin
@RestController
@RequestMapping("/product")
public class BaseCategoryController {

    @Resource
    private BaseCategory1Service baseCategory1Service;

    @Resource
    private BaseCategory2Service baseCategory2Service;

    @Resource
    private BaseCategory3Service baseCategory3Service;

    /**
     * 查询一级分类列表
     */
    @ApiOperation("查询一级分类")
    @GetMapping("/getCategory1")
    public RetVal<List<BaseCategory1>> getCategory1() {
        List<BaseCategory1> baseCategory1List = baseCategory1Service.list(null);
        return RetVal.ok(baseCategory1List);
    }

    /**
     * 查询二级分类列表
     *
     * @param category1Id 一级分类id
     */
    @ApiOperation("查询二级分类表")
    @GetMapping("/getCategory2/{category1Id}")
    public RetVal<List<BaseCategory2>> getCategory2(
            @ApiParam(name = "category1Id", value = "一级分类id", required = true)
            @PathVariable Long category1Id
    ) {
        QueryWrapper<BaseCategory2> baseCategory2QueryWrapper = new QueryWrapper<>();
        baseCategory2QueryWrapper.eq("category1_id", category1Id);
        List<BaseCategory2> baseCategory2List = baseCategory2Service.list(baseCategory2QueryWrapper);
        return RetVal.ok(baseCategory2List);
    }

    /**
     * 查询三级分类表
     *
     * @param category2Id 二级分类id
     */
    @ApiOperation("查询三级分类表")
    @GetMapping("/getCategory3/{category2Id}")
    public RetVal<List<BaseCategory3>> getCategory3(
            @ApiParam(name = "category2Id", value = "二级分类id", required = true)
            @PathVariable Long category2Id
    ) {
        QueryWrapper<BaseCategory3> baseCategory3QueryWrapper = new QueryWrapper<>();
        baseCategory3QueryWrapper.eq("category2_id", category2Id);
        List<BaseCategory3> baseCategory3List = baseCategory3Service.list(baseCategory3QueryWrapper);
        return RetVal.ok(baseCategory3List);
    }

}

