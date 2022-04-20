package com.atguigu.fallback;

import com.atguigu.client.CartFeignClient;
import com.atguigu.entity.CartInfo;
import com.atguigu.result.RetVal;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lucky845
 * @date 2022年04月16日
 */
public class CartFallback implements CartFeignClient {
    /**
     * 加入购物车
     *
     * @param skuId  商品skuId
     * @param skuNum 商品数量
     */
    @Override
    public RetVal<Object> addToCart(Long skuId, Integer skuNum) {
        return RetVal.ok();
    }

    /**
     * 查询用户购物清单
     *
     * @param userId 用户id
     */
    @Override
    public List<CartInfo> getSelectedProduct(String userId) {
        return new ArrayList<>();
    }
}
