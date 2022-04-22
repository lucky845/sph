package com.atguigu.consumer;

import com.atguigu.client.SearchFeignClient;
import com.atguigu.constant.MqConst;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author lucky845
 * @since 2022年04月22日
 */
public class EsConsumer {

    @Resource
    private SearchFeignClient searchFeignClient;

    /**
     * 接受商品上架消息(默认是自动签收)
     *
     * @param skuId   商品skuId
     * @param channel 通道
     * @param message 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.ON_SALE_QUEUE, durable = "false"),
            exchange = @Exchange(value = MqConst.ON_OFF_SALE_EXCHANGE, durable = "false"),
            key = {MqConst.ON_SALE_ROUTING_KEY}))
    public void onSale(Long skuId, Channel channel, Message message) throws IOException {
        if (skuId != null) {
            searchFeignClient.onSale(skuId);
        }
        // 手动签收 deliveryTag: 签收那个消息 multiple: 是否应答多个消息 true 应答多个消息 false 代表只应答一个消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 接受商品下架消息(默认是自动签收)
     *
     * @param skuId   商品skuId
     * @param channel 通道
     * @param message 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.OFF_SALE_QUEUE, durable = "false"),
            exchange = @Exchange(value = MqConst.ON_OFF_SALE_EXCHANGE, durable = "false"),
            key = {MqConst.OFF_SALE_ROUTING_KEY}))
    public void offSale(Long skuId, Channel channel, Message message) throws IOException {
        if (skuId != null) {
            searchFeignClient.offSale(skuId);
        }
        // 手动签收 deliveryTag: 签收那个消息 multiple: 是否应答多个消息 true 应答多个消息 false 代表只应答一个消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


}
