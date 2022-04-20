package com.atguigu.fallback;

import com.atguigu.client.UserFeignClient;
import com.atguigu.entity.UserAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lucky845
 * @since 2022年04月20日
 */
public class UserFallback implements UserFeignClient {
    /**
     * 查询收货人的信息
     *
     * @param userId 用户id
     */
    @Override
    public List<UserAddress> getUserAddressByUserId(String userId) {
        return new ArrayList<>();
    }
}
