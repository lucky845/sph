package com.atguigu.async;

import com.atguigu.exception.SleepUtils;

import java.util.concurrent.CompletableFuture;

/**
 * @author lucky845
 * @date 2022年04月12日
 */
public class FutureDemo01 {

    public static void main(String[] args) throws Exception {
        runAsync();
        runSupplyAsync();
        System.out.println("main线程执行");
    }

    /**
     * 异步请求
     * 1. 没有返回值
     */
    private static void runAsync() throws Exception {
        CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "你好runAsync");
            SleepUtils.sleep(2);
        });
    }

    /**
     * 异步请求
     * 1. 有返回值
     */
    private static void runSupplyAsync() throws Exception {
        CompletableFuture<String> supply = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "你好supplyAsync");
            SleepUtils.sleep(2);
            return "hello";
        });
        // get() 阻塞方法
        System.out.println(Thread.currentThread().getName() + " : " + supply.get());
    }

}
