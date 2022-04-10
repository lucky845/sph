package com.atguigu.controller;

import com.atguigu.utils.SleepUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 测试Redisson作为分布式锁
 *
 * @author lucky845
 * @date 2022年04月10日
 */
@RestController
@RequestMapping("/product")
public class RedissonController {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 测试lock锁: 最简单的分布式锁
     * 1. Redisson加锁，默认有30秒的过期时间，不会出现死锁
     * 2. 每隔10秒就会自动续期
     */
    @GetMapping("/lock")
    public String lock() {
        RLock lock = redissonClient.getLock("lock");
        // 加锁，一直等到加锁成功，才会继续，一般采用这种模式
        lock.lock();
        String uuid = UUID.randomUUID().toString();
        try {
            System.out.println(Thread.currentThread().getName() + "执行业务" + uuid);
            SleepUtils.sleep(40);
        } finally {
            // 释放锁
            lock.unlock();
        }
        return Thread.currentThread().getName() + ":" + uuid;
    }

    /**
     * 测试lock锁，并设置超时时间，一般不采用这种方式，这种方式不会自动续期
     */
    @GetMapping("/lockWithTimeOut")
    public String lockWithTimeOut() {
        RLock lock = redissonClient.getLock("lock");
        // 加锁，设置锁10秒自动过期，一般不采用这种方式，这种方式不会自动续期
        lock.lock(5, TimeUnit.SECONDS);
        String uuid = UUID.randomUUID().toString();
        try {
            System.out.println(Thread.currentThread().getName() + "执行业务" + uuid);
            SleepUtils.sleep(40);
        } finally {
            // 释放锁
            lock.unlock();
        }
        return Thread.currentThread().getName() + ":" + uuid;
    }

    /**
     * 测试tryLock，10s以内尝试拿锁，如果能拿到锁，给锁的过期时间设置为35秒，一般不采用这种方法
     */
    @GetMapping("/tryLock")
    public String tryLock() throws Exception {
        RLock lock = redissonClient.getLock("lock");
        // 10s以内尝试拿锁，如果能拿到锁，给锁的过期时间设置为35秒，一般不采用这种方法
        lock.tryLock(10, 35, TimeUnit.SECONDS);
        String uuid = UUID.randomUUID().toString();
        try {
            System.out.println(Thread.currentThread().getName() + "执行业务" + uuid);
            SleepUtils.sleep(40);
        } finally {
            // 释放锁
            lock.unlock();
        }
        return Thread.currentThread().getName() + ":" + uuid;
    }


}
