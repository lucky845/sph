package com.atguigu.client;

import com.atguigu.entity.SeckillProduct;
import com.atguigu.fallback.SeckillFallback;
import com.atguigu.result.RetVal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "shop-seckill", fallback = SeckillFallback.class)
public interface SeckillFeignClient {

    /**
     * 查询所有的秒杀商品
     */
    @GetMapping("/seckill/queryAllSeckillProduct")
    public RetVal<List<Object>> queryAllSeckillProduct();

    /**
     * 根据skuId获取秒杀对象数据
     *
     * @param skuId 商品skuId
     */
    @GetMapping("/seckill/getSeckillProductBySkuId/{skuId}")
    public RetVal<SeckillProduct> getSeckillProductBySkuId(@PathVariable Long skuId);

}
