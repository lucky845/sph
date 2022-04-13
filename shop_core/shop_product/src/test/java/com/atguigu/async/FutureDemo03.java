package com.atguigu.async;

import com.atguigu.exception.SleepUtils;

import java.util.concurrent.CompletableFuture;

/**
 * @author lucky845
 * @date 2022年04月12日
 */
public class FutureDemo03 {

    public static void main(String[] args) throws Exception {
        runAsync1();
        runAsync2();
        SleepUtils.sleep(1);
        System.out.println("main线程执行");
    }

    /**
     * 异步请求<br />
     * 1. 没有返回值<br />
     * 2. whenCompleteAsync：类似vue里面发起异步请求之后的then方法，不管是否有异常都会执行<br />
     */
    private static void runAsync1() throws Exception {
        CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + " 你好runAsync1");
        }).whenComplete((acceptVal, throwable) -> {
            System.out.println(Thread.currentThread().getName() + " runAsync1之后执行whenComplete获取的值: " + acceptVal);
        });
    }

    /**
     * 异步请求<br />
     * 1. 没有返回值<br />
     * 2. whenCompleteAsync：异步执行，类似vue里面发起异步请求之后的then方法，不管是否有异常都会执行<br />
     */
    private static void runAsync2() throws Exception {
        CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + " 你好runAsync2");
        }).whenCompleteAsync((acceptVal, throwable) -> {
            System.out.println(Thread.currentThread().getName() + " runAsync2之后执行whenComplete获取的值: " + acceptVal);
        });
    }

}
