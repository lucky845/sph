package com.atguigu.service;

import com.atguigu.entity.ProductSalePropertyKey;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * spu销售属性 服务类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
public interface ProductSalePropertyKeyService extends IService<ProductSalePropertyKey> {

    /**
     * 根据SPUId查询销售属性
     *
     * @param spuId 商品SPUId
     */
    List<ProductSalePropertyKey> querySalePropertyByProductId(Long spuId);
}
