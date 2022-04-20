package com.atguigu.service;

import com.atguigu.entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 订单表 订单表 服务类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-20
 */
public interface OrderInfoService extends IService<OrderInfo> {

    /**
     * 订单确认接口
     */
    Map<String, Object> confirm(HttpServletRequest request);

    /**
     * 保存用户订单返回订单号
     *
     * @param orderInfo 订单信息
     */
    Long saveOrderAndDetail(OrderInfo orderInfo);

    boolean checkTradeNo(String userId, String tradeNoUI);

    /**
     * 使用库存系统校验库存与价格
     *
     * @param userId    用户id
     * @param orderInfo 订单信息
     */
    String checkStockAndPrice(String userId, OrderInfo orderInfo);
}
