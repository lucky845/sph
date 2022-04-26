package com.atguigu.fallback;

import com.atguigu.client.PaymentFeignClient;
import com.atguigu.entity.PaymentInfo;

/**
 * @author lucky845
 * @since 2022年04月26日
 */
public class PaymentFallback implements PaymentFeignClient {
    /**
     * 支付宝退款接口
     *
     * @param orderId 订单号
     */
    @Override
    public boolean refund(Long orderId) {
        return false;
    }

    /**
     * 查询支付宝中是否有记录
     *
     * @param orderId 订单id
     */
    @Override
    public boolean queryAlipayTrade(Long orderId) {
        return false;
    }

    /**
     * 交易关闭
     *
     * @param orderId 订单id
     */
    @Override
    public boolean closeAlipayTrade(Long orderId) {
        return false;
    }

    /**
     * 根据outTradeNo查询支付表信息
     *
     * @param outTradeNo 订单流水号
     */
    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo) {
        return new PaymentInfo();
    }
}
