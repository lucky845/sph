package com.atguigu.utils;

/**
 * @author lucky845
 * @date 2022年04月07日 23:46
 */
public class SleepUtils {

    public static void sleep(int second) {
        try {
            Thread.sleep(second * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sleepMillis(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
