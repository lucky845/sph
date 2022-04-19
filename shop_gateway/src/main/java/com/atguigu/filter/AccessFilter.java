package com.atguigu.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.result.RetVal;
import com.atguigu.result.RetValCodeEnum;
import com.atguigu.util.IpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 这是一个全局访问过滤器
 *
 * @author lucky845
 * @date 2022年04月18日
 */
//@Component
public class AccessFilter implements GlobalFilter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Value("${filter.whiteList}")
    private String filterWhiteList;
    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * @param exchange 服务网络交换机，存储着请求与响应属性，是一个不可变的2实例，需要调用mutate()来生成一个实例
     * @param chain    网关过滤器，用于过滤链式调用(设计模式：责任链模式)
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        // 3. 获取用户的临时id在购物车使用
        String userTempId = getUserTempId(request);
        // 4. 获取用户的登陆id,购物车使用
        String userId = getUserId(request);
        // 如果用户换了一个网络环境(cookie被盗)，就提示没有权限
        if ("-1".equals(userId)) {
            // 把信息写给浏览器
            return writeDataToBrowser(exchange, RetValCodeEnum.NO_PERMISSION);
        }

        // 1. 对于内部接口，不允许外界访问，提示没有权限
        if (antPathMatcher.match("/sku/**", path)) {
            // 把信息写给浏览器
            return writeDataToBrowser(exchange, RetValCodeEnum.NO_PERMISSION);
        }

        // 2. 请求白名单(指定那些接口)，并且用户未登录的情况下，必须先登陆
        for (String filterWhite : filterWhiteList.split(",")) {
            if (path.contains(filterWhite) && StringUtils.isEmpty(userId)) {
                // 跳转到登陆页面
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.SEE_OTHER);
                response.getHeaders().set(
                        HttpHeaders.LOCATION,
                        "http://passport.gmall.com/login.html?originalUrl=" + request.getURI());
                return response.setComplete();
            }
        }

        // 5. 将用户信息保存到header中传输到shop-web模块的购物车的request中
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)) {
            if (!StringUtils.isEmpty(userId)) {
                // 将userId放到header中
                request.mutate().header("userId", userId).build();
            }
            if (!StringUtils.isEmpty(userTempId)) {
                // 将userTempId放到header中
                request.mutate().header("userTempId", userTempId).build();
            }
            // 过滤器放开拦截继续执行(修改了exchange)
            return chain.filter(exchange.mutate().request(request).build());
        }

        // 过滤器放开拦截继续执行
        return chain.filter(exchange);
    }

    /**
     * 将信息写给浏览器
     */
    private Mono<Void> writeDataToBrowser(ServerWebExchange exchange, RetValCodeEnum retValCodeEnum) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        // 发送的数据
        RetVal<Object> retVal = RetVal.build(null, retValCodeEnum);
        // 把数据转换为json字符串
        byte[] retBytes = JSONObject.toJSONString(retVal).getBytes(StandardCharsets.UTF_8);
        // 创建一个DataBuffer
        DataBuffer dataBuffer = response.bufferFactory().wrap(retBytes);
        // 将信息返回给页面
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * 获取用户id
     */
    private String getUserId(ServerHttpRequest request) {
        // 1. 获取登陆后的token
        List<String> headerValueList = request.getHeaders().get("token");
        String token = "";
        if (!CollectionUtils.isEmpty(headerValueList)) {
            token = headerValueList.get(0);
        } else {
            HttpCookie cookie = request.getCookies().getFirst("token");
            if (cookie != null) {
                token = cookie.getValue();
            }
        }
        // 2. 通过token拼接从Redis中获取userId
        if (!StringUtils.isEmpty(token)) {
            String userKey = "user:login:" + token;
            JSONObject loginJsonObject = (JSONObject) redisTemplate.opsForValue().get(userKey);
            if (loginJsonObject != null) {
                // 为了判断是否在同一网络环境下
                String loginIp = loginJsonObject.getString("loginIp");
                // 获取当前电脑所处环境的ip地址
                String gatewayIpAddress = IpUtil.getGatwayIpAddress(request);
                if (loginIp.equals(gatewayIpAddress)) {
                    return loginJsonObject.getString("userId");
                } else {
                    // 代表这是一个错误的标识
                    return "-1";
                }
            }
        }
        return null;
    }

    /**
     * 获取用户的临时id
     */
    private String getUserTempId(ServerHttpRequest request) {
        List<String> headerValueList = request.getHeaders().get("userTempId");
        String userTempId = "";
        if (!CollectionUtils.isEmpty(headerValueList)) {
            userTempId = headerValueList.get(0);
        } else {
            HttpCookie cookie = request.getCookies().getFirst("userTempId");
            if (cookie != null) {
                userTempId = cookie.getValue();
            }
        }
        return userTempId;
    }
}
