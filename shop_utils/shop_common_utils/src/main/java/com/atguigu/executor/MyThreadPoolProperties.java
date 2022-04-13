package com.atguigu.executor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lucky845
 * @date 2022年04月13日
 */
@Data
@ConfigurationProperties(prefix = "thread.pool")
public class MyThreadPoolProperties {

    /**
     * 核心线程数
     */
    private Integer corePoolSize = 16;

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize = 32;

    /**
     * 存活时间
     */
    private Long keepAliveTime = 50L;

    /**
     * 队列长度
     */
    private Integer queueLength = 100;

}
