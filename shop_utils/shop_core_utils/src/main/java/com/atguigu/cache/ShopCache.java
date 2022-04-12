package com.atguigu.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为当前类或方法添加缓存<br/>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ShopCache {

    /**
     * 缓存前缀,用来区分该缓存是那一个部分的缓存
     */
    String prefix() default "cache";

    /**
     * 是否开启布隆器
     */
    boolean enableBloom() default false;

}
