package com.atguigu.service.impl;

import com.atguigu.entity.ProductImage;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.ProductSalePropertyValue;
import com.atguigu.entity.ProductSpu;
import com.atguigu.mapper.ProductSpuMapper;
import com.atguigu.service.ProductImageService;
import com.atguigu.service.ProductSalePropertyKeyService;
import com.atguigu.service.ProductSalePropertyValueService;
import com.atguigu.service.ProductSpuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
@Service
public class ProductSpuServiceImpl extends ServiceImpl<ProductSpuMapper, ProductSpu> implements ProductSpuService {

    @Resource
    private ProductImageService imageService;

    @Resource
    private ProductSalePropertyKeyService salePropertyKeyService;

    @Resource
    private ProductSalePropertyValueService salePropertyValueService;

    /**
     * 保存商品SPU信息
     *
     * @param productSpu 商品SPU对象
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveProductSpu(ProductSpu productSpu) {
        // 1. 保存商品SPU
        baseMapper.insert(productSpu);
        Long spuId = productSpu.getId();
        // 2. 保存商品SPU对应的图片
        List<ProductImage> productImageList = productSpu.getProductImageList();
        if (!CollectionUtils.isEmpty(productImageList)) {
            for (ProductImage productImage : productImageList) {
                productImage.setProductId(spuId);
            }
            imageService.saveBatch(productImageList);
        }
        // 3. 保存商品SPU对应的销售属性key
        List<ProductSalePropertyKey> salePropertyKeyList = productSpu.getSalePropertyKeyList();
        if (!CollectionUtils.isEmpty(salePropertyKeyList)) {
            for (ProductSalePropertyKey salePropertyKey : salePropertyKeyList) {
                salePropertyKey.setProductId(spuId);
                // 4. 保存商品SPU对应的销售属性value
                List<ProductSalePropertyValue> salePropertyValueList = salePropertyKey.getSalePropertyValueList();
                if (!CollectionUtils.isEmpty(salePropertyValueList)) {
                    for (ProductSalePropertyValue salePropertyValue : salePropertyValueList) {
                        salePropertyValue.setProductId(spuId);
                        salePropertyValue.setSalePropertyKeyName(salePropertyKey.getSalePropertyKeyName());
                    }
                    salePropertyValueService.saveBatch(salePropertyValueList);
                }
            }
            salePropertyKeyService.saveBatch(salePropertyKeyList);
        }
    }
}
