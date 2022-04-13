package com.atguigu.executor;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lucky845
 * @date 2022年04月13日
 */
@EnableConfigurationProperties(MyThreadPoolProperties.class)
@Configuration
public class MyThreadPoolConfig {

    @Resource
    private MyThreadPoolProperties myThreadPoolProperties;

    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor() {
        // 采用LinkedBlockingQueue不会产生空间碎片
        // 采用ArrayBlockingQueue会产生空间碎片,导致内存空间不连续
        return new ThreadPoolExecutor(
                myThreadPoolProperties.getCorePoolSize(), // 核心线程数
                myThreadPoolProperties.getMaximumPoolSize(), // 最大线程数
                myThreadPoolProperties.getKeepAliveTime(), // 存活时间
                TimeUnit.SECONDS, // 时间单位
                new LinkedBlockingQueue<>(myThreadPoolProperties.getQueueLength()), // 阻塞队列
                Executors.defaultThreadFactory(), // 线程工厂
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
    }
}
