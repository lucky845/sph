package com.atguigu.job;

import com.atguigu.constant.MqConst;
import com.atguigu.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 定时上下架商品
 *
 * @author lucky845
 * @since 2022年04月27日
 */
@Slf4j
@EnableScheduling
@Component
public class ScheduledJob {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 秒杀商品下架操作
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void taskEveryNight01() {
        // 发送的消息
        rabbitTemplate.convertAndSend(MqConst.SCAN_SECKILL_EXCHANGE, MqConst.SCAN_SECKILL_ROUTE_KEY, "");
        log.info("开始下架商品");
    }

    /**
     * 秒杀商品的上架操作
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void taskEveryNight02() {
        rabbitTemplate.convertAndSend(MqConst.CLEAR_REDIS_EXCHANGE, MqConst.CLEAR_REDIS_ROUTE_KEY, "");
        log.info("开始上架商品");
    }

    /**
     * 布隆过滤器的维护
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void taskBloomFilter() {
        // 初始化布隆过滤器
        String result = HttpClientUtil.doGet("http://127.0.0.1:8000/init/sku/bloom");
        log.info("初始化布隆过滤器: {}", result);
    }

}
