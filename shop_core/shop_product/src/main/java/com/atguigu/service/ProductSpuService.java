package com.atguigu.service;

import com.atguigu.entity.ProductSpu;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
public interface ProductSpuService extends IService<ProductSpu> {

    /**
     * 保存商品SPU信息
     *
     * @param productSpu 商品SPU对象
     */
    void saveProductSpu(ProductSpu productSpu);
}
