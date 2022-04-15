package com.atguigu.service;

import com.atguigu.entity.PlatformPropertyKey;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 属性表 服务类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
public interface PlatformPropertyKeyService extends IService<PlatformPropertyKey> {

    /**
     * 根据一二三级分类id获取平台属性信息
     *
     * @param category1Id 一级分类id
     * @param category2Id 二级分类id
     * @param category3Id 三级分类id
     */
    List<PlatformPropertyKey> getPlatformPropertyByCategoryId(Long category1Id, Long category2Id, Long category3Id);

    /**
     * 保存修改平台属性key与value
     *
     * @param platformPropertyKey 平台属性key对象
     */
    void savePlatformProperty(PlatformPropertyKey platformPropertyKey);

    /**
     * 根据商品skuId查询商品的平台信息
     *
     * @param skuId 商品销售属性Id
     */
    List<PlatformPropertyKey> getPlatformPropertyBySkuId(Long skuId);
}
