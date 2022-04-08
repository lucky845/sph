package com.atguigu.service.impl;

import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuImage;
import com.atguigu.entity.SkuInfo;
import com.atguigu.mapper.ProductSalePropertyKeyMapper;
import com.atguigu.service.SkuDetailService;
import com.atguigu.service.SkuImageService;
import com.atguigu.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lucky845
 * @date 2022年04月08日
 */
@Service
public class SkuDetailServiceImpl implements SkuDetailService {

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuImageService skuImageService;

    @Resource
    private ProductSalePropertyKeyMapper salePropertyKeyMapper;

    /**
     * 根据skuId查询商品基本信息
     *
     * @param skuId 商品skuId
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        // 1. 查询商品的基本信息
        SkuInfo skuInfo = skuInfoService.getById(skuId);
        // 2. 查询商品的图片信息
        if (skuInfo != null) {
            QueryWrapper<SkuImage> wrapper = new QueryWrapper<>();
            wrapper.eq("sku_id", skuId);
            List<SkuImage> skuImageList = skuImageService.list(wrapper);
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }

    /**
     * 获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)
     *
     * @param productId 商品Id
     * @param skuId     商品skuId
     */
    @Override
    public List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(Long productId, Long skuId) {
        return salePropertyKeyMapper.getSpuSalePropertyAndSelected(productId, skuId);
    }
}
