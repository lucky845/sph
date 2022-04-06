package com.atguigu.mapper;

import com.atguigu.entity.ProductSalePropertyKey;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * spu销售属性 Mapper 接口
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
public interface ProductSalePropertyKeyMapper extends BaseMapper<ProductSalePropertyKey> {

    /**
     * 根据SPUId查询销售属性
     *
     * @param spuId 商品SPUId
     */
    List<ProductSalePropertyKey> querySalePropertyByProductId(Long spuId);
}
