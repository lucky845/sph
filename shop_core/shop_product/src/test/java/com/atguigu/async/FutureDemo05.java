package com.atguigu.async;

import com.atguigu.exception.SleepUtils;

import java.util.concurrent.CompletableFuture;

/**
 * @author lucky845
 * @date 2022年04月12日
 */
public class FutureDemo05 {

    public static void main(String[] args) throws Exception {
        runSupplyAsync();
        SleepUtils.sleep(6);
        System.out.println("main线程执行");
    }

    /**
     * 异步请求<br />
     * 1. 有返回值<br />
     * 2. thenAccept：runSupplyAsync之后执行，依赖异步执行的返回结果，没有返回值<br />
     * 3. thenApply(串行)：runSupplyAsync之后执行,依赖异步执行的返回结果，有返回值,thenApply的get()方法会阻塞线程<br />
     */
    private static void runSupplyAsync() throws Exception {
        CompletableFuture<String> supply = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "你好supplyAsync");
            SleepUtils.sleep(2);
            return "hello";
        });

        CompletableFuture<String> thenApply1 = supply.thenApply((acceptVal) -> {
            System.out.println(Thread.currentThread().getName() + " 第一个thenApply获取的值: " + acceptVal);
            SleepUtils.sleep(2);
            return acceptVal;
        });

        CompletableFuture<String> thenApply2 = supply.thenApply((acceptVal) -> {
            System.out.println(Thread.currentThread().getName() + " 第二个thenApply获取的值: " + acceptVal);
            SleepUtils.sleep(2);
            return acceptVal;
        });

        System.out.println(thenApply1.get());
        System.out.println(thenApply2.get());
    }

}
