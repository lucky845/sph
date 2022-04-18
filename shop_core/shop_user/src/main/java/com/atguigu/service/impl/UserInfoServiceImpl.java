package com.atguigu.service.impl;

import com.atguigu.entity.UserInfo;
import com.atguigu.mapper.UserInfoMapper;
import com.atguigu.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-18
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    /**
     * 根据用户账户密码查询用户信息
     *
     * @param userInfo 封装了用户账户密码信息
     */
    @Override
    public UserInfo queryUserInfoFromDB(UserInfo userInfo) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("login_name", userInfo.getLoginName());
        String password = userInfo.getPasswd();
        // 对页面传过来的密码进行加密
        String encodedPasswd = DigestUtils.md5DigestAsHex(password.getBytes());
        wrapper.eq("passwd", encodedPasswd);
        return baseMapper.selectOne(wrapper);
    }
}
