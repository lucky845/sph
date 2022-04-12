package com.atguigu.cache;

import com.atguigu.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 缓存消息切面
 *
 * @author lucky845
 * @date 2022年04月12日
 */
@Component
@Aspect
public class ShopCacheAspect {

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RBloomFilter<Object> bloomFilter;

    /**
     * 使用Redisson分布式锁
     */
    //@Around("@annotation(com.atguigu.cache.ShopCache)")
    public Object cacheAroundAdvice1(ProceedingJoinPoint target) {
        // 获取方法的参数
        Object[] methodParams = target.getArgs();
        MethodSignature signature = (MethodSignature) target.getSignature();
        Method method = signature.getMethod();
        // 获取方法上的注解
        ShopCache shopCache = method.getAnnotation(ShopCache.class);
        // 获取注解参数
        String prefix = shopCache.prefix();
        boolean enableBloom = shopCache.enableBloom();
        // 获取方法参数的第一个值
        Object firstMethodParam = methodParams[0];
        // 缓存key
        String cacheKey = prefix + ":" + firstMethodParam;
        // 从缓存获取数据
        Object cacheObject = redisTemplate.opsForValue().get(cacheKey);
        // 如果缓存中没有数据，则从数据库中获取
        if (cacheObject == null) {
            // 让锁的粒度更小，提高效率
            String lockKey = "lock-" + firstMethodParam;
            // 获取锁
            RLock lock = redissonClient.getLock(lockKey);
            // 上锁
            lock.lock();
            try {
                Object targetObject = null;
                // 判断是否开启了布隆过滤器
                if (enableBloom) {
                    // 查询之前先进行判断，该id是否在布隆过滤器中存在
                    boolean contains = bloomFilter.contains(firstMethodParam);
                    // 布隆过滤器中存在才查询数据库
                    if (contains) {
                        // 执行目标方法
                        targetObject = target.proceed();
                    }
                } else {
                    // 没有开启布隆过滤器，直接执行目标方法
                    targetObject = target.proceed();
                }
                // 将数据保存到Redis
                redisTemplate.opsForValue().set(cacheKey, targetObject, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                // 返回数据库中的数据
                return targetObject;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                // 释放锁
                lock.unlock();
            }
        }
        return cacheObject;
    }

    /**
     * 使用本地锁
     */
    @Around("@annotation(com.atguigu.cache.ShopCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint target) {
        // 1. 获取方法的参数
        Object[] methodParams = target.getArgs();
        MethodSignature signature = (MethodSignature) target.getSignature();
        Method method = signature.getMethod();
        // 2. 获取方法上的注解的prefix值
        ShopCache shopCache = method.getAnnotation(ShopCache.class);
        String prefix = shopCache.prefix();
        // 3. 获取是否开启了布隆过滤器
        boolean enableBloom = shopCache.enableBloom();
        // 获取方法参数的第一个值
        Object firstMethodParam = methodParams[0];
        // 4. 缓存key
        String cacheKey = prefix + ":" + firstMethodParam;
        /*
         5. 从Redis缓存中获取数据  如果Redis缓存中有数据，直接返回
            如果没有，执行方法，并将结果放入Redis缓存
            如果开启了布隆过滤器，则判断是否命中 如果命中才查询数据库
            如果没有开启布隆过滤器，直接执行方法，并将结果放入Redis缓存
        */
        Object cacheObject = redisTemplate.opsForValue().get(cacheKey);
        // 如果缓存中没有数据，则从数据库中获取
        if (cacheObject == null) {
            // 让锁的粒度更小，提高效率
            String lockKey = "lock-" + firstMethodParam;
            /**
             * 这个地方的this代表当前JVM对象，只有一个this对象
             * lockKey.intern() 本地锁
             */
            synchronized (lockKey.intern()) {
                try {
                    Object targetObject = null;
                    // 判断是否开启了布隆过滤器
                    if (enableBloom) {
                        // 查询之前先进行判断，该id是否在布隆过滤器中存在
                        boolean contains = bloomFilter.contains(firstMethodParam);
                        // 布隆过滤器中存在才查询数据库
                        if (contains) {
                            // 执行目标方法
                            targetObject = target.proceed();
                        }
                    } else {
                        // 没有开启布隆过滤器，直接执行目标方法
                        targetObject = target.proceed();
                    }
                    // 将数据保存到Redis
                    redisTemplate.opsForValue().set(cacheKey, targetObject, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    // 返回数据库中的数据
                    return targetObject;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        return cacheObject;
    }

}
