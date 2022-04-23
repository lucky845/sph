package com.atguigu.service;

import com.atguigu.entity.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 支付信息表 服务类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-23
 */
public interface PaymentInfoService extends IService<PaymentInfo> {

    /**
     * 返回支付二维码页面信息
     *
     * @param orderId 订单id
     */
    String createQrCode(Long orderId) throws Exception;
}
