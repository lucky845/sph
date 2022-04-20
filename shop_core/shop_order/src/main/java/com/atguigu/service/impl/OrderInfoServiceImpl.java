package com.atguigu.service.impl;

import com.atguigu.client.CartFeignClient;
import com.atguigu.client.UserFeignClient;
import com.atguigu.constant.RedisConst;
import com.atguigu.entity.CartInfo;
import com.atguigu.entity.OrderDetail;
import com.atguigu.entity.OrderInfo;
import com.atguigu.entity.UserAddress;
import com.atguigu.enums.ProcessStatus;
import com.atguigu.mapper.OrderInfoMapper;
import com.atguigu.service.OrderDetailService;
import com.atguigu.service.OrderInfoService;
import com.atguigu.util.AuthContextHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

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

    @Resource
    private OrderDetailService orderDetailService;

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

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
        // 生成一个流水号给界面(防止订单重复提交)
        String tradeNo = generateTradeNo(userId);
        retMap.put("tradeNo", tradeNo);
        return retMap;
    }

    /**
     * 生成一个订单流水号
     *
     * @param userId 用户id
     */
    private String generateTradeNo(String userId) {
        String tradeNo = UUID.randomUUID().toString();
        // 在Redis中放一份
        String tradeNoKey = RedisConst.TRADENO_PREFIX + userId + RedisConst.TRADENO_SUFFIX;
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);
        return tradeNo;
    }

    /**
     * @param userId
     * @param tradeNoUI
     */
    @Override
    public boolean checkTradeNo(String userId, String tradeNoUI) {
        // 从Redis中取出tradeNo进行对比
        String tradeNoKey = RedisConst.TRADENO_PREFIX + userId + RedisConst.TRADENO_SUFFIX;
        String redisTradeNo = (String) redisTemplate.opsForValue().get(tradeNoKey);
        if (tradeNoUI.equals(redisTradeNo)) {
            // Redis中已经有了,代表重复提交
            return true;
        }
        // Redis中没有,不是重复提交,删除Redis中的tradeNo
        redisTemplate.delete(tradeNoKey);
        return false;
    }

    /**
     * 提交订单信息
     *
     * @param orderInfo 订单信息
     */
    @Override
    public Long saveOrderAndDetail(OrderInfo orderInfo) {
        // 1. 保存订单基本信息
        // 商品对外交易号，给微信或者支付宝使用
        String outTradeNo = "lucky845" + System.currentTimeMillis();
        orderInfo.setOutTradeNo(outTradeNo);
        // 订单的描述信息
        orderInfo.setTradeBody("换个新手机爽一爽");
        // 订单创建时间
        orderInfo.setCreateTime(new Date());
        // 订单支付过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);
        orderInfo.setExpireTime(calendar.getTime());
        // 订单的进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        baseMapper.insert(orderInfo);

        // 2. 保存订单详细信息
        Long orderInfoId = orderInfo.getId();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            for (OrderDetail orderDetail : orderDetailList) {
                orderDetail.setOrderId(orderInfoId);
            }
            orderDetailService.saveBatch(orderDetailList);
        }
        return orderInfoId;
    }

}
