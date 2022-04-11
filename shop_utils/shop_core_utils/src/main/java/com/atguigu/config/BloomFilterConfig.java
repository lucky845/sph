package com.atguigu.config;

import com.atguigu.constant.RedisConst;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置类
 *
 * @author lucky845
 * @date 2022年04月11日
 */
@Configuration
public class BloomFilterConfig {

//    @Resource
//    private RedissonClient redissonClient;

    @Bean
    public RBloomFilter<Object> getBloomFilter(RedissonClient redissonClient) {
        // 设置布隆过滤器的名称
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.BLOOM_SKU_ID);
        // 初始化布隆过滤器，参数为布隆过滤器的可信性，建议设置为常量
        bloomFilter.tryInit(100000000L, 0.0001);
        return bloomFilter;
    }

}
