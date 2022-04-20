package com.atguigu.gmall.ware.constant;

public class MqConst {
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

    public static final String MQ_KEY_PREFIX = "mq:list";
    public static final int RETRY_COUNT = 3;

}
