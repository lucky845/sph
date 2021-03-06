package com.atguigu.consumer;

import com.atguigu.constant.MqConst;
import com.atguigu.constant.RedisConst;
import com.atguigu.entity.SeckillProduct;
import com.atguigu.entity.UserSeckillSkuInfo;
import com.atguigu.service.SeckillProductService;
import com.atguigu.utils.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author lucky845
 * @since 2022年04月26日
 */
@Component
public class SeckillConsumer {

    @Resource
    private SeckillProductService seckillProductService;

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 接收秒杀商品上架的消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.SCAN_SECKILL_QUEUE, durable = "false"),
            exchange = @Exchange(value = MqConst.SCAN_SECKILL_EXCHANGE, durable = "false", autoDelete = "true"),
            key = {MqConst.SCAN_SECKILL_ROUTE_KEY})
    )
    public void scanSeckillProductToRedis() {
        // 1. 扫描数据库符合秒杀的商品
        QueryWrapper<SeckillProduct> wrapper = new QueryWrapper<>();
        // 1为秒杀商品 2为结束 3为审核未通过
        wrapper.eq("status", 1);
        // 剩余库存>0
        wrapper.gt("stock_count", 0);
        // 取出开始时间应该小于当前日期
        wrapper.ge("DATE_FORMAT(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));
        List<SeckillProduct> seckillProductList = seckillProductService.list(wrapper);
        if (!CollectionUtils.isEmpty(seckillProductList)) {
            for (SeckillProduct seckillProduct : seckillProductList) {
                String skuIdString = seckillProduct.getSkuId().toString();
                // 2. 没有该秒杀商品就把他放入缓存
                redisTemplate.boundHashOps(RedisConst.SECKILL_PRODUCT).put(skuIdString, seckillProduct);
                // 3. 利用list的数据结构,构建库存存储数量,卖出一个就吐出一个数据,防止超卖问题
                for (Integer i = 0; i < seckillProduct.getNum(); i++) {
                    redisTemplate
                            .boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuIdString)
                            .leftPush(skuIdString);
                }
                // 4. 通知Redis集群其他节点,商品可以秒杀了,秒杀标志位
                redisTemplate.convertAndSend(RedisConst.PREPARE_PUB_SUB_SECKILL, skuIdString + ":" + RedisConst.CAN_SECKILL);
            }
        }
    }

    /**
     * 秒杀商品预下单
     *
     * @param userSeckillSkuInfo 用户秒杀商品信息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.PREPARE_SECKILL_QUEUE, durable = "false"),
            exchange = @Exchange(value = MqConst.PREPARE_SECKILL_EXCHANGE, durable = "false", autoDelete = "true"),
            key = {MqConst.PREPARE_SECKILL_ROUTE_KEY})
    )
    public void prepareSeckill(UserSeckillSkuInfo userSeckillSkuInfo) {
        if (userSeckillSkuInfo != null) {
            // 开始处理预下单逻辑
            seckillProductService.prepareSeckill(userSeckillSkuInfo);
        }
    }

    /**
     * 秒杀结束收尾工作
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.CLEAR_REDIS_QUEUE, durable = "false"),
            exchange = @Exchange(value = MqConst.CLEAR_REDIS_EXCHANGE, durable = "false", autoDelete = "true"),
            key = {MqConst.CLEAR_REDIS_ROUTE_KEY})
    )
    public void clearSeckill() {
        // 1. 把小于当前时间的商品下架
        QueryWrapper<SeckillProduct> wrapper = new QueryWrapper<>();
        // 1为秒杀商品 2为结束 3为审核未通过
        wrapper.eq("status", 1);
        // 把小于等于当前时间的商品下架
        wrapper.le("end_time", new Date());
        List<SeckillProduct> seckillProductList = seckillProductService.list(wrapper);
        if (!CollectionUtils.isEmpty(seckillProductList)) {
            for (SeckillProduct seckillProduct : seckillProductList) {
                // 需要对商品的状态进行改变,2为结束
                seckillProduct.setStatus("2");
                seckillProductService.updateById(seckillProduct);
                // 2. 把即将下架的秒杀商品信息从Redis中删除
                Long skuId = seckillProduct.getSkuId();
                redisTemplate.delete(RedisConst.SECKILL_STATE_PREFIX + skuId);
                redisTemplate.delete(RedisConst.SECKILL_STOCK_PREFIX + skuId);
            }
        }
        // 3. 再删除其他和秒杀业务有关的Redis信息
        redisTemplate.delete(RedisConst.SECKILL_PRODUCT);
        redisTemplate.delete(RedisConst.BOUGHT_SECKILL_USER_ORDER);
        redisTemplate.delete(RedisConst.PREPARE_SECKILL_USERID_SKUID);
        redisTemplate.delete(RedisConst.PREPARE_SECKILL_USERID_ORDER);
    }

}
