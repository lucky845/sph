package com.atguigu.controller;

/**
 * @author lucky845
 * @date 2022年04月06日 13:46
 */

import com.atguigu.entity.BaseSaleProperty;
import com.atguigu.entity.ProductSpu;
import com.atguigu.result.RetVal;
import com.atguigu.service.BaseSalePropertyService;
import com.atguigu.service.ProductSpuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * 商品表 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-05
 */
@Api(tags = "商品SPU信息")
@RestController
@RequestMapping("/product")
public class SpuController {

    @Resource
    private ProductSpuService spuService;

    @Resource
    private BaseSalePropertyService salePropertyService;

    /**
     * 根据分类id查询商品SPU分页信息
     *
     * @param currentPageNum 当前页码
     * @param pageSize       每页记录数
     * @param category3Id    三级分类id
     */
    @ApiOperation("根据分类id查询商品SPU分页信息")
    @GetMapping("/queryProductSpuByPage/{currentPageNum}/{pageSize}/{category3Id}")
    public RetVal<Page<ProductSpu>> queryBrandByPage(
            @ApiParam(name = "currentPageNum", value = "当前页码", required = true)
            @PathVariable Long currentPageNum,

            @ApiParam(name = "pageSize", value = "每页记录数", required = true)
            @PathVariable Long pageSize,

            @ApiParam(name = "category3Id", value = "三级分类id", required = true)
            @PathVariable Long category3Id
    ) {
        Page<ProductSpu> page = new Page<>(currentPageNum, pageSize);
        QueryWrapper<ProductSpu> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id", category3Id).orderByDesc("id");
        spuService.page(page, wrapper);
        return RetVal.ok(page);
    }

    /**
     * 查询所有的销售属性
     */
    @ApiOperation("查询所有的销售属性")
    @GetMapping("/queryAllSaleProperty")
    public RetVal<List<BaseSaleProperty>> queryAllSaleProperty() {
        List<BaseSaleProperty> salePropertyList = salePropertyService.list(null);
        return RetVal.ok(salePropertyList);
    }

}
