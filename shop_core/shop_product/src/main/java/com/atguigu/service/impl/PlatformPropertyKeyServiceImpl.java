package com.atguigu.service.impl;

import com.atguigu.entity.PlatformPropertyKey;
import com.atguigu.entity.PlatformPropertyValue;
import com.atguigu.mapper.PlatformPropertyKeyMapper;
import com.atguigu.service.PlatformPropertyKeyService;
import com.atguigu.service.PlatformPropertyValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
    private PlatformPropertyValueService platformPropertyValueService;

    /**
     * 根据一二三级分类id获取平台属性信息
     *
     * @param category1Id 一级分类id
     * @param category2Id 二级分类id
     * @param category3Id 三级分类id
     */
    @Override
    public List<PlatformPropertyKey> getPlatformPropertyByCategoryId(Long category1Id, Long category2Id, Long category3Id) {
        // 1. 根据分类id查询平台属性名称
        List<PlatformPropertyKey> platformPropertyKeyList = baseMapper.getPlatformPropertyByCategoryId(category1Id, category2Id, category3Id);
        // 2. 根据平台属性id查询平台属性list
        for (PlatformPropertyKey platformPropertyKey : platformPropertyKeyList) {
            List<PlatformPropertyValue> platformPropertyValueList = platformPropertyValueService.getPlatformPropertyValueByKeyId(platformPropertyKey.getId());
            platformPropertyKey.setPlatformPropertyValueList(platformPropertyValueList);
        }
        return platformPropertyKeyList;
    }
}
