package com.atguigu.mapper;

import com.atguigu.entity.PlatformPropertyKey;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import feign.Param;

import java.util.List;

/**
 * <p>
 * 属性表 Mapper 接口
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
public interface PlatformPropertyKeyMapper extends BaseMapper<PlatformPropertyKey> {

    /**
     * 根据一二三级分类id获取平台属性信息
     *
     * @param category1Id 一级分类id
     * @param category2Id 二级分类id
     * @param category3Id 三级分类id
     */
    List<PlatformPropertyKey> getPlatformPropertyByCategoryId(@Param("category1Id") Long category1Id,
                                                              @Param("category2Id") Long category2Id,
                                                              @Param("category3Id") Long category3Id);
}
