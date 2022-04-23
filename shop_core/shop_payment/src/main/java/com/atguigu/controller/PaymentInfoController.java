package com.atguigu.controller;


import com.atguigu.service.PaymentInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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


}

