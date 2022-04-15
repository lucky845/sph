package com.atguigu.fallback;

import com.atguigu.client.SearchFeignClient;
import com.atguigu.result.RetVal;

/**
 * @author lucky845
 * @date 2022年04月15日
 */
public class SearchFallback implements SearchFeignClient {
    /**
     * 商品的上架
     *
     * @param skuId 商品skuId
     */
    @Override
    public RetVal<Object> onSale(Long skuId) {
        return RetVal.ok();
    }

    /**
     * 商品的下架
     *
     * @param skuId 商品skuId
     */
    @Override
    public RetVal<Object> offSale(Long skuId) {
        return RetVal.ok();
    }
}
