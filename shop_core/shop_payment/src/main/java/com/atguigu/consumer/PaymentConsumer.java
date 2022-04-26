package com.atguigu.consumer;

import com.atguigu.constant.MqConst;
import com.atguigu.entity.PaymentInfo;
import com.atguigu.enums.PaymentStatus;
import com.atguigu.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author lucky845
 * @since 2022年04月26日
 */
@Component
public class PaymentConsumer {

    @Resource
    private PaymentInfoService paymentInfoService;

    /**
     * 支付成功后修改订单状态
     *
     * @param orderId 订单id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.CLOSE_PAYMENT_QUEUE, durable = "false"),
            exchange = @Exchange(value = MqConst.CLOSE_PAYMENT_EXCHANGE, durable = "false"),
            key = {MqConst.CLOSE_PAYMENT_ROUTE_KEY}
    ))
    public void closePaymentInfo(Long orderId, Channel channel, Message message) throws IOException {
        if (orderId != null) {
            QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("order_id", orderId);
            PaymentInfo paymentInfo = paymentInfoService.getOne(wrapper);
            paymentInfo.setPaymentStatus(PaymentStatus.ClOSED.name());
            paymentInfoService.updateById(paymentInfo);
        }
        // 手动签收 deliveryTag: 签收那个消息 multiple: 是否应答多个消息 true 应答多个消息 false 代表只应答一个消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
