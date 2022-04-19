package com.atguigu.service.impl;

import com.atguigu.client.ProductFeignClient;
import com.atguigu.constant.RedisConst;
import com.atguigu.entity.CartInfo;
import com.atguigu.entity.SkuInfo;
import com.atguigu.mapper.CartInfoMapper;
import com.atguigu.service.CartInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jodd.util.StringUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 购物车表 用户登录系统时更新冗余 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-19
 */
@Service
public class CartInfoServiceImpl extends ServiceImpl<CartInfoMapper, CartInfo> implements CartInfoService {

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 加入购物车
     *
     * @param oneOfUserId 用户id或临时用户id
     * @param skuId       商品skuId
     * @param skuNum      商品数量
     */
    @Override
    public void addToCart(String oneOfUserId, Long skuId, Integer skuNum) {
        // 1. 根据skuId查询购物车中是否有该商品
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id", skuId);
        wrapper.eq("user_id", oneOfUserId);
        CartInfo existCartInfo = baseMapper.selectOne(wrapper);
        // 2. 如果有该商品, 则更新数量
        if (existCartInfo != null) {
            // 把原来的数量加上新的数量
            existCartInfo.setSkuNum(existCartInfo.getSkuNum() + skuNum);
            // 更新加入购物车的实时价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            existCartInfo.setRealTimePrice(skuPrice);
            // 修改库存数量
            baseMapper.updateById(existCartInfo);
        } else {
            existCartInfo = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            existCartInfo.setUserId(oneOfUserId);
            existCartInfo.setSkuId(skuId);
            // 添加购物车时候的价格
            existCartInfo.setCartPrice(skuInfo.getPrice());
            existCartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            existCartInfo.setSkuNum(skuNum);
            existCartInfo.setSkuName(skuInfo.getSkuName());
            // 默认勾选该商品
            existCartInfo.setIsChecked(1);
            // 更新加入购物车的商品的实时价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            existCartInfo.setRealTimePrice(skuPrice);
            // 保存到数据库
            baseMapper.insert(existCartInfo);
        }
        // 4. 保存一份到Redis中
        String userCartKey = getUserCartKey(oneOfUserId);
        redisTemplate.boundHashOps(userCartKey).put(skuId.toString(), existCartInfo);
    }

    /**
     * 购物车列表查询
     *
     * @param userId     用户id
     * @param userTempId 用户临时id
     */
    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        ArrayList<CartInfo> cartInfoList = new ArrayList<>();

        // 用户未登录
        if (StringUtil.isEmpty(userId) && !StringUtil.isEmpty(userTempId)) {
            cartInfoList = queryCartInfoListFromDBToRedis(userTempId);
        }

        // 用户已登录


        return null;
    }

    /**
     * 查询购物车列表
     *
     * @param oneOfUserId 用户id或临时用户id
     */
    private ArrayList<CartInfo> queryCartInfoListFromDBToRedis(String oneOfUserId) {

        return null;
    }

    /**
     * 获取Redis中的userCartKey
     *
     * @param oneOfUserId 用户id或临时用户id
     */
    private String getUserCartKey(String oneOfUserId) {
        return RedisConst.USER_KEY_PREFIX + oneOfUserId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}
