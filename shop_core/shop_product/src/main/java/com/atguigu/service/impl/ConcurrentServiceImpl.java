package com.atguigu.service.impl;

import com.atguigu.service.ConcurrentService;
import com.atguigu.utils.SleepUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
     * 3. 测试分布式锁: 使用Redis的setnx命令
     * 问题: 使用Redis的setnx命令,如果doBusiness()出现异常，锁会一直占用，无法释放
     * 解决方法: 设置一个超时时间，超时后自动释放锁
     */
    public void setNum3() {
        boolean acquireLock = redisTemplate.opsForValue().setIfAbsent("lock", "ok");
        if (acquireLock) {
            // 拿到锁
            doBusiness();
            // 业务结束，释放锁
            redisTemplate.delete("lock");
        } else {
            // 如果没有拿到锁，就递归
            setNum3();
        }
    }

    /**
     * 4. 测试分布式锁: 设置一个超时时间，超时后自动释放锁
     * 问题: 多线程操作，因为业务时间和超时时间不一致，可能会释放其他线程的锁，导致数据错误
     * 解决方法: 添加一个标记，释放前先判断是否是自己的锁
     */
    public void setNum4() {
        // 使用Redis的setnx命令,设置一个超时时间，3秒后自动释放锁
        boolean acquireLock = redisTemplate.opsForValue().setIfAbsent("lock", "ok", 3, TimeUnit.SECONDS);
        if (acquireLock) {
            // 拿到锁
            doBusiness();
            // 业务执行5秒
            SleepUtils.sleep(5);
            // 业务结束，释放锁
            redisTemplate.delete("lock");
        } else {
            // 如果没有拿到锁，就递归
            setNum4();
        }
    }

    /**
     * 5. 测试分布式锁: 添加一个标记，释放前先判断是否是自己的锁
     * 问题: 判断和删除是两个操作，不满足原子性，可能会出现数据错误
     * 解决方法: 使用lua脚本，实现原子性操作
     */
    public void setNum5() {
        // 放一个锁的标记
        String token = UUID.randomUUID().toString();
        // 使用Redis的setnx命令,设置一个超时时间，3秒后自动释放锁
        boolean acquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token, 3, TimeUnit.SECONDS);
        if (acquireLock) {
            // 拿到锁
            doBusiness();
            // 从redis获取标记
            String redisToken = (String) redisTemplate.opsForValue().get("lock");
            // 判断是否是自己的锁
            if (token.equals(redisToken)) {
                // 业务结束，释放锁
                redisTemplate.delete("lock");
            }
        } else {
            // 如果没有拿到锁，就递归
            setNum5();
        }
    }

    /**
     * 6. 测试分布式锁: 使用lua脚本，实现原子性操作
     * 问题: 会出现栈溢出异常,引起结果不准确
     * 性能问题-->假若在拿锁的时候还有很多操作,因为递归,那么每次就会执行多次
     * 解决方法: 设置 栈空间的大小 -Xss15M   使用自旋,只去获取锁
     */
    public void setNum6() {
        // 放一个锁的标记
        String token = UUID.randomUUID().toString();
        // 使用Redis的setnx命令,设置一个超时时间，3秒后自动释放锁
        boolean acquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token, 3, TimeUnit.SECONDS);
        if (acquireLock) {
            // 拿到锁
            doBusiness();
            luaBusiness(token);
        } else {
            // 如果没有拿到锁，就递归
            setNum6();
        }
    }

    /**
     * lua脚本执行原子性操作，判断锁是否是自己的并删除锁
     *
     * @param token 锁的标记
     */
    private void luaBusiness(String token) {
        // 使用lua脚本,实现原子性操作,判断是否是自己的锁,并删除锁
        String luaScript = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        // 把脚本封装到redisScript
        redisScript.setScriptText(luaScript);
        // 设置执行完成之后返回上面类型的数据
        redisScript.setResultType(Long.class);
        // 准备执行脚本 public <T> T execute(RedisScript<T> script, List<K> keys, Object... args)
        redisTemplate.execute(redisScript, Collections.singletonList("lock"), token);
    }

    /**
     * 7. 测试分布式锁: 使用自旋，只去获取锁
     * 问题: 出现死锁
     * 解决方法: 告知已经获取到锁了,使用一个map存储状态(重入锁的设计)
     */
    public void setNum7() {
        // 获取锁之前还有100行代码.....
        // 放一个锁的标记
        String token = UUID.randomUUID().toString();
        boolean acquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token, 30, TimeUnit.MINUTES);
        if (acquireLock) {
            doBusiness();
            luaBusiness(token);
        } else {
            // 自旋获取锁
            for (; ; ) {
                SleepUtils.sleepMillis(50);
                boolean retryAcquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token, 30, TimeUnit.MINUTES);
                if (retryAcquireLock) {
                    // 拿到了锁
                    break;
                }
            }
            // 如果没有拿到锁，就递归
            setNum7();
        }
    }

    private final Map<Thread, Boolean> threadMap = new HashMap<>();

    /**
     * 8. 测试分布式锁: 告知已经获取到锁了,使用一个map存储状态(重入锁的设计)
     * 问题: local未删除
     * 解决方法: 将mao的值设置为token，因为token有超时时间，会自动删除
     */
    @Override
    public void setNum() {
        // 获取锁之前还有100行代码.....
        // 从map中获取锁的标记
        Boolean flag = threadMap.get(Thread.currentThread());
        String token = null;
        boolean acquireLock = false;
        // 代表线程刚进来，还没有自旋过
        if (flag == null || !flag) {
            // 放一个锁的标记
            token = UUID.randomUUID().toString();
            acquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token, 30, TimeUnit.MINUTES);
        } else {
            // 已经进来过了，一定是自旋获取到了锁
            acquireLock = true;
        }
        // 判断是否获取到锁
        if (acquireLock) {
            doBusiness();
            luaBusiness(token);
            // 移除锁标记，防止内存溢出
            threadMap.remove(Thread.currentThread());
        } else {
            // 自旋获取锁
            for (; ; ) {
                SleepUtils.sleepMillis(50);
                boolean retryAcquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token, 30, TimeUnit.MINUTES);
                if (retryAcquireLock) {
                    // 拿到了锁，将锁标记到一个map中
                    threadMap.put(Thread.currentThread(), true);
                    break;
                }
            }
            // 如果没有拿到锁，就递归
            setNum();
        }
    }
}
