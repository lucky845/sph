package com.atguigu.controller;


import com.atguigu.constant.RedisConst;
import com.atguigu.entity.OrderInfo;
import com.atguigu.result.RetVal;
import com.atguigu.service.OrderInfoService;
import com.atguigu.util.AuthContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

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

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 提供订单确认的接口
     */
    @ApiOperation("提供订单确认的接口")
    @GetMapping("/confirm")
    public RetVal<Map<String, Object>> confirm(HttpServletRequest request) {
        Map<String, Object> retMap = orderInfoService.confirm(request);
        return RetVal.ok(retMap);
    }

    /**
     * 提交订单信息
     *
     * @param orderInfo 订单信息
     */
    @ApiOperation("提交订单信息")
    @PostMapping("/submitOrder")
    public RetVal<Object> submitOrder(
            @ApiParam(name = "orderInfo", value = "订单信息", required = true)
            @RequestBody OrderInfo orderInfo,
            HttpServletRequest request
    ) {
        // 拿到用户id赋值给orderInfo
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.valueOf(userId));
        // 获取订单传递过来的订单流水号
        String tradeNoUI = request.getParameter("tradeNo");
        // 判断订单是否已经提交过
        boolean flag = orderInfoService.checkTradeNo(userId, tradeNoUI);
        if (!flag) {
            return RetVal.fail().message("请勿重复提交订单");
        }
        // 保存订单信息，返回订单id
        Long orderId = orderInfoService.saveOrderAndDetail(orderInfo);
        // 删除Redis中的tradeNo
        String tradeNoKey = RedisConst.TRADENO_PREFIX + userId + RedisConst.TRADENO_SUFFIX;
        redisTemplate.delete(tradeNoKey);
        return RetVal.ok(orderId);
    }

}

