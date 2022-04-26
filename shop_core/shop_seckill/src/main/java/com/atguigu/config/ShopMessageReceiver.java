package com.atguigu.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * @author lucky845
 * @since 2022年04月26日
 */
@Slf4j
@Component
public class ShopMessageReceiver {

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 接收订阅消息
     *
     * @param message 消息
     */
    public void receiveChannelMessage(String message) {
        log.info("收到了redis发布的订阅消息");
        if (!StringUtils.isEmpty(message)) {
            // 把接收到的消费者的消息进行解析并且存储标志位
            message = message.replaceAll("\"", "");
            String[] messageSplit = message.split(":");
            if (messageSplit.length == 2) {
                // 商品id
                String skuIdString = messageSplit[0];
                // 商品的状态位
                String state = messageSplit[1];
                redisTemplate.opsForValue().set(skuIdString, state);
            }
        }
    }

}
