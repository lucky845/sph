package com.atguigu.mapper;

import com.atguigu.entity.SkuSalePropertyValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * sku销售属性值 Mapper 接口
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
public interface SkuSalePropertyValueMapper extends BaseMapper<SkuSalePropertyValue> {

    /**
     * 获取skuId与销售属性组合的一个映射关系
     *
     * @param productId 商品Id
     */
    List<Map<Object, Object>> getSalePropertyAndSkuIdMapping(Long productId);
}
