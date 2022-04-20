package com.atguigu.controller;


import com.atguigu.result.RetVal;
import com.atguigu.service.OrderInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 订单表 订单表 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-20
 */
@Api(tags = "订单信息")
@RestController
@RequestMapping("/order")
public class OrderInfoController {

    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 提供订单确认的接口
     */
    @ApiOperation("提供订单确认的接口")
    @GetMapping("/confirm")
    public RetVal<Map<String, Object>> confirm(HttpServletRequest request) {
        Map<String, Object> retMap = orderInfoService.confirm(request);
        return RetVal.ok(retMap);
    }


}

