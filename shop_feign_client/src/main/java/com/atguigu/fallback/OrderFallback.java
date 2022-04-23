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
}
