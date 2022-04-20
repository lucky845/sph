package com.atguigu.client;

import com.atguigu.fallback.OrderFallback;
import com.atguigu.result.RetVal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(value = "shop-order", fallback = OrderFallback.class)
public interface OrderFeignClient {

    @GetMapping("/order/confirm")
    public RetVal<Map<String, Object>> confirm();

}
