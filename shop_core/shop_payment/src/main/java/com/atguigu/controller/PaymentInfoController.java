package com.atguigu.controller;


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

        return "";
        //return "success";
    }

}

