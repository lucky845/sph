package com.atguigu.service.impl;

import com.atguigu.entity.PlatformPropertyValue;
import com.atguigu.mapper.PlatformPropertyValueMapper;
import com.atguigu.service.PlatformPropertyValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 属性值表 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
@Service
public class PlatformPropertyValueServiceImpl extends ServiceImpl<PlatformPropertyValueMapper, PlatformPropertyValue> implements PlatformPropertyValueService {

    /**
     * 根据平台属性id查询平台属性list
     *
     * @param KeyId 平台属性id
     */
    @Override
    public List<PlatformPropertyValue> getPlatformPropertyValueByKeyId(Long KeyId) {
        QueryWrapper<PlatformPropertyValue> platformPropertyValueQueryWrapper = new QueryWrapper<>();
        platformPropertyValueQueryWrapper.eq("property_key_id", KeyId);
        return baseMapper.selectList(platformPropertyValueQueryWrapper);
    }
}
