package com.atguigu.service.impl;

import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.mapper.ProductSalePropertyKeyMapper;
import com.atguigu.service.ProductSalePropertyKeyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * spu销售属性 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
@Service
public class ProductSalePropertyKeyServiceImpl extends ServiceImpl<ProductSalePropertyKeyMapper, ProductSalePropertyKey> implements ProductSalePropertyKeyService {


    /**
     * 根据SPUId查询销售属性
     *
     * @param spuId 商品SPUId
     */
    @Override
    public List<ProductSalePropertyKey> querySalePropertyByProductId(Long spuId) {
        return baseMapper.querySalePropertyByProductId(spuId);
    }
}
