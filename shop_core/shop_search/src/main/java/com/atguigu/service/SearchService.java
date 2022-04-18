package com.atguigu.service;

import com.atguigu.search.SearchParam;
import com.atguigu.search.SearchResponseVo;

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

    /**
     * 商品的搜索
     *
     * @param searchParam 商品搜索条件对象
     */
    SearchResponseVo searchProduct(SearchParam searchParam);
}
