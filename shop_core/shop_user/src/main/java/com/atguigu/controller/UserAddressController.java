package com.atguigu.controller;


import com.atguigu.entity.UserAddress;
import com.atguigu.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 用户地址表 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-18
 */
@Api(tags = "用户地址信息")
@RestController
@RequestMapping("/user")
public class UserAddressController {

    @Resource
    private UserAddressService userAddressService;

    /**
     * 查询收货人的信息
     *
     * @param userId 用户id
     */
    @ApiOperation("查询收货人的信息")
    @GetMapping("/getUserAddressByUserId/{userId}")
    public List<UserAddress> getUserAddressByUserId(
            @ApiParam(name = "", value = "", required = true)
            @PathVariable String userId
    ) {
        QueryWrapper<UserAddress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return userAddressService.list(wrapper);
    }

}

