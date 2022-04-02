package com.atguigu.service;

import com.atguigu.entity.PlatformPropertyValue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 属性值表 服务类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
public interface PlatformPropertyValueService extends IService<PlatformPropertyValue> {

    /**
     * 根据平台属性id查询平台属性list
     *
     * @param KeyId 平台属性id
     */
    List<PlatformPropertyValue> getPlatformPropertyValueByKeyId(Long KeyId);
}
