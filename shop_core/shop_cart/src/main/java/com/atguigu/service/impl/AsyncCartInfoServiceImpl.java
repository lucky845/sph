package com.atguigu.service.impl;

import com.atguigu.entity.CartInfo;
import com.atguigu.mapper.CartInfoMapper;
import com.atguigu.service.AsyncCartInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author lucky845
 * @date 2022年04月20日
 */
@Async
@Service
public class AsyncCartInfoServiceImpl extends ServiceImpl<CartInfoMapper, CartInfo> implements AsyncCartInfoService {

    @Override
    public void updateCartInfo(CartInfo existCartInfo) {
        baseMapper.updateById(existCartInfo);
    }

    @Override
    public void saveCartInfo(CartInfo existCartInfo) {
        baseMapper.insert(existCartInfo);
    }

    @Override
    public void checkDbCart(String oneOfUserId, Long skuId, Integer isChecked) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", oneOfUserId);
        wrapper.eq("sku_id", skuId);
        baseMapper.update(cartInfo, wrapper);
    }

    @Override
    public void deleteCartInfo(String oneOfUserId, Long skuId) {
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(oneOfUserId)) {
            wrapper.eq("user_id", oneOfUserId);
        }
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        baseMapper.delete(wrapper);
    }
}
