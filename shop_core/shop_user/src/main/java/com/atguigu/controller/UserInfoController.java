package com.atguigu.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.constant.RedisConst;
import com.atguigu.entity.UserInfo;
import com.atguigu.result.RetVal;
import com.atguigu.service.UserInfoService;
import com.atguigu.util.IpUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-18
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 登陆的时候的认证逻辑
     *
     * @param userInfo 用户信息
     */
    @ApiOperation("用户登陆")
    @PostMapping("/login")
    public RetVal<Object> login(
            @ApiParam(name = "userInfo", value = "用户信息", required = true)
            @RequestBody UserInfo userInfo,
            HttpServletRequest request
    ) {
        // 1. 根据用户账户密码查询用户信息
        UserInfo dbUserInfo = userInfoService.queryUserInfoFromDB(userInfo);
        if (dbUserInfo != null) {
            Map<String, Object> retValMap = new HashMap<>();
            // 2. 返回一个token给前端
            String token = UUID.randomUUID().toString();
            retValMap.put("token", token);
            // 3. 返回用户昵称给前端
            String nickName = dbUserInfo.getNickName();
            retValMap.put("nickName", nickName);
            // 4. 将用户信息保存到Redis中
            String userKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
            JSONObject userInfoJson = new JSONObject();
            userInfoJson.put("userId", dbUserInfo.getId());
            userInfoJson.put("loginIp", IpUtil.getIpAddress(request));
            redisTemplate.opsForValue().set(userKey, userInfoJson, RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            return RetVal.ok(retValMap);
        } else {
            return RetVal.fail().message("登陆失败");
        }
    }

    /**
     * 用户退出登陆
     */
    @ApiOperation("用户退出登陆")
    @GetMapping("/logout")
    public RetVal<Object> logout(HttpServletRequest request) {
        // 请求在前端拦截放了一份在请求头中，所以可以从请求头中获取到token信息
        String userKey = RedisConst.USER_LOGIN_KEY_PREFIX + request.getHeader("token");
        // 删除Redis中的用户信息
        redisTemplate.delete(userKey);
        return RetVal.ok();
    }


}

