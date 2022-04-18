package com.atguigu.service;

import com.atguigu.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-18
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 根据用户账户密码查询用户信息
     *
     * @param userInfo 封装了用户账户密码信息
     */
    UserInfo queryUserInfoFromDB(UserInfo userInfo);

}
