package com.atguigu.handler;

import com.atguigu.exception.GmallException;
import com.atguigu.result.RetVal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理类
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 统一异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public RetVal<?> error(Exception e) {
        e.printStackTrace();
        return RetVal.fail();
    }

    /**
     * 自定义异常处理方法
     *
     * @param e
     * @return
     */
    @ExceptionHandler(GmallException.class)
    @ResponseBody
    public RetVal<Object> error(GmallException e) {
        return RetVal.fail(e.getMessage());
    }
}
