package com.atguigu.service.impl;

import com.atguigu.client.UserFeignClient;
import com.atguigu.constant.RedisConst;
import com.atguigu.entity.*;
import com.atguigu.mapper.SeckillProductMapper;
import com.atguigu.result.RetVal;
import com.atguigu.result.RetValCodeEnum;
import com.atguigu.service.SeckillProductService;
import com.atguigu.util.MD5;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-26
 */
@Service
public class SeckillProductServiceImpl extends ServiceImpl<SeckillProductMapper, SeckillProduct> implements SeckillProductService {

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    @Resource
    private UserFeignClient userFeignClient;

    /**
     * 根据skuId获取秒杀对象数据
     *
     * @param skuId 商品skuId
     */
    @Override
    public SeckillProduct getSeckillProduct(Long skuId) {
        return (SeckillProduct) redisTemplate.boundHashOps(RedisConst.SECKILL_PRODUCT).get(skuId.toString());
    }

    /**
     * 处理秒杀预下单
     *
     * @param userSeckillSkuInfo 用户秒杀商品信息
     */
    @Override
    public void prepareSeckill(UserSeckillSkuInfo userSeckillSkuInfo) {
        String userId = userSeckillSkuInfo.getUserId();
        Long skuId = userSeckillSkuInfo.getSkuId();
        String state = (String) redisTemplate.opsForValue().get(RedisConst.SECKILL_STATE_PREFIX + skuId);
        // 商品已售空
        if (RedisConst.CAN_NOT_SECKILL.equals(state)) {
            return;
        }
        // 判断用户是否已经下过此预售单
        boolean flag = redisTemplate.opsForValue().setIfAbsent(RedisConst.PREPARE_SECKILL_USERID_SKUID + ":" + userId + ":" + skuId, skuId, RedisConst.PREPARE_SECKILL_LOCK_TIME, TimeUnit.SECONDS);
        if (!flag) {
            return;
        }
        // 校验库存是否足够,如果有库存,还需要减库存
        String redisStockSkuId = (String) redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).rightPop();
        if (StringUtils.isEmpty(redisStockSkuId)) {
            // 没有库存,通知其他Redis节点修改秒杀状态位
            redisTemplate.convertAndSend(RedisConst.PREPARE_PUB_SUB_SECKILL, skuId + ":" + RedisConst.CAN_NOT_SECKILL);
            return;
        }
        // 生成一个临时订单存储到redis当中  prepare:seckill:userId:order
        PrepareSeckillOrder prepareSeckillOrder = new PrepareSeckillOrder();
        prepareSeckillOrder.setUserId(userId);
        prepareSeckillOrder.setBuyNum(1);
        SeckillProduct seckillProduct = getSeckillProduct(skuId);
        prepareSeckillOrder.setSeckillProduct(seckillProduct);
        // 设置一个订单码
        prepareSeckillOrder.setPrepareOrderCode(MD5.encrypt(userId + skuId));
        redisTemplate.boundHashOps(RedisConst.PREPARE_SECKILL_USERID_ORDER).put(userId, prepareSeckillOrder);
        // 更新库存量
        updateSecKillStockCount(skuId);
    }

    /**
     * 更新库存量
     *
     * @param skuId 商品skuId
     */
    private void updateSecKillStockCount(Long skuId) {
        // 剩余库存量
        Long leftStock = redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).size();
        // 更新库存(数据库)频次 定义一个规则
        if (leftStock % 2 == 0) {
            SeckillProduct redisSeckillProduct = getSeckillProduct(skuId);
            // 锁定库存量=总商品数量-剩余库存数量
            Integer totalStock = redisSeckillProduct.getNum();
            int lockStock = totalStock - Integer.parseInt(leftStock + "");
            redisSeckillProduct.setStockCount(lockStock);
            // 更新Redis中的商品num库存量,目的是为了给购买的人看进度
            redisTemplate.boundHashOps(RedisConst.SECKILL_PRODUCT).put(skuId.toString(), redisSeckillProduct);
            // 更新数据库为了持久化防止数据丢失
            baseMapper.updateById(redisSeckillProduct);
        }
    }

    /**
     * 判断用户是否具备抢购资格
     *
     * @param skuId  商品skuId
     * @param userId 用户id
     */
    @Override
    public RetVal hasQualified(Long skuId, String userId) {
        // 1. 如果预下单里面有用户信息,就代表该用户具有抢购资格
        boolean isExist = redisTemplate.hasKey(RedisConst.PREPARE_SECKILL_USERID_SKUID + ":" + userId + ":" + skuId);
        // 如果有资格
        if (isExist) {
            // 拿出用户的预购单信息
            PrepareSeckillOrder prepareSeckillOrder = (PrepareSeckillOrder) redisTemplate.boundHashOps(RedisConst.PREPARE_SECKILL_USERID_ORDER).get(userId);
            if (prepareSeckillOrder != null) {
                // 返回预购单信息与秒杀资格抢购成功的信息
                return RetVal.build(prepareSeckillOrder, RetValCodeEnum.PREPARE_SECKILL_SUCCESS);
            }
        }
        // 如果用户已经购买成功该商品
        Integer orderId = (Integer) redisTemplate.boundHashOps(RedisConst.BOUGHT_SECKILL_USER_ORDER).get(userId);
        if (orderId != null) {
            // 返回下单成功的信息
            return RetVal.build(null, RetValCodeEnum.SECKILL_ORDER_SUCCESS);
        }
        // 如果预购单中没有,客户继续排队
        return RetVal.build(null, RetValCodeEnum.SECKILL_RUN);
    }

    /**
     * 返回秒杀确认页面需要的数据
     *
     * @param userId 用户id
     */
    @Override
    public RetVal seckillConfirm(String userId) {
        // 1. 用户地址信息
        List<UserAddress> userAddressList = userFeignClient.getUserAddressByUserId(userId);
        // 2. 用户预购单信息
        PrepareSeckillOrder prepareSeckillOrder = (PrepareSeckillOrder) redisTemplate.boundHashOps(RedisConst.PREPARE_SECKILL_USERID_ORDER).get(userId);
        if (prepareSeckillOrder == null) {
            return RetVal.fail().message("非法请求");
        }
        // 3. 把预购单里面的商品信息转换为orderDetail
        SeckillProduct seckillProduct = prepareSeckillOrder.getSeckillProduct();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillProduct.getSkuId());
        orderDetail.setSkuName(seckillProduct.getSkuName());
        orderDetail.setImgUrl(seckillProduct.getSkuDefaultImg());
        orderDetail.setSkuNum(prepareSeckillOrder.getBuyNum() + "");
        // 把购物车里面的价格赋值给了订单价格 购物车的价格来源于skuInfo表
        orderDetail.setOrderPrice(seckillProduct.getCostPrice());
        // 由于是复用之前的页面 所以这里虽然只有一个商品需要转换为list
        List<OrderDetail> orderDetailList = new ArrayList<>();
        orderDetailList.add(orderDetail);

        Map<String, Object> retMap = new HashMap<>();
        retMap.put("userAddressList", userAddressList);
        retMap.put("orderDetailList", orderDetailList);
        retMap.put("totalMoney", seckillProduct.getCostPrice());
        return RetVal.ok(retMap);
    }
}
