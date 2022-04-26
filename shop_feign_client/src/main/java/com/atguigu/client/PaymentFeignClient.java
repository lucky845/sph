package com.atguigu.client;

import com.atguigu.entity.PaymentInfo;
import com.atguigu.fallback.PaymentFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "shop-payment", fallback = PaymentFallback.class)
public interface PaymentFeignClient {

    /**
     * 支付宝退款接口
     *
     * @param orderId 订单号
     */
    @GetMapping("/payment/refund/{orderId}")
    public boolean refund(@PathVariable Long orderId);

    /**
     * 查询支付宝中是否有记录
     *
     * @param orderId 订单id
     */
    @GetMapping("/payment/queryAlipayTrade/{orderId}")
    public boolean queryAlipayTrade(@PathVariable Long orderId);

    /**
     * 交易关闭
     *
     * @param orderId 订单id
     */
    @GetMapping("/payment/closeAlipayTrade/{orderId}")
    public boolean closeAlipayTrade(@PathVariable Long orderId);

    /**
     * 根据outTradeNo查询支付表信息
     *
     * @param outTradeNo 订单流水号
     */
    @GetMapping("/payment/getPaymentInfo/{outTradeNo}")
    public PaymentInfo getPaymentInfo(String outTradeNo);
}
