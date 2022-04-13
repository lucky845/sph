package com.atguigu.async;

import com.atguigu.exception.SleepUtils;

import java.util.concurrent.CompletableFuture;

/**
 * @author lucky845
 * @date 2022年04月12日
 */
public class FutureDemo04 {

    public static void main(String[] args) throws Exception {
        runSupplyAsync();
        SleepUtils.sleep(2);
        System.out.println("main线程执行");
    }

    /**
     * 异步请求<br />
     * 1. 有返回值<br />
     * 2. thenAccept：runSupplyAsync之后执行，没有返回值<br />
     */
    private static void runSupplyAsync() throws Exception {
        CompletableFuture<String> supply = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "你好supplyAsync");
            SleepUtils.sleep(2);
            return "hello";
        });

        supply.thenAccept((acceptVal) -> {
            System.out.println(Thread.currentThread().getName() + " 第一个thenAccept获取的值: " + acceptVal);
        });

        supply.thenAccept((acceptVal) -> {
            System.out.println(Thread.currentThread().getName() + " 第二个thenAccept获取的值: " + acceptVal);
        });
    }

}
