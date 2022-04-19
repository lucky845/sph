package com.atguigu.controller;

import com.atguigu.client.CartFeignClient;
import com.atguigu.client.ProductFeignClient;
import com.atguigu.entity.SkuInfo;
import com.atguigu.util.AuthContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author lucky845
 * @date 2022年04月19日
 */
@Controller
public class WebCartController {

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private CartFeignClient cartFeignClient;

    /**
     * 添加到购物车
     *
     * @param skuId  商品skuId
     * @param skuNum 商品数量
     */
    @GetMapping("/addCart.html")
    public String addCart(@RequestParam Long skuId, @RequestParam Integer skuNum, HttpServletRequest request) {
        // 获取用户id与临时用户id
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);

        // 远程调用加入购物车
        cartFeignClient.addToCart(skuId, skuNum);

        // 远程调用shop-product模块查询商品信息，用于页面回显
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);
        return "cart/addCart";
    }

    /**
     * 跳转到购物车页面
     */
    @GetMapping("/cart.html")
    public String cart() {
        return "cart/index";
    }

}
