package com.atguigu.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author lucky845
 * @date 2022年04月10日
 */
@Data
//@EnableConfigurationProperties(RedisProperties.class)
@AutoConfigureAfter(RedisProperties.class)
@Configuration
public class RedissonConfig {

    // 方式一
//    @Value("${spring.redis.host}")

    // 方式二
    @Resource
    private RedisProperties redisProperties;

    @Bean
    public RedissonClient getRedissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" +
                        redisProperties.getHost() + ":" +
                        redisProperties.getPort());
        return Redisson.create(config);
    }

}
