package com.atguigu.controller;


import com.atguigu.client.OrderFeignClient;
import com.atguigu.constant.MqConst;
import com.atguigu.constant.RedisConst;
import com.atguigu.entity.OrderInfo;
import com.atguigu.entity.PrepareSeckillOrder;
import com.atguigu.entity.SeckillProduct;
import com.atguigu.entity.UserSeckillSkuInfo;
import com.atguigu.result.RetVal;
import com.atguigu.result.RetValCodeEnum;
import com.atguigu.service.SeckillProductService;
import com.atguigu.util.AuthContextHolder;
import com.atguigu.util.MD5;
import com.atguigu.utils.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-26
 */
@Api(tags = "秒杀")
@RestController
@RequestMapping("/seckill")
public class SeckillProductController {

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private SeckillProductService seckillProductService;

    @Resource
    private OrderFeignClient orderFeignClient;

    /**
     * 查询所有的秒杀商品
     */
    @ApiOperation("查询所有的秒杀商品")
    @GetMapping("/queryAllSeckillProduct")
    public RetVal<List<Object>> queryAllSeckillProduct() {
        List<Object> seckillProductList = redisTemplate.boundHashOps(RedisConst.SECKILL_PRODUCT).values();
        return RetVal.ok(seckillProductList);
    }

    /**
     * 根据skuId获取秒杀对象数据
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("根据skuId获取秒杀对象数据")
    @GetMapping("/getSeckillProductBySkuId/{skuId}")
    public RetVal<SeckillProduct> getSeckillProductBySkuId(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId
    ) {
        SeckillProduct seckillProduct = seckillProductService.getSeckillProduct(skuId);
        return RetVal.ok(seckillProduct);
    }

    /**
     * 生成一个抢购码 防止用户直接跳过商品详情页面进入秒杀地址
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("生成一个抢购码")
    @GetMapping("/generateSeckillCode/{skuId}")
    public RetVal<Object> generateSeckillCode(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId,
            HttpServletRequest request
    ) {
        // 1. 判断用户是否登陆
        String userId = AuthContextHolder.getUserId(request);
        if (!StringUtils.isEmpty(userId)) {
            // 2. 从缓存中获取秒杀商品信息
            SeckillProduct seckillProduct = seckillProductService.getSeckillProduct(skuId);
            // 3. 判断当前时间是否在秒杀时间范围之内
            Date nowTime = new Date();
            if (DateUtil.dateCompare(seckillProduct.getStartTime(), nowTime) && DateUtil.dateCompare(nowTime, seckillProduct.getEndTime())) {
                // 4. 利用MD5对userId进行加密
                String seckillCode = MD5.encrypt(userId);
                return RetVal.ok(seckillCode);
            }
        }
        return RetVal.fail().message("获取抢购码失败,请登陆");
    }

    /**
     * 秒杀预下单
     *
     * @param skuId       商品skuId
     * @param seckillCode 秒杀抢购码
     */
    @ApiOperation("秒杀预下单")
    @PostMapping("/prepareSeckill/{skuId}")
    public RetVal prepareSeckill(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId,

            @ApiParam(name = "seckillCode", value = "秒杀抢购码", required = true)
                    String seckillCode,
            HttpServletRequest request
    ) {
        // 1. 判断抢购码是否正确
        String userId = AuthContextHolder.getUserId(request);
        if (!MD5.encrypt(userId).equals(seckillCode)) {
            // 抢购码不合法,直接报错
            return RetVal.build(null, RetValCodeEnum.SECKILL_ILLEGAL);
        }
        // 2. 秒杀商品是否可以进行秒杀,状态为1,说明可以秒杀
        String state = (String) redisTemplate.opsForValue().get(RedisConst.SECKILL_STATE_PREFIX + skuId);
        if (StringUtils.isEmpty(state)) {
            // 商品秒杀状态位不存在,报异常
            return RetVal.build(null, RetValCodeEnum.SECKILL_ILLEGAL);
        }
        // 如果该商品可以进行秒杀
        if (RedisConst.CAN_SECKILL.equals(state)) {
            // 3. 如果可以秒杀就生成一个预下单
            UserSeckillSkuInfo userSeckillSkuInfo = new UserSeckillSkuInfo();
            userSeckillSkuInfo.setUserId(userId);
            userSeckillSkuInfo.setSkuId(skuId);
            rabbitTemplate.convertAndSend(MqConst.PREPARE_SECKILL_EXCHANGE, MqConst.PREPARE_SECKILL_ROUTE_KEY, userSeckillSkuInfo);
        } else {
            // 秒杀商品已售空
            return RetVal.build(null, RetValCodeEnum.SECKILL_FINISH);
        }
        return RetVal.ok();
    }

    /**
     * 判断用户是否具备抢购资格
     *
     * @param skuId 商品skuId
     */
    @ApiOperation("判断用户是否具备抢购资格")
    @GetMapping("/hasQualified/{skuId}")
    public RetVal hasQualified(
            @ApiParam(name = "skuId", value = "商品skuId", required = true)
            @PathVariable Long skuId,
            HttpServletRequest request
    ) {
        String userId = AuthContextHolder.getUserId(request);
        return seckillProductService.hasQualified(skuId, userId);
    }

    /**
     * 返回秒杀确认页面需要的数据
     */
    @ApiOperation("返回秒杀确认页面需要的数据")
    @GetMapping("/seckillConfirm")
    public RetVal seckillConfirm(HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        return seckillProductService.seckillConfirm(userId);
    }

    /**
     * 提交秒杀订单信息
     *
     * @param orderInfo 订单信息
     */
    @ApiOperation("提交秒杀订单信息")
    @PostMapping("/submitSecKillOrder")
    public RetVal submitSecKillOrder(
            @ApiParam(name = "orderInfo", value = "订单信息", required = true)
            @RequestBody OrderInfo orderInfo,

            HttpServletRequest request
    ) {
        // 1. 判断用户是否有预下单
        String userId = AuthContextHolder.getUserId(request);
        PrepareSeckillOrder prepareSeckillOrder = (PrepareSeckillOrder) redisTemplate.boundHashOps(RedisConst.PREPARE_SECKILL_USERID_ORDER).get(userId);
        if (prepareSeckillOrder == null) {
            return RetVal.fail().message("非法请求");
        }
        // 2. 通过远程调用shop-order微服务进行下单
        orderInfo.setUserId(Long.parseLong(userId));
        Long orderId = orderFeignClient.saveOrderAndDetail(orderInfo);
        if (orderId == null) {
            return RetVal.fail().message("下单失败");
        }
        // 3. 删除Redis里面的预购单信息
        redisTemplate.boundHashOps(RedisConst.PREPARE_SECKILL_USERID_ORDER).delete(userId);
        // 4. 在Redis中把用户购买的商品信息放到里面,用于判断用户是否购买过
        redisTemplate.boundHashOps(RedisConst.BOUGHT_SECKILL_USER_ORDER).put(userId, orderId);
        return RetVal.ok(orderId);
    }

}

