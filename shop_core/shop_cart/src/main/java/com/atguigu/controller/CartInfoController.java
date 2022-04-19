package com.atguigu.controller;


import com.atguigu.entity.CartInfo;
import com.atguigu.result.RetVal;
import com.atguigu.service.CartInfoService;
import com.atguigu.util.AuthContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jodd.util.StringUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 购物车表 用户登录系统时更新冗余 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-19
 */
@Api(tags = "购物车")
@RestController
@RequestMapping("/cart")
public class CartInfoController {

    @Resource
    private CartInfoService cartInfoService;

    /**
     * 加入购物车
     *
     * @param skuId  商品skuId
     * @param skuNum 商品数量
     */
    @ApiOperation("加入购物车")
    @PostMapping("/addToCart/{skuId}/{skuNum}")
    public RetVal<Object> addToCart(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId,

            @ApiParam(name = "skuNum", value = "商品数量", required = true)
            @PathVariable Integer skuNum,

            HttpServletRequest request
    ) {
        String oneOfUserId = "";
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtil.isEmpty(userId)) {
            // 用户未登录使用临时用户id
            oneOfUserId = AuthContextHolder.getUserTempId(request);
        } else {
            oneOfUserId = userId;
        }
        cartInfoService.addToCart(oneOfUserId, skuId, skuNum);
        return RetVal.ok();
    }

    /**
     * 购物车列表查询
     */
    @ApiOperation("购物车列表")
    @GetMapping("/getCartList")
    public RetVal<List<CartInfo>> getCartList(HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);

        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId, userTempId);
        return RetVal.ok(cartInfoList);
    }


}

