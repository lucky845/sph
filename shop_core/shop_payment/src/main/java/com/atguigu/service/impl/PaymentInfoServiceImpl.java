package com.atguigu.service.impl;

import com.atguigu.entity.PaymentInfo;
import com.atguigu.mapper.PaymentInfoMapper;
import com.atguigu.service.PaymentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付信息表 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-23
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

}
