package com.atguigu.client;

import com.atguigu.fallback.SearchFallback;
import com.atguigu.result.RetVal;
import com.atguigu.search.SearchParam;
import com.atguigu.search.SearchResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    /**
     * 商品的搜索
     *
     * @param searchParam 商品搜索条件对象
     */
    @PostMapping("/search/searchProduct")
    public RetVal<SearchResponseVo> searchProduct(@RequestBody SearchParam searchParam);

}
