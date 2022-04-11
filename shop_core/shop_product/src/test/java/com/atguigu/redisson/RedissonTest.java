package com.atguigu.redisson;


import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author lucky845
 * @date 2022年04月10日
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void testRedissonClient() {
        System.out.println("redissonClient = " + redissonClient);
    }

}
