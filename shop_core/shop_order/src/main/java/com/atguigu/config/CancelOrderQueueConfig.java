package com.atguigu.config;

import com.atguigu.constant.MqConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lucky845
 * @since 2022年04月22日
 */
@Configuration
public class CancelOrderQueueConfig {

    /**
     * 队列
     */
    @Bean
    public Queue cancelOrderQueue() {
        return new Queue(MqConst.CANCEL_ORDER_QUEUE, false);
    }

    /**
     * 由于采用延迟插件,自定义交换机
     */
    @Bean
    public CustomExchange cancelOrderExchange() {
        Map<String, Object> arguments = new HashMap<>();
        // 常规交换机类型
        arguments.put("x-delayed-type", "direct");
        return new CustomExchange(MqConst.CANCEL_ORDER_EXCHANGE, "x-delayed-message", false, true, arguments);
    }

    /**
     * 队列和交换机的绑定
     *
     * @param cancelOrderQueue    队列
     * @param cancelOrderExchange 交换机
     */
    @Bean
    public Binding bindingDelayedQueue(@Qualifier("cancelOrderQueue") Queue cancelOrderQueue,
                                       @Qualifier("cancelOrderExchange") CustomExchange cancelOrderExchange) {
        return BindingBuilder.bind(cancelOrderQueue).to(cancelOrderExchange).with(MqConst.CANCEL_ORDER_ROUTE_KEY).noargs();
    }

}
