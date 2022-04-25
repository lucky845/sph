package com.atguigu.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.client.CartFeignClient;
import com.atguigu.client.ProductFeignClient;
import com.atguigu.client.UserFeignClient;
import com.atguigu.constant.MqConst;
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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jodd.util.StringUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
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

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Value("${cancel.order.delay}")
    private Integer cancelOrderDelay;

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
        // 发送延时消息,超时自动取消订单
        rabbitTemplate.convertAndSend(
                MqConst.CANCEL_ORDER_EXCHANGE,
                MqConst.CANCEL_ORDER_ROUTE_KEY,
                orderInfoId,
                correlationDate -> {
                    correlationDate.getMessageProperties().setDelay(cancelOrderDelay);
                    return correlationDate;
                });
        return orderInfoId;
    }

    /**
     * 获取订单信息
     *
     * @param orderId 订单id
     */
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        if (orderInfo != null) {
            QueryWrapper<OrderDetail> wrapper = new QueryWrapper<>();
            wrapper.eq("order_id", orderId);
            List<OrderDetail> orderDetailList = orderDetailService.list(wrapper);
            orderInfo.setOrderDetailList(orderDetailList);
        }
        return orderInfo;
    }

    /**
     * 修改订单状态
     *
     * @param orderInfo     订单信息
     * @param processStatus 订单支付状态
     */
    @Override
    public void updateOrderStatus(OrderInfo orderInfo, ProcessStatus processStatus) {
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        orderInfo.setProcessStatus(processStatus.name());
        baseMapper.updateById(orderInfo);
    }

    /**
     * 发送消息通知仓库系统减库存
     *
     * @param orderInfo 订单信息
     */
    @Override
    public void sendMsgToWareHouse(OrderInfo orderInfo) {
        // 1. 修改订单状态为已通知仓库
        updateOrderStatus(orderInfo, ProcessStatus.NOTIFIED_WARE);
        // 2. 把数据封装为json格式
        Map<String, Object> dataMap = assembleWareHouseData(orderInfo);
        String dataMapJson = JSON.toJSONString(dataMap);
        // 3. 发送消息给仓库系统
        rabbitTemplate.convertAndSend(MqConst.DECREASE_STOCK_EXCHANGE, MqConst.DECREASE_STOCK_ROUTE_KEY, dataMapJson);
    }

    /**
     * 将数据封装为map
     *
     * @param orderInfo 订单信息
     */
    private Map<String, Object> assembleWareHouseData(OrderInfo orderInfo) {
        // 构建一个map封装数据
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("orderId", orderInfo.getId());
        dataMap.put("consignee", orderInfo.getConsignee());
        dataMap.put("consigneeTel", orderInfo.getConsigneeTel());
        dataMap.put("orderComment", orderInfo.getOrderComment());
        dataMap.put("orderBody", orderInfo.getTradeBody());
        dataMap.put("deliveryAddress", orderInfo.getDeliveryAddress());
        dataMap.put("paymentWay", 2);
        // 仓库id 减库存拆单时使用
        if (!StringUtil.isEmpty(orderInfo.getWareHouseId())) {
            dataMap.put("wareId", orderInfo.getWareHouseId());
        }
        // 封装订单信息集合
        List<Map<String, Object>> orderDetailMapList = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            HashMap<String, Object> skuInfo = new HashMap<>();
            skuInfo.put("skuId", orderDetail.getSkuId());
            skuInfo.put("skuNum", orderDetail.getSkuNum());
            skuInfo.put("skuName", orderDetail.getSkuName());
            // 完成数据组装
            orderDetailMapList.add(skuInfo);
        }
        dataMap.put("details", orderDetailMapList);
        return dataMap;
    }

    /**
     * 拆单
     *
     * @param orderId                 订单id
     * @param wareHouseIdSkuIdMapJson 库存id和商品skuid的map的json字符串
     */
    @Override
    public String splitOrder(Long orderId, String wareHouseIdSkuIdMapJson) {
        List<Map<String, Object>> assembleWareHouseDataList = new ArrayList<>();
        // 1. 获取原始订单
        OrderInfo parentOrderInfo = getOrderInfo(orderId);
        // 把json字符串转换为list集合
        List<Map> wareHouseIdSkuIdMapList = JSON.parseArray(wareHouseIdSkuIdMapJson, Map.class);
        for (Map wareHouseIdSkuIdMap : wareHouseIdSkuIdMapList) {
            String wareHouseId = (String) wareHouseIdSkuIdMap.get("wareHouseId");
            List<String> skuIdList = (List<String>) wareHouseIdSkuIdMap.get("skuIdList");
            // 2. 设置子订单信息并保存
            OrderInfo childOrderInfo = new OrderInfo();
            BeanUtils.copyProperties(parentOrderInfo, childOrderInfo);
            childOrderInfo.setId(null);
            // 把原始订单id设置为子订单的父订单
            childOrderInfo.setParentOrderId(orderId);
            // 设置子订单的仓库
            childOrderInfo.setWareHouseId(wareHouseId);
            // 3. 设置子订单详情
            List<OrderDetail> parentOrderDetailList = parentOrderInfo.getOrderDetailList();
            List<OrderDetail> childOrderDetailList = new ArrayList<>();
            BigDecimal childTotalMoney = new BigDecimal(0);
            for (OrderDetail parentOrderDetail : parentOrderDetailList) {
                for (String skuId : skuIdList) {
                    // 如果该原始订单详情属于该订单信息
                    if (Long.parseLong(skuId) == parentOrderDetail.getSkuId()) {
                        // 把该原始订单详情信息放入子订单详情信息中
                        childOrderDetailList.add(parentOrderDetail);
                        // 设置子订单的金额
                        BigDecimal orderPrice = parentOrderDetail.getOrderPrice();
                        String skuNum = parentOrderDetail.getSkuNum();
                        childTotalMoney = childTotalMoney.add(orderPrice.multiply(new BigDecimal(skuNum)));
                    }
                }
            }
            childOrderInfo.setOrderDetailList(childOrderDetailList);
            childOrderInfo.setTotalMoney(childTotalMoney);
            // 保存子订单信息
            saveOrderAndDetail(childOrderInfo);
            // 添加子订单到集合中
            Map<String, Object> childOrderInfoMap = assembleWareHouseData(childOrderInfo);
            assembleWareHouseDataList.add(childOrderInfoMap);
        }
        // 4. 修改原始订单状态为split
        updateOrderStatus(parentOrderInfo, ProcessStatus.SPLIT);
        // 5. 返回信息给库存系统
        return JSON.toJSONString(assembleWareHouseDataList);
    }
}
