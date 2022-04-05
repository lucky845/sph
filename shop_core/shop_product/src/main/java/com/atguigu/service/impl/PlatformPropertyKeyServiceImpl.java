package com.atguigu.service.impl;

import com.atguigu.entity.PlatformPropertyKey;
import com.atguigu.entity.PlatformPropertyValue;
import com.atguigu.mapper.PlatformPropertyKeyMapper;
import com.atguigu.service.PlatformPropertyKeyService;
import com.atguigu.service.PlatformPropertyValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 属性表 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
@Service
public class PlatformPropertyKeyServiceImpl extends ServiceImpl<PlatformPropertyKeyMapper, PlatformPropertyKey> implements PlatformPropertyKeyService {

    @Resource
    private PlatformPropertyValueService propertyValueService;

    /**
     * 根据一二三级分类id获取平台属性信息(优化版)
     *
     * @param category1Id 一级分类id
     * @param category2Id 二级分类id
     * @param category3Id 三级分类id
     */
    @Override
    public List<PlatformPropertyKey> getPlatformPropertyByCategoryId(Long category1Id, Long category2Id, Long category3Id) {
        return baseMapper.getPlatformPropertyByCategoryId(category1Id, category2Id, category3Id);
    }

    /**
     * 保存修改平台属性key与value
     *
     * @param platformPropertyKey 平台属性key对象
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void savePlatformProperty(PlatformPropertyKey platformPropertyKey) {
        if (platformPropertyKey.getId() != null) {
            // 修改
            baseMapper.updateById(platformPropertyKey);
        } else {
            // 保存
            baseMapper.insert(platformPropertyKey);
        }
        QueryWrapper<PlatformPropertyValue> wrapper = new QueryWrapper<>();
        wrapper.eq("property_key_id", platformPropertyKey.getId());
        propertyValueService.remove(wrapper);
        List<PlatformPropertyValue> propertyValueList = platformPropertyKey.getPropertyValueList();
        if (!CollectionUtils.isEmpty(propertyValueList)) {
            propertyValueService.saveBatch(propertyValueList);
        }
    }
}
