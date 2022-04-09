package com.atguigu.controller;

import com.atguigu.constant.RedisConst;
import com.atguigu.entity.BaseCategoryView;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;
import com.atguigu.mapper.SkuSalePropertyValueMapper;
import com.atguigu.service.BaseCategoryViewService;
import com.atguigu.service.SkuDetailService;
import com.atguigu.service.SkuInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author lucky845
 * @date 2022年04月08日
 */
@Api(tags = "商品详情")
@RequestMapping("/sku")
@RestController
public class SkuDetailController {

    @Resource
    private SkuDetailService skuDetailService;

    @Resource
    private BaseCategoryViewService categoryViewService;

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuSalePropertyValueMapper skuSalePropertyValueMapper;

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    private final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    /**
     * 根据skuId查询商品基本信息
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("根据skuId查询商品基本信息")
    @GetMapping("/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId
    ) {
        return getSkuInfoFromRedisWithThreadLocal(skuId);
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
                acquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, token, 30, TimeUnit.MINUTES);
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
                    boolean retryAcquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, token, 30, TimeUnit.MINUTES);
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
        return skuDetailService.getSkuInfoFromDB(skuId);
    }

    /**
     * 根据商品三级分类id查询商品分类信息
     *
     * @param category3Id 商品三级分类id
     */
    @ApiOperation("根据三级分类id查询商品分类信息")
    @GetMapping("/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(
            @ApiParam(name = "category3Id", value = "商品三级分类id", required = true)
            @PathVariable Long category3Id
    ) {
        return categoryViewService.getById(category3Id);
    }

    /**
     * 根据skuId查询商品实时价格
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("根据skuId查询商品实时价格")
    @GetMapping("/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId
    ) {
        SkuInfo skuInfo = skuInfoService.getById(skuId);
        return skuInfo.getPrice();
    }

    /**
     * 获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)
     *
     * @param productId 商品Id
     * @param skuId     商品skuId
     */
    @ApiOperation("获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)")
    @GetMapping("/getSpuSalePropertyAndSelected/{productId}/{skuId}")
    public List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(
            @ApiParam(name = "productId", value = "商品Id", required = true)
            @PathVariable Long productId,

            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId
    ) {
        return skuDetailService.getSpuSalePropertyAndSelected(productId, skuId);
    }

    /**
     * 获取skuId与销售属性组合的一个映射关系
     *
     * @param productId 商品Id
     */
    @ApiOperation("获取skuId与销售属性组合的一个映射关系")
    @GetMapping("/getSalePropertyAndSkuIdMapping/{productId}")
    public Map<Object, Object> getSalePropertyAndSkuIdMapping(
            @ApiParam(name = "productId", value = "商品Id", required = true)
            @PathVariable Long productId
    ) {
        HashMap<Object, Object> salePropertyAndSkuId = new HashMap<>();
        List<Map<Object, Object>> retMapList = skuSalePropertyValueMapper.getSalePropertyAndSkuIdMapping(productId);
        for (Map<Object, Object> map : retMapList) {
            salePropertyAndSkuId.put(map.get("sale_property_value_id"), map.get("sku_id"));
        }
        return salePropertyAndSkuId;
    }


}
