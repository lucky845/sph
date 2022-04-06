package com.atguigu.service;

import com.atguigu.entity.SkuInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 库存单元表 服务类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
public interface SkuInfoService extends IService<SkuInfo> {

    /**
     * 保存商品SKU信息
     *
     * @param skuInfo 商品SKU对象
     */
    void saveSkuInfo(SkuInfo skuInfo);
}
