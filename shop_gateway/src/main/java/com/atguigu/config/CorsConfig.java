package com.atguigu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author lucky845
 * @date 2022年04月05日 23:04
 */
@Configuration
public class CorsConfig {
    // 表示将某一个对象注入到spring容器中
    @Bean
    public CorsWebFilter corsWebFilter() {
        // 创建这个对象CorsConfiguration
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); // 允许所有的网络请求。cookie.setDomain("http://localhost")
        corsConfiguration.setAllowCredentials(true); // 配置这个允许携带cookie。
        corsConfiguration.addAllowedMethod("*"); // 允许所有的请求方法
        corsConfiguration.addAllowedHeader("*"); // 允许请求头中携带信息
        // 创建对应的 CorsConfigurationSource
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        // 设置配置选项
        // 第一个参数path 过来拦截哪个路径
        // 第二个参数CorsConfiguration 制作一个跨域的规则配置
        configurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(configurationSource);
    }
}
