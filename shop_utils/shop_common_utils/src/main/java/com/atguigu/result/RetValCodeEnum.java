package com.atguigu.result;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 */
@Getter
public enum RetValCodeEnum {

    /**
     * 200,成功
     */
    SUCCESS(200, "成功"),

    /**
     * 201，失败
     */
    FAIL(201, "失败"),

    /**
     * 2012，服务异常
     */
    SERVICE_ERROR(2012, "服务异常"),

    /**
     * 205，支付中
     */
    PAY_RUN(205, "支付中"),

    /**
     * 208，未登录
     */
    NO_LOGIN(208, "未登陆"),

    /**
     * 209，没有权限
     */
    NO_PERMISSION(209, "没有权限"),

    /**
     * 210，秒杀还没开始
     */
    SECKILL_NO_START(210, "秒杀还没开始"),

    /**
     * 211，正在排队中
     */
    SECKILL_RUN(211, "正在排队中"),

    /**
     * 您有未支付的订单
     */
    SECKILL_NO_PAY_ORDER(212, "您有未支付的订单"),

    /**
     * 213，已售空
     */
    SECKILL_FINISH(213, "已售空"),

    /**
     * 214，秒杀已结束
     */
    SECKILL_END(214, "秒杀已结束"),

    /**
     * 215，抢单成功
     */
    PREPARE_SECKILL_SUCCESS(215, "抢单成功"),

    /**
     * 216，抢单失败
     */
    SECKILL_FAIL(216, "抢单失败"),

    /**
     * 217，请求不合法
     */
    SECKILL_ILLEGAL(217, "请求不合法"),

    /**
     * 218，下单成功
     */
    SECKILL_ORDER_SUCCESS(218, "下单成功"),
    ;

    private final Integer code;

    private final String message;

    RetValCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
