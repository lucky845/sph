package com.atguigu.service.impl;

import com.atguigu.entity.OrderDetail;
import com.atguigu.mapper.OrderDetailMapper;
import com.atguigu.service.OrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细表 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-20
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}
