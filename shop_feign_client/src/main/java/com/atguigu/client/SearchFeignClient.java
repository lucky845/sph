package com.atguigu.client;

import com.atguigu.fallback.SearchFallback;
import com.atguigu.result.RetVal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lucky845
 * @date 2022年04月06日
 */
@FeignClient(value = "shop-search", fallback = SearchFallback.class)
public interface SearchFeignClient {

    /**
     * 商品的上架
     *
     * @param skuId 商品skuId
     */
    @GetMapping("/search/onSale/{skuId}")
    public RetVal<Object> onSale(@PathVariable Long skuId);

    /**
     * 商品的下架
     *
     * @param skuId 商品skuId
     */
    @GetMapping("/search/offSale/{skuId}")
    public RetVal<Object> offSale(@PathVariable Long skuId);

}
