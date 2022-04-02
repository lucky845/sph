package com.atguigu.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 全局统一返回结果类
 */
@Data
@ApiModel(value = "全局统一返回结果")
public class RetVal<T> {

    // 200 , 成功!
    @ApiModelProperty(value = "返回码")
    private Integer code;

    @ApiModelProperty(value = "返回消息")
    private String message;

    @ApiModelProperty(value = "返回数据")
    private T data;

    public RetVal() {
    }

    /**
     * 返回数据
     *
     * @param data 需要返回的参数
     */
    protected static <T> RetVal<T> build(T data) {
        RetVal<T> retVal = new RetVal<T>();
        if (data != null)
            retVal.setData(data);
        return retVal;
    }

    public static <T> RetVal<T> build(T body, RetValCodeEnum retValCodeEnum) {
        RetVal<T> retVal = build(body);
        retVal.setCode(retValCodeEnum.getCode());
        retVal.setMessage(retValCodeEnum.getMessage());
        return retVal;
    }

    public static <T> RetVal<T> ok() {
        return RetVal.ok(null);
    }

    /**
     * 操作成功
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> RetVal<T> ok(T data) {
        RetVal<T> retVal = build(data);
        return build(data, RetValCodeEnum.SUCCESS);
    }

    public static <T> RetVal<T> fail() {
        return RetVal.fail(null);
    }

    /**
     * 操作失败
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> RetVal<T> fail(T data) {
        RetVal<T> retVal = build(data);
        return build(data, RetValCodeEnum.FAIL);
    }

    public RetVal<T> message(String msg) {
        this.setMessage(msg);
        return this;
    }

    public RetVal<T> code(Integer code) {
        this.setCode(code);
        return this;
    }

    public boolean isOk() {
        return this.getCode().intValue() == RetValCodeEnum.SUCCESS.getCode().intValue();
    }
}
