package com.atguigu.service;

import com.atguigu.entity.CartInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 购物车表 用户登录系统时更新冗余 服务类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-19
 */
public interface CartInfoService extends IService<CartInfo> {

    /**
     * 加入购物车
     *
     * @param oneOfUserId 用户id或临时用户id
     * @param skuId       商品skuId
     * @param skuNum      商品数量
     */
    void addToCart(String oneOfUserId, Long skuId, Integer skuNum);

    /**
     * 购物车列表查询
     *
     * @param userId     用户id
     * @param userTempId 用户临时id
     */
    List<CartInfo> getCartList(String userId, String userTempId);

    /**
     * 修改商品的勾选状态
     *
     * @param oneOfUserId 用户id或临时用户id
     * @param skuId       商品skuId
     * @param isChecked   勾选状态
     */
    void checkCart(String oneOfUserId, Long skuId, Integer isChecked);
}
