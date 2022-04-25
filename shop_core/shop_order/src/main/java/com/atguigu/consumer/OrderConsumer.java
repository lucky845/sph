package com.atguigu.consumer;

import com.alibaba.fastjson.JSON;
import com.atguigu.constant.MqConst;
import com.atguigu.entity.OrderInfo;
import com.atguigu.enums.ProcessStatus;
import com.atguigu.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import jodd.util.StringUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

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
        orderInfoService.updateOrderStatus(orderInfo, ProcessStatus.CLOSED);
        // 2. TODO 后续还有其他事情

        // 手动签收 deliveryTag: 签收那个消息 multiple: 是否应答多个消息 true 应答多个消息 false 代表只应答一个消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 支付成功后更新订单信息
     *
     * @param orderId 订单id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.PAY_ORDER_QUEUE, durable = "false"),
            exchange = @Exchange(value = MqConst.PAY_ORDER_EXCHANGE, durable = "false", autoDelete = "true"),
            key = MqConst.PAY_ORDER_ROUTE_KEY
    ))
    public void updateOrderAfterPaySuccess(Long orderId, Message message, Channel channel) throws Exception {
        if (orderId != null) {
            OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
            // 订单状态为未支付的时候才修改
            if (orderInfo != null && orderInfo.getProcessStatus().equals(ProcessStatus.UNPAID.getOrderStatus().name())) {
                // 修改订单状态
                orderInfoService.updateOrderStatus(orderInfo, ProcessStatus.PAID);
                // 发送消息通知仓库系统减库存
                orderInfoService.sendMsgToWareHouse(orderInfo);
            }
        }
        // 手动签收 deliveryTag: 签收那个消息 multiple: 是否应答多个消息 true 应答多个消息 false 代表只应答一个消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 发送减库存成功的消息
     *
     * @param jsonData json类型的参数
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.SUCCESS_DECREASE_STOCK_QUEUE, durable = "false"),
            exchange = @Exchange(value = MqConst.SUCCESS_DECREASE_STOCK_EXCHANGE, durable = "false", autoDelete = "true"),
            key = MqConst.SUCCESS_DECREASE_STOCK_ROUTE_KEY
    ))
    public void updateOrderAfterDecreaseStock(String jsonData, Message message, Channel channel) throws Exception {
        if (!StringUtil.isEmpty(jsonData)) {
            // 将json字符串转为map
            Map<String, Object> map = JSON.parseObject(jsonData, Map.class);
            String orderId = (String) map.get("orderId");
            String status = (String) map.get("status");
            // 仓库系统减库存成功,将订单状态改为等待发货
            OrderInfo orderInfo = orderInfoService.getOrderInfo(Long.parseLong(orderId));
            // 判断订单状态是否为已减库存
            if ("DEDUCTED".equals(status)) {
                // 减库存成功,待发货
                orderInfoService.updateOrderStatus(orderInfo, ProcessStatus.WAITING_DELEVER);
            } else {
                // 减库存失败(超卖),库存异常
                orderInfoService.updateOrderStatus(orderInfo, ProcessStatus.STOCK_EXCEPTION);
            }
        }
        // 手动签收 deliveryTag: 签收那个消息 multiple: 是否应答多个消息 true 应答多个消息 false 代表只应答一个消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}