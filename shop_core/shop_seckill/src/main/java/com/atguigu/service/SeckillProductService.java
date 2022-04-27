package com.atguigu.service;

import com.atguigu.entity.SeckillProduct;
import com.atguigu.entity.UserSeckillSkuInfo;
import com.atguigu.result.RetVal;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-26
 */
public interface SeckillProductService extends IService<SeckillProduct> {

    /**
     * 根据skuId获取秒杀对象数据
     *
     * @param skuId 商品skuId
     */
    SeckillProduct getSeckillProduct(Long skuId);

    /**
     * 判断用户是否具备抢购资格
     *
     * @param skuId  商品skuId
     * @param userId 用户id
     */
    RetVal hasQualified(Long skuId, String userId);

    /**
     * 处理秒杀预下单
     *
     * @param userSeckillSkuInfo 用户秒杀商品信息
     */
    void prepareSeckill(UserSeckillSkuInfo userSeckillSkuInfo);

    /**
     * 返回秒杀确认页面需要的数据
     *
     * @param userId 用户id
     */
    RetVal seckillConfirm(String userId);
}
