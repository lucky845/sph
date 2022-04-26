package com.atguigu.controller;

import com.atguigu.constant.MqConst;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 模拟定时上架秒杀商品
 *
 * @author lucky845
 * @since 2022年04月26日
 */
@RestController
public class SimulateQuartzController {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送上架秒杀商品的通知
     */
    @GetMapping("/sendMsgToScanSeckill")
    public String sendMsgToScanSeckill() {
        // 发送消息起到通知作用
        rabbitTemplate.convertAndSend(MqConst.SCAN_SECKILL_EXCHANGE, MqConst.SCAN_SECKILL_ROUTE_KEY, "");
        return "success";
    }

    /**
     * 发送下架秒杀商品的通知
     */
    @GetMapping("/sendMsgToClearSeckill")
    public String sendMsgToClearSeckill() {
        // 发送消息起到通知作用
        rabbitTemplate.convertAndSend(MqConst.CLEAR_REDIS_EXCHANGE, MqConst.CLEAR_REDIS_ROUTE_KEY, "");
        return "success";
    }

}
