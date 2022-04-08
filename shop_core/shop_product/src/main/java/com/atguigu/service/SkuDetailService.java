package com.atguigu.service;

import com.atguigu.entity.SkuInfo;

/**
 * @author lucky845
 * @date 2022年04月08日
 */
public interface SkuDetailService {

    /**
     * 根据skuId查询商品基本信息
     *
     * @param skuId 商品skuId
     */
    SkuInfo getSkuInfo(Long skuId);
}
