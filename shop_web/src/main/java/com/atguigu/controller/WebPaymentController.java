package com.atguigu.controller;

import com.atguigu.client.OrderFeignClient;
import com.atguigu.entity.OrderInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author lucky845
 * @since 2022年04月23日
 */
@Controller
public class WebPaymentController {

    @Resource
    private OrderFeignClient orderFeignClient;

    /**
     * 跳转到支付页面
     *
     * @param orderId 订单id
     */
    @GetMapping("pay.html")
    public String payment(@RequestParam Long orderId, HttpServletRequest request) {
        // 远程调用order模块获得订单信息
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        request.setAttribute("orderInfo", orderInfo);
        return "payment/pay";
    }

}

