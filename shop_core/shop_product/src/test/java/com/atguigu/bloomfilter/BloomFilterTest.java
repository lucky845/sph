package com.atguigu.bloomfilter;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author lucky845
 * @date 2022年04月11日
 */
@SpringBootTest
public class BloomFilterTest {

    @Resource
    private RBloomFilter<Object> bloomFilter;

    @Test
    public void testBloom() {
        boolean contains = bloomFilter.contains(24L);
        System.out.println("contains = " + contains);
        contains = bloomFilter.contains(25L);
        System.out.println("contains = " + contains);
        contains = bloomFilter.contains(1L);
        System.out.println("contains = " + contains);
    }

}
