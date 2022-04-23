package com.atguigu.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.client.OrderFeignClient;
import com.atguigu.config.AlipayConfig;
import com.atguigu.entity.OrderInfo;
import com.atguigu.entity.PaymentInfo;
import com.atguigu.enums.PaymentStatus;
import com.atguigu.enums.PaymentType;
import com.atguigu.mapper.PaymentInfoMapper;
import com.atguigu.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * <p>
 * 支付信息表 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-23
 */
@Slf4j
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    @Resource
    private OrderFeignClient orderFeignClient;

    @Resource
    private AlipayClient alipayClient;

    /**
     * 返回支付二维码页面信息
     *
     * @param orderId 订单id
     */
    @Override
    public String createQrCode(Long orderId) throws Exception {
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        // 保存支付信息
        savePaymentInfo(orderInfo, PaymentType.ALIPAY.name());
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 设置同步回调地址
        request.setReturnUrl(AlipayConfig.return_payment_url);
        // 设置异步回调地址
        request.setNotifyUrl(AlipayConfig.notify_payment_url);
        // 在公共参数中设置回跳和通知地址
        JSONObject bizContent = new JSONObject();
        //商户订单号
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        //订单总金额
        bizContent.put("total_amount", orderInfo.getTotalMoney());
        //订单标题
        bizContent.put("subject", "天气转热 买个锤子手机");
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            log.info("调用支付成功");
            return response.getBody();
        } else {
            log.info("调用支付失败");
        }
        return null;
    }

    /**
     * 保存支付信息
     *
     * @param orderInfo   订单信息
     * @param paymentType 支付方式
     */
    private void savePaymentInfo(OrderInfo orderInfo, String paymentType) {
        // 订单和支付方式,这两个作为一个标准不能重复
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderInfo.getId());
        wrapper.eq("payment_type", paymentType);
        Integer count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            return;
        }
        // 创建一个paymentInfo对象
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId() + "");
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setPaymentMoney(orderInfo.getTotalMoney());
        paymentInfo.setPaymentContent(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
        paymentInfo.setCreateTime(new Date());
        baseMapper.insert(paymentInfo);
    }
}
