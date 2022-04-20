package com.atguigu.service.impl;

import com.atguigu.client.CartFeignClient;
import com.atguigu.client.UserFeignClient;
import com.atguigu.entity.CartInfo;
import com.atguigu.entity.OrderDetail;
import com.atguigu.entity.OrderInfo;
import com.atguigu.entity.UserAddress;
import com.atguigu.mapper.OrderInfoMapper;
import com.atguigu.service.OrderInfoService;
import com.atguigu.util.AuthContextHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 订单表 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-20
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private CartFeignClient cartFeignClient;

    /**
     * 订单确认接口
     */
    @Override
    public Map<String, Object> confirm(HttpServletRequest request) {
        // 1. 获取用户的地址信息
        String userId = AuthContextHolder.getUserId(request);
        List<UserAddress> userAddressList = userFeignClient.getUserAddressByUserId(userId);
        // 2. 获取用户的购物清单
        List<CartInfo> detailArrayList = cartFeignClient.getSelectedProduct(userId);
        // 3. 商品的其他参数
        int totalNum = 0;
        BigDecimal totalMoney = new BigDecimal(0);
        List<OrderDetail> orderDetailList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(detailArrayList)) {
            for (CartInfo cartInfo : detailArrayList) {
                // 把购物车信息转换为商品详情
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuNum(cartInfo.getSkuNum() + "");
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                // 订单的总金额
                totalMoney = totalMoney.add(cartInfo.getCartPrice()
                        .multiply(new BigDecimal(cartInfo.getSkuNum())));
                // 订单的总件数
                totalNum += cartInfo.getSkuNum();
                orderDetailList.add(orderDetail);
            }
        }
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("userAddressList", userAddressList);
        retMap.put("detailArrayList", orderDetailList);
        retMap.put("totalMoney", totalMoney);
        retMap.put("totalNum", totalNum);
        return retMap;
    }

}
