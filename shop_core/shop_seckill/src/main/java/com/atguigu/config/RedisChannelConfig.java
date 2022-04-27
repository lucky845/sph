package com.atguigu.config;

import com.atguigu.constant.RedisConst;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisChannelConfig {

    /**
     * 定义发布订阅 当有消息的时候接收处理消息的方法
     *
     * @param shopMessageListener 处理消息的类
     */
    @Bean
    MessageListenerAdapter messageListenerAdapter(ShopMessageListener shopMessageListener) {
        return new MessageListenerAdapter(shopMessageListener, "receiveChannelMessage");
    }

    /**
     * 连接的Redis是谁 用哪个类处理消息 订阅的那个频道
     *
     * @param factory                Redis连接工厂
     * @param messageListenerAdapter 消息监听适配器
     */
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory factory, MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(factory);
        // 订阅那个频道
        listenerContainer.addMessageListener(messageListenerAdapter, new PatternTopic(RedisConst.PREPARE_PUB_SUB_SECKILL));
        return listenerContainer;
    }
}
