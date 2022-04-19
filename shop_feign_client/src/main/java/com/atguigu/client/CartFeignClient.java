package com.atguigu.client;

import com.atguigu.fallback.CartFallback;
import com.atguigu.result.RetVal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author lucky845
 * @since 2022-04-19
 */
@FeignClient(value = "shop-cart", fallback = CartFallback.class)
public interface CartFeignClient {

    /**
     * 加入购物车
     *
     * @param skuId  商品skuId
     * @param skuNum 商品数量
     */
    @PostMapping("/cart/addToCart/{skuId}/{skuNum}")
    public RetVal<Object> addToCart(@PathVariable Long skuId, @PathVariable Integer skuNum);

}
