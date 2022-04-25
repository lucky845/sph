package com.atguigu.controller;


import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.config.AlipayConfig;
import com.atguigu.entity.PaymentInfo;
import com.atguigu.enums.PaymentStatus;
import com.atguigu.service.PaymentInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * <p>
 * 支付信息表 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-23
 */
@Api("支付宝")
@RestController
@RequestMapping("/payment")
public class PaymentInfoController {

    @Resource
    private PaymentInfoService paymentInfoService;

    /**
     * 返回支付二维码页面信息
     *
     * @param orderId 订单id
     */
    @ApiOperation("返回支付二维码页面信息")
    @RequestMapping("/createQrCode/{orderId}")
    public String createQrCode(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @PathVariable Long orderId
    ) throws Exception {
        return paymentInfoService.createQrCode(orderId);
    }

    /**
     * 支付宝异步调用
     *
     * @param paramMap 支付宝返回的参数
     */
    @ApiOperation("支付宝异步调用接口")
    @PostMapping("/async/notify")
    public String asyncNotify(@RequestParam Map<String, String> paramMap) throws Exception {
        // 支付宝回调验签
        boolean signature = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        if (signature) {
            // 编写商户自身业务
            String tradeStatus = paramMap.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 交易成功,查询支付表信息
                String outTradeNo = paramMap.get("out_trade_no");
                PaymentInfo paymentInfo = paymentInfoService.getPaymentInfo(outTradeNo);
                String paymentStatus = paymentInfo.getPaymentStatus();
                if (paymentStatus.equals(PaymentStatus.PAID.name()) || paymentStatus.equals(PaymentStatus.ClOSED.name())) {
                    // 支付完成
                    return "success";
                }
                // 修改支付表信息
                paymentInfoService.updatePaymentInfo(paramMap);
            }
        } else {
            // 验签失败,返回failure
            return "failure";
        }
        return "failure";
    }

}

