package com.atguigu.controller;

import com.atguigu.exception.SleepUtils;
import org.redisson.api.*;
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

    private String message = "hello";

    /**
     * 测试写锁
     */
    @GetMapping("/write")
    public String write() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        // 获取写锁
        RLock writeLock = rwLock.writeLock();
        try {
            // 加锁
            writeLock.lock();
            message += UUID.randomUUID().toString();
            System.out.println(Thread.currentThread().getName() + "执行业务");
            SleepUtils.sleep(15);
        } finally {
            // 释放锁
            writeLock.unlock();
        }
        return message;
    }

    /**
     * 测试读锁
     */
    @GetMapping("/read")
    public String read() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        // 获取读锁
        RLock readLock = rwLock.readLock();
        try {
            // 加锁
            readLock.lock();
            return message;
        } finally {
            // 释放锁
            readLock.unlock();
        }
    }

    /**
     * 测试Semaphore
     * 停车场有五个车位
     * 1. 占用车位
     * 2. 离开车位
     */
    @GetMapping("/park")
    public String park() throws Exception {
        RSemaphore parkStation = redissonClient.getSemaphore("park");
        // 停车信号量减一
        parkStation.acquire(1);
        System.out.println(Thread.currentThread().getName() + "进入车位");
        return Thread.currentThread().getName() + "进入车位";
    }

    // 需要先调用left方法
    @GetMapping("/left")
    public String left() throws Exception {
        RSemaphore parkStation = redissonClient.getSemaphore("park");
        // 离开车位信号量加一
        parkStation.release(1);
        System.out.println(Thread.currentThread().getName() + "离开车位");
        return Thread.currentThread().getName() + "离开车位";
    }

    /**
     * 测试CountDownLatch
     * 所有同学全部走完班长才能锁门
     */
    @GetMapping("/ready")
    public String ready() {
        RCountDownLatch leftClassroom = redissonClient.getCountDownLatch("left_classroom");
        // 离开教室，数量减一
        leftClassroom.countDown();
        System.out.println(Thread.currentThread().getName() + "离开教室");
        return Thread.currentThread().getName() + "离开教室";
    }

    @GetMapping("/lockDoor")
    public String lockDoor() throws Exception {
        RCountDownLatch leftClassroom = redissonClient.getCountDownLatch("left_classroom");
        // 设置多少个人走了才锁门
        leftClassroom.trySetCount(6);
        // 离开教室，数量减一
        leftClassroom.await();
        System.out.println(Thread.currentThread().getName() + "班长锁门");
        return Thread.currentThread().getName() + "班长锁门";
    }

}
