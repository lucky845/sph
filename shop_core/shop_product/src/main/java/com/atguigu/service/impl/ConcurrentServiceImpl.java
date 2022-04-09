package com.atguigu.service.impl;

import com.atguigu.service.ConcurrentService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * @author lucky845
 * @date 2022年04月09日
 */
@Service
public class ConcurrentServiceImpl implements ConcurrentService {

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 抽取出来的业务代码
     */
    private void doBusiness() {
        String value = (String) redisTemplate.opsForValue().get("num");
        if (StringUtils.isEmpty(value)) {
            redisTemplate.opsForValue().set("num", "1");
        } else {
            redisTemplate.opsForValue().set("num", Integer.parseInt(value) + 1 + "");
        }
    }

    /**
     * 1. 测试分布式锁: 未使用锁，在高并发情况下，数据异常
     * 问题: 使用ab -n 1000 - c 100 192.168.229.2:8000/product/setNum 进行高并发访问测试,数据异常
     * 解决方法: 使用synchronized加锁
     */
    public void setNum1() {
        doBusiness();
    }

    /**
     * 2. 测试分布式锁: 在单台服务器的情况下，数据加锁成功，数据未出现异常
     * 问题: 使用idea配置两个微服务,这样就有两个JVM，导致加锁对象不是同一个，出现数据异常
     * 解决方法: 使用Redis的setnx命令-->set if not exists
     */
    public synchronized void setNum2() {
        doBusiness();
    }

    /**
     * 3. 测试分布式锁
     * 问题: 使用Redis的setnx命令,如果doBusiness()出现异常，锁会一直占用，无法释放
     * 解决方法: 设置一个超时时间，超时后自动释放锁
     */
    @Override
    public void setNum() {
        boolean acquireLock = redisTemplate.opsForValue().setIfAbsent("lock", "ok");
        if (acquireLock) {
            // 拿到锁
            doBusiness();
            // 业务结束，释放锁
            redisTemplate.delete("lock");
        } else {
            // 如果没有拿到锁，就递归
            setNum();
        }
    }
}
