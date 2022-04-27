package com.atguigu.fallback;

import com.atguigu.client.SeckillFeignClient;
import com.atguigu.entity.SeckillProduct;
import com.atguigu.result.RetVal;

import java.util.List;

/**
 * @author lucky845
 * @since 2022年04月26日
 */
public class SeckillFallback implements SeckillFeignClient {

    /**
     * 查询所有的秒杀商品
     */
    @Override
    public RetVal<List<Object>> queryAllSeckillProduct() {
        return RetVal.ok();
    }

    /**
     * 根据skuId获取秒杀对象数据
     *
     * @param skuId 商品skuId
     */
    @Override
    public RetVal<SeckillProduct> getSeckillProductBySkuId(Long skuId) {
        return RetVal.ok();
    }

    /**
     * 返回秒杀确认页面需要的数据
     */
    @Override
    public RetVal seckillConfirm() {
        return RetVal.ok();
    }
}
