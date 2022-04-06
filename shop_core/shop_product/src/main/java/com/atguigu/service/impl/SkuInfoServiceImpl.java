package com.atguigu.service.impl;

import com.atguigu.entity.SkuImage;
import com.atguigu.entity.SkuInfo;
import com.atguigu.entity.SkuPlatformPropertyValue;
import com.atguigu.entity.SkuSalePropertyValue;
import com.atguigu.mapper.SkuInfoMapper;
import com.atguigu.service.SkuImageService;
import com.atguigu.service.SkuInfoService;
import com.atguigu.service.SkuPlatformPropertyValueService;
import com.atguigu.service.SkuSalePropertyValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 库存单元表 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {

    @Resource
    private SkuPlatformPropertyValueService skuPlatformPropertyValueService;

    @Resource
    private SkuSalePropertyValueService skuSalePropertyValueService;

    @Resource
    private SkuImageService skuImageService;

    /**
     * 保存商品SKU信息
     *
     * @param skuInfo 商品SKU对象
     */
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        // 1. 保存基本的SKU信息
        baseMapper.insert(skuInfo);
        Long skuId = skuInfo.getId();
        Long spuId = skuInfo.getProductId();
        // 2. 保存SKU的平台属性
        List<SkuPlatformPropertyValue> skuPlatformPropertyValueList = skuInfo.getSkuPlatformPropertyValueList();
        if (!CollectionUtils.isEmpty(skuPlatformPropertyValueList)) {
            for (SkuPlatformPropertyValue skuPlatformPropertyValue : skuPlatformPropertyValueList) {
                skuPlatformPropertyValue.setSkuId(skuId);
            }
            skuPlatformPropertyValueService.saveBatch(skuPlatformPropertyValueList);
        }
        // 3. 保存SKU的销售属性
        List<SkuSalePropertyValue> skuSalePropertyValueList = skuInfo.getSkuSalePropertyValueList();
        if (!CollectionUtils.isEmpty(skuSalePropertyValueList)) {
            for (SkuSalePropertyValue skuSalePropertyValue : skuSalePropertyValueList) {
                // 该销售属性属于那个SKU
                skuSalePropertyValue.setSkuId(skuId);
                // 该销售属性属于那个SPU
                skuSalePropertyValue.setProductId(spuId);
            }
            skuSalePropertyValueService.saveBatch(skuSalePropertyValueList);
        }
        // 4. 保存SKU的勾选图片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuId);
            }
            skuImageService.saveBatch(skuImageList);
        }
    }
}
