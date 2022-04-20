package com.atguigu.client;

import com.atguigu.entity.UserAddress;
import com.atguigu.fallback.UserFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "shop-user", fallback = UserFallback.class)
public interface UserFeignClient {

    /**
     * 查询收货人的信息
     *
     * @param userId 用户id
     */
    @GetMapping("/user/getUserAddressByUserId/{userId}")
    public List<UserAddress> getUserAddressByUserId(@PathVariable String userId);

}