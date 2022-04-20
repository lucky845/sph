package com.atguigu.service;

import com.atguigu.entity.CartInfo;

public interface AsyncCartInfoService {
    void updateCartInfo(CartInfo existCartInfo);

    void saveCartInfo(CartInfo existCartInfo);

    void checkDbCart(String oneOfUserId, Long skuId, Integer isChecked);

    void deleteCartInfo(String oneOfUserId, Long skuId);
}
