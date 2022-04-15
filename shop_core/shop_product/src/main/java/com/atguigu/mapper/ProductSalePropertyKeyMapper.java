package com.atguigu.mapper;

import com.atguigu.entity.ProductSalePropertyKey;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 获取该sku对应的销售属性(只有一份)和spu所有的销售属性(全份)
     *
     * @param productId 商品Id
     * @param skuId     商品skuId
     */
    List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(@Param("productId") Long productId,
                                                               @Param("skuId") Long skuId);
}
