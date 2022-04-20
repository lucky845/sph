package com.atguigu.service.impl;

import com.atguigu.client.CartFeignClient;
import com.atguigu.client.ProductFeignClient;
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
import com.atguigu.util.HttpClientUtil;
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

    @Resource
    private ProductFeignClient productFeignClient;

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
     * @param userId    用户id
     * @param tradeNoUI 订单流水号
     */
    @Override
    public boolean checkTradeNo(String userId, String tradeNoUI) {
        // 从Redis中取出tradeNo进行对比
        String tradeNoKey = RedisConst.TRADENO_PREFIX + userId + RedisConst.TRADENO_SUFFIX;
        String redisTradeNo = (String) redisTemplate.opsForValue().get(tradeNoKey);
        // Redis中已经有了,代表重复提交
        return tradeNoUI.equals(redisTradeNo);
    }

    /**
     * 使用库存系统校验库存与价格
     *
     * @param userId    用户id
     * @param orderInfo 订单信息
     */
    @Override
    public String checkStockAndPrice(String userId, OrderInfo orderInfo) {
        StringBuilder sb = new StringBuilder();
        // 1. 拿到用户的购物清单
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            for (OrderDetail orderDetail : orderDetailList) {
                Long skuId = orderDetail.getSkuId();
                String skuNum = orderDetail.getSkuNum();
                // 2. 调用库存系统,判断库存是否足够
                String url = "http://localhost:8100/hasStock?skuId=" + skuId + "&num=" + skuNum;
                String result = HttpClientUtil.doGet(url);
                // 判断库存是否充足
                if ("0".equals(result)) {
                    sb.append(orderDetail.getSkuName()).append("库存不足,");
                }
                // 判断商品价格是否发生了变化
                BigDecimal realTimePrice = productFeignClient.getSkuPrice(skuId);
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                if (orderPrice.compareTo(realTimePrice) != 0) {
                    sb.append(orderDetail.getSkuName()).append("价格有变化,请刷新页面");
                }
            }
        }
        return sb.toString();
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
