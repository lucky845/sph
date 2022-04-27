package com.atguigu.client;

import com.atguigu.entity.OrderInfo;
import com.atguigu.fallback.OrderFallback;
import com.atguigu.result.RetVal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "shop-order", fallback = OrderFallback.class)
public interface OrderFeignClient {

    @GetMapping("/order/confirm")
    public RetVal<Map<String, Object>> confirm();

    /**
     * 获取订单信息
     *
     * @param orderId 订单id
     */
    @GetMapping("/order/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId);

    /**
     * 保存订单基本信息与详情信息
     *
     * @param orderInfo 订单信息
     */
    @PostMapping("/order/saveOrderAndDetail")
    public Long saveOrderAndDetail(@RequestBody OrderInfo orderInfo);
}
