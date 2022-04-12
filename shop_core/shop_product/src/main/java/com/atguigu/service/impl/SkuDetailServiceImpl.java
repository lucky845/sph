package com.atguigu.service.impl;

import com.atguigu.constant.RedisConst;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuImage;
import com.atguigu.entity.SkuInfo;
import com.atguigu.mapper.ProductSalePropertyKeyMapper;
import com.atguigu.service.SkuDetailService;
import com.atguigu.service.SkuImageService;
import com.atguigu.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author lucky845
 * @date 2022年04月08日
 */
@Service
public class SkuDetailServiceImpl implements SkuDetailService {

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuImageService skuImageService;

    @Resource
    private ProductSalePropertyKeyMapper salePropertyKeyMapper;

    private final ThreadLocal<String> threadLocal = new ThreadLocal<>();
    @Resource
    private RedisTemplate<Object, Object> redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RBloomFilter<Object> bloomFilter;

    /**
     * 根据skuId查询商品基本信息
     *
     * @param skuId 商品skuId
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return getSkuInfoFromDB(skuId);
    }

    /**
     * 利用Redisson实现查询商品的基本信息+布隆过滤器
     *
     * @param skuId 商品skuId
     */
    private SkuInfo getSkuInfoFromRedisson(Long skuId) {
        String cacheKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        // 从缓存中获取数据
        SkuInfo skuInfoFromRedis = (SkuInfo) redisTemplate.opsForValue().get(cacheKey);
        // 如果缓存中没有数据，则从数据库中获取
        if (skuInfoFromRedis == null) {
            // 让锁的粒度更小，提高效率
            String lockKey = "lock-" + skuId;
            // 获取锁
            RLock lock = redissonClient.getLock(lockKey);
            // 上锁
            lock.lock();
            try {
                // 查询之前先进行判断，该id是否在布隆过滤器中存在
                boolean flag = bloomFilter.contains(skuId);
                SkuInfo skuInfoFromDB = null;
                if (flag) {
                    // 从数据库获取数据
                    skuInfoFromDB = getSkuInfoFromDB(skuId);
                }
                // 将数据保存到Redis
                redisTemplate.opsForValue().set(cacheKey, skuInfoFromDB, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                // 返回数据库中的数据
                return skuInfoFromDB;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 释放锁
                lock.unlock();
            }
        }
        // 返回缓存中的数据
        return skuInfoFromRedis;
    }

    /**
     * 利用Redis+Lua+ThreadLocal实现查询商品的基本信息
     *
     * @param skuId 商品skuId
     */
    private SkuInfo getSkuInfoFromRedisWithThreadLocal(Long skuId) {
        String cacheKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        // 从缓存中获取数据
        SkuInfo skuInfoFromRedis = (SkuInfo) redisTemplate.opsForValue().get(cacheKey);
        // 如果缓存中没有数据，则从数据库中获取
        if (skuInfoFromRedis == null) {
            String token = threadLocal.get();
            boolean acquireLock = false;
            // 让锁的粒度更小，提高效率
            String lockKey = "lock-" + skuId;
            if (token == null) {
                // 代表线程刚进来，还没有自旋过，需要获取锁
                token = UUID.randomUUID().toString();
                acquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, token, RedisConst.SKUKEY_TIMEOUT, TimeUnit.MINUTES);
            } else {
                // 程序已经自旋过，已经获取到了锁
                acquireLock = true;
            }
            if (acquireLock) {
                SkuInfo skuInfoFromDB = getSkuInfoFromDB(skuId);
                // 将数据放入Redis缓存
                redisTemplate.opsForValue().set(cacheKey, skuInfoFromDB, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                String luaScript = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                redisScript.setScriptText(luaScript);
                redisScript.setResultType(Long.class);
                redisTemplate.execute(redisScript, Collections.singletonList(lockKey), token);
                // 清除ThreadLocal中的数据，防止内存泄漏
                threadLocal.remove();
                // 返回数据库中的数据
                return skuInfoFromDB;
            } else {
                // 自旋
                for (; ; ) {
                    // 尝试获取锁
                    boolean retryAcquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, token, RedisConst.SKUKEY_TIMEOUT, TimeUnit.MINUTES);
                    if (retryAcquireLock) {
                        // 拿到锁之后，就不需要自旋了，把拿到的锁的标记放到ThreadLocal中
                        threadLocal.set(token);
                        break;
                    }
                }
                // 如果没有拿到锁，就递归
                return getSkuInfoFromRedisWithThreadLocal(skuId);
            }
        } else {
            // 返回缓存中的数据
            return skuInfoFromRedis;
        }
    }

    /**
     * 从Redis获取数据(添加Redis缓存)
     *
     * @param skuId 商品skuId
     */
    private SkuInfo getSkuInfoFromRedis(Long skuId) {
        // 缓存key
        String cacheKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        // 从缓存获取数据
        SkuInfo skuInfoFromRedis = (SkuInfo) redisTemplate.opsForValue().get(cacheKey);
        // 如果数据库中没有缓存
        if (skuInfoFromRedis == null) {
            // 从数据库查询数据
            SkuInfo skuInfoFromDB = getSkuInfoFromDB(skuId);
            // 将数据放入缓存
            redisTemplate.opsForValue().set(cacheKey, skuInfoFromDB, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
            return skuInfoFromDB;
        }
        return skuInfoFromRedis;
    }

    /**
     * 从数据库获取数据
     *
     * @param skuId 商品skuId
     */
    private SkuInfo getSkuInfoFromDB(Long skuId) {
        // 1. 查询商品的基本信息
        SkuInfo skuInfo = skuInfoService.getById(skuId);
        // 2. 查询商品的图片信息
        if (skuInfo != null) {
            QueryWrapper<SkuImage> wrapper = new QueryWrapper<>();
            wrapper.eq("sku_id", skuId);
            List<SkuImage> skuImageList = skuImageService.list(wrapper);
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }

    /**
     * 获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)
     *
     * @param productId 商品Id
     * @param skuId     商品skuId
     */
    @Override
    public List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(Long productId, Long skuId) {
        return salePropertyKeyMapper.getSpuSalePropertyAndSelected(productId, skuId);
    }
}
