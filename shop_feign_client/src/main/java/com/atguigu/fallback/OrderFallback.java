package com.atguigu.fallback;

import com.atguigu.client.OrderFeignClient;
import com.atguigu.entity.OrderInfo;
import com.atguigu.result.RetVal;

import java.util.Map;

/**
 * @author lucky845
 * @since 2022年04月20日
 */
public class OrderFallback implements OrderFeignClient {

    @Override
    public RetVal<Map<String, Object>> confirm() {
        return RetVal.ok();
    }

    /**
     * 获取订单信息
     *
     * @param orderId 订单id
     */
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return new OrderInfo();
    }

    /**
     * 把保存订单基本信息与详情信息封装为一个接口
     *
     * @param orderInfo 订单信息
     */
    @Override
    public Long saveOrderAndDetail(OrderInfo orderInfo) {
        return -1L;
    }
}
