package com.atguigu.consumer;

import com.atguigu.constant.MqConst;
import com.atguigu.entity.OrderInfo;
import com.atguigu.enums.OrderStatus;
import com.atguigu.enums.ProcessStatus;
import com.atguigu.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class OrderConsumer {

    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 超时未支付自动取消订单
     *
     * @param orderId 订单id
     */
    @RabbitListener(queues = MqConst.CANCEL_ORDER_QUEUE)
    public void cancelOrder(Channel channel, Message message, Long orderId) throws IOException {
        // 1. 修改订单状态为已关闭
        OrderInfo orderInfo = orderInfoService.getById(orderId);
        orderInfo.setOrderStatus(OrderStatus.CLOSED.name());
        orderInfo.setProcessStatus(ProcessStatus.CLOSED.name());
        orderInfoService.updateById(orderInfo);
        // 2. TODO 后续还有其他事情

        // 手动签收 deliveryTag: 签收那个消息 multiple: 是否应答多个消息 true 应答多个消息 false 代表只应答一个消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}