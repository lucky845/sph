package com.atguigu.constant;

public class MqConst {
    /**
     * 商品上下架
     */
    public static final String ON_OFF_SALE_EXCHANGE = "on.off.exchange";
    public static final String ON_SALE_ROUTING_KEY = "on.sale.routing.key";
    public static final String OFF_SALE_ROUTING_KEY = "off.sale.routing.key";
    public static final String ON_SALE_QUEUE = "on.sale.queue";
    public static final String OFF_SALE_QUEUE = "off.sale.queue";


    /**
     * 取消订单，发送延迟队列
     */
    public static final String CANCEL_ORDER_EXCHANGE = "cancel.order.exchange";
    public static final String CANCEL_ORDER_ROUTE_KEY = "cancel.order.key";
    public static final String CANCEL_ORDER_QUEUE = "cancel.order.queue";

    /**
     * 支付订单
     */
    public static final String PAY_ORDER_EXCHANGE = "pay.order.exchange";
    public static final String PAY_ORDER_ROUTE_KEY = "pay.order.key";
    public static final String PAY_ORDER_QUEUE = "pay.order.queue";

    /**
     * 减库存
     */
    public static final String DECREASE_STOCK_EXCHANGE = "decrease.stock.exchange";
    public static final String DECREASE_STOCK_ROUTE_KEY = "decrease.stock.key";
    public static final String DECREASE_STOCK_QUEUE = "decrease.stock.queue";

    /**
     * 减库存成功，更新订单状态
     */
    public static final String SUCCESS_DECREASE_STOCK_EXCHANGE = "success.decrease.stock.exchange";
    public static final String SUCCESS_DECREASE_STOCK_ROUTE_KEY = "success.decrease.stock.key";
    public static final String SUCCESS_DECREASE_STOCK_QUEUE = "success.decrease.stock.queue";

    /**
     * 关闭支付订单
     */
    public static final String CLOSE_PAYMENT_EXCHANGE = "close.payment.exchange";
    public static final String CLOSE_PAYMENT_ROUTE_KEY = "close.payment.key";
    public static final String CLOSE_PAYMENT_QUEUE = "close.payment.queue";


    /**
     * 定时任务
     */
    public static final String SCAN_SECKILL_EXCHANGE = "scan.seckill.exchange";
    public static final String SCAN_SECKILL_ROUTE_KEY = "scan.seckill.key";
    public static final String SCAN_SECKILL_QUEUE = "scan.seckill.queue";

    public static final String CLEAR_REDIS_EXCHANGE = "clear.redis.exchange";
    public static final String CLEAR_REDIS_ROUTE_KEY = "clear.redis.key";
    public static final String CLEAR_REDIS_QUEUE = "clear.redis.queue";

    /**
     * 秒杀预下单
     */
    public static final String PREPARE_SECKILL_EXCHANGE = "prepare.seckill.exchange";
    public static final String PREPARE_SECKILL_ROUTE_KEY = "prepare.seckill.key";
    public static final String PREPARE_SECKILL_QUEUE = "prepare.seckill.queue";
}
