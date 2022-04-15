package com.atguigu.service.impl;

import com.atguigu.client.ProductFeignClient;
import com.atguigu.dao.ProductMapper;
import com.atguigu.entity.BaseBrand;
import com.atguigu.entity.BaseCategoryView;
import com.atguigu.entity.PlatformPropertyKey;
import com.atguigu.entity.SkuInfo;
import com.atguigu.search.Product;
import com.atguigu.search.SearchPlatformProperty;
import com.atguigu.service.SearchService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lucky845
 * @date 2022年04月15日
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private ProductMapper productMapper;

    /**
     * 商品的上架
     *
     * @param skuId 商品销售属性id
     */
    @Override
    public void onSale(Long skuId) {
        Product product = new Product();
        // 1. 商品的基本信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuInfo != null) {
            product.setId(skuInfo.getId());
            product.setProductName(skuInfo.getSkuName());
            product.setCreateTime(new Date());
            product.setPrice(skuInfo.getPrice().doubleValue());
            product.setDefaultImage(skuInfo.getSkuDefaultImg());
            // 2. 品牌信息
            Long brandId = skuInfo.getBrandId();
            BaseBrand brand = productFeignClient.getBrandByBrandId(brandId);
            if (brand != null) {
                product.setBrandId(brandId);
                product.setBrandName(brand.getBrandName());
                product.setBrandLogoUrl(brand.getBrandLogoUrl());
            }
            // 3. 商品的分类信息
            Long category3Id = skuInfo.getCategory3Id();
            BaseCategoryView categoryView = productFeignClient.getCategoryView(category3Id);
            if (categoryView != null) {
                product.setCategory1Id(categoryView.getCategory1Id());
                product.setCategory1Name(categoryView.getCategory1Name());
                product.setCategory2Id(categoryView.getCategory2Id());
                product.setCategory2Name(categoryView.getCategory2Name());
                product.setCategory3Id(categoryView.getCategory3Id());
                product.setCategory3Name(categoryView.getCategory3Name());
            }
            // 4. 单个商品的平台属性
            List<PlatformPropertyKey> propertyKeyList = productFeignClient.getPlatformPropertyBySkuId(skuId);
            if (!CollectionUtils.isEmpty(propertyKeyList)) {
                List<SearchPlatformProperty> searchPropertyList = propertyKeyList.stream().map(propertyKey -> {
                    SearchPlatformProperty searchPlatformProperty = new SearchPlatformProperty();
                    // 平台属性id
                    searchPlatformProperty.setPropertyKeyId(propertyKey.getId());
                    // 平台属性名称
                    searchPlatformProperty.setPropertyKey(propertyKey.getPropertyKey());
                    // 平台属性值
                    String propertyValue = propertyKey.getPropertyValueList().get(0).getPropertyValue();
                    searchPlatformProperty.setPropertyValue(propertyValue);
                    return searchPlatformProperty;
                }).collect(Collectors.toList());
                product.setPlatformProperty(searchPropertyList);
            }
        }
        // 存储到es中
        productMapper.save(product);
    }

    /**
     * 商品的下架
     *
     * @param skuId 商品销售属性id
     */
    @Override
    public void offSale(Long skuId) {
        // 从es中删除
        productMapper.deleteById(skuId);
    }
}
