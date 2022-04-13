package com.atguigu.async;

import java.util.concurrent.CompletableFuture;

/**
 * @author lucky845
 * @date 2022年04月12日
 */
public class FutureDemo02 {

    public static void main(String[] args) throws Exception {
        runAsync();
        System.out.println("main线程执行");
    }

    /**
     * 异步请求<br />
     * 1. 没有返回值<br />
     * 2. whenComplete：类似vue里面发起异步请求之后的then方法，不管是否有异常都会执行<br />
     * 3. exceptionally：类似于vue里面发起异步请求之后的catch方法，有异常才执行<br />
     */
    private static void runAsync() throws Exception {
        CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + " 你好runAsync");
            int i = 10 / 0;
        }).whenComplete((acceptVal, throwable) -> {
            System.out.println(Thread.currentThread().getName() + " runAsync之后执行whenComplete获取的值: " + acceptVal);
            System.out.println(Thread.currentThread().getName() + " runAsync之后执行whenComplete获取的异常: " + throwable);
        }).exceptionally(throwable -> {
            System.out.println(Thread.currentThread().getName() + " runAsync之后执行exceptionally获取的异常: " + throwable);
            return null;
        });
    }

}
