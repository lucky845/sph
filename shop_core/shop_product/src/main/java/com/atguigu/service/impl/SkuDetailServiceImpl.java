package com.atguigu.service.impl;

import com.atguigu.entity.SkuImage;
import com.atguigu.entity.SkuInfo;
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
}
