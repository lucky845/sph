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
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        List<CartInfo> cartInfoList = new ArrayList<>();

        // 用户未登录
        if (StringUtil.isEmpty(userId) && !StringUtil.isEmpty(userTempId)) {
            cartInfoList = queryCartInfoListFromDbToRedis(userTempId);
        }

        // 用户已登录
        if (!StringUtil.isEmpty(userId) && !StringUtil.isEmpty(userTempId)) {
            // 1. 先查询未登录的购物车项
            List<CartInfo> noLoginCartInfoList = queryCartInfoListFromDbToRedis(userTempId);
            if (!CollectionUtils.isEmpty(noLoginCartInfoList)) {
                // 2. 合并未登陆和已登录的购物车项
                mergeCartInfoList(userId, userTempId);
                // 3. 合并之后，删除临时用户的购物车项信息
                cartInfoList = deleteOldDataLoadNewData(userId, userTempId);
            } else {
                // 先删除缓存，再查询数据库
                String userCartKey = getUserCartKey(userId);
                redisTemplate.delete(userCartKey);
                cartInfoList = queryCartInfoListFromDbToRedis(userId);
            }
        }
        return cartInfoList;
    }

    /**
     * 修改购物车勾选状态
     *
     * @param oneOfUserId 用户id或临时用户id
     * @param skuId       商品skuId
     * @param isChecked   商品勾选状态
     */
    @Override
    public void checkCart(String oneOfUserId, Long skuId, Integer isChecked) {
        // 1. 从Redis中获取数据
        String userCartKey = getUserCartKey(oneOfUserId);
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(userCartKey);
        if (boundHashOperations.hasKey(skuId.toString())) {
            CartInfo redisCartInfo = (CartInfo) boundHashOperations.get(skuId.toString());
            redisCartInfo.setIsChecked(isChecked);
            redisTemplate.boundHashOps(userCartKey).put(skuId.toString(), redisCartInfo);
            // 设置过期时间
            setCartKeyExpire(userCartKey);
        }
        // 2. 从数据库中获取数据进行修改
        checkDbCart(oneOfUserId, skuId, isChecked);
    }

    /**
     * 从缓存中获取数据并进行修改
     *
     * @param oneOfUserId 用户id或临时用户id
     * @param skuId       商品skuId
     * @param isChecked   商品勾选状态
     */
    private void checkDbCart(String oneOfUserId, Long skuId, Integer isChecked) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", oneOfUserId);
        wrapper.eq("sku_id", skuId);
        baseMapper.update(cartInfo, wrapper);
    }

    /**
     * 删除缓存中的老数据，更新新数据
     *
     * @param userId     用户id
     * @param userTempId 用户临时id
     */
    private List<CartInfo> deleteOldDataLoadNewData(String userId, String userTempId) {
        // 删除数据库里面的临时用户id的数据
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userTempId);
        List<CartInfo> cartInfoList;
        baseMapper.delete(wrapper);
        // 删除缓存里面的旧数据，并重新查询数据库，放入缓存
        String userCartKey = getUserCartKey(userId);
        String userTempCartKey = getUserCartKey(userTempId);
        redisTemplate.delete(userCartKey);
        redisTemplate.delete(userTempCartKey);
        cartInfoList = queryCartInfoListFromDbToRedis(userId);
        return cartInfoList;
    }

    /**
     * 合并未登录和已登录的购物车列表
     *
     * @param userId     用户id
     * @param userTempId 临时用户id
     */
    private void mergeCartInfoList(String userId, String userTempId) {
        // 1. 先查询未登录的购物车项
        List<CartInfo> noLoginCartInfoList = queryCartInfoListFromDbToRedis(userTempId);
        // 2. 查询已登陆的购物车项
        List<CartInfo> loginCartInfoList = queryCartInfoListFromDbToRedis(userId);
        // 3. 把已登陆的购物车项转换为一个map，可以根据key判断该map中是否有记录可以减少迭代
        Map<Long, CartInfo> longCartInfoMap = loginCartInfoList.stream()
                .collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> cartInfo));
        // 4.
        for (CartInfo noLoginCartInfo : noLoginCartInfoList) {
            Long noLoginCartInfoSkuId = noLoginCartInfo.getSkuId();
            // 对比skuId是否相同，代表已登录里面包含该购物车项
            if (longCartInfoMap.containsKey(noLoginCartInfoSkuId)) {
                CartInfo loginCartInfo = longCartInfoMap.get(noLoginCartInfoSkuId);
                // 商品数量相加
                loginCartInfo.setSkuNum(loginCartInfo.getSkuNum() + noLoginCartInfo.getSkuNum());
                // 当未登录的商品勾选了的时候，登录后也应该勾选
                if (noLoginCartInfo.getIsChecked() == 1) {
                    loginCartInfo.setIsChecked(1);
                }
                // 更新数据库
                baseMapper.updateById(loginCartInfo);
            } else {
                // 如果已登陆的里面没有
                noLoginCartInfo.setUserId(userId);
                baseMapper.updateById(noLoginCartInfo);
            }
        }

    }

    /**
     * 从数据库查询购物车列表，并放入缓存
     *
     * @param oneOfUserId 用户id或临时用户id
     */
    private List<CartInfo> queryCartInfoListFromDbToRedis(String oneOfUserId) {
        // 先查询Redis缓存，缓存没有再查询数据库
        String userCartKey = getUserCartKey(oneOfUserId);
        List<CartInfo> cartInfoList = redisTemplate.boundHashOps(userCartKey).values();
        if (CollectionUtils.isEmpty(cartInfoList)) {
            // 缓存中没有，查询数据库
            QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", oneOfUserId);
            cartInfoList = baseMapper.selectList(wrapper);
            // 将查询出来的数据放到缓存中
            HashMap<String, CartInfo> cartInfoMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                cartInfo.setRealTimePrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
                cartInfoMap.put(cartInfo.getSkuId().toString(), cartInfo);
            }
            redisTemplate.boundHashOps(userCartKey).putAll(cartInfoMap);
            // 设置Redis中的过期时间
            setCartKeyExpire(userCartKey);
        }
        return cartInfoList;
    }

    /**
     * 设置Redis中购物车信息的过期时间
     *
     * @param userCartKey 缓存键
     */
    private void setCartKeyExpire(String userCartKey) {
        redisTemplate.expire(userCartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
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
