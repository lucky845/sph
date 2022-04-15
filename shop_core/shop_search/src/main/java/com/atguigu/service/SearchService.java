package com.atguigu.service;

public interface SearchService {

    /**
     * 商品的上架
     *
     * @param skuId 商品销售属性id
     */
    void onSale(Long skuId);

    /**
     * 商品的下架
     *
     * @param skuId 商品销售属性id
     */
    void offSale(Long skuId);
}
