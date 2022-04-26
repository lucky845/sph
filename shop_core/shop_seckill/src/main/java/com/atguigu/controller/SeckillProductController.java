package com.atguigu.controller;


import com.atguigu.constant.RedisConst;
import com.atguigu.entity.SeckillProduct;
import com.atguigu.result.RetVal;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-26
 */
@RestController
@RequestMapping("/seckill")
public class SeckillProductController {

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 查询所有的秒杀商品
     */
    @ApiOperation("查询所有的秒杀商品")
    @GetMapping("/queryAllSeckillProduct")
    public RetVal<List<Object>> queryAllSeckillProduct() {
        List<Object> seckillProductList = redisTemplate.boundHashOps(RedisConst.SECKILL_PRODUCT).values();
        return RetVal.ok(seckillProductList);
    }

    /**
     * 根据skuId获取秒杀对象数据
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("根据skuId获取秒杀对象数据")
    @GetMapping("/getSeckillProductBySkuId/{skuId}")
    public RetVal<SeckillProduct> getSeckillProductBySkuId(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId
    ) {
        SeckillProduct seckillProduct = getSeckillProduct(skuId);
        return RetVal.ok(seckillProduct);
    }

    /**
     * 根据skuId获取秒杀对象数据
     *
     * @param skuId 商品skuId
     */
    private SeckillProduct getSeckillProduct(Long skuId) {
        return (SeckillProduct) redisTemplate.boundHashOps(RedisConst.SECKILL_PRODUCT).get(skuId.toString());
    }

}

