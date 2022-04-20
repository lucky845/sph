package com.atguigu.constant;

/**
 * Redis常量配置类
 */
public class RedisConst {

    public static final String SKUKEY_PREFIX = "sku:";
    public static final String SKUKEY_SUFFIX = ":info";
    //商品sku_id布隆过滤器
    public static final String BLOOM_SKU_ID = "bloom_sku_id";
    //单位：秒
    public static final long SKUKEY_TIMEOUT = 24 * 60 * 60;
    // 防止缓存穿透制作的过期时间
    public static final long SKUKEY_TEMPORARY_TIMEOUT = 10 * 60;
    //单位：秒 尝试获取锁的最大等待时间
    public static final long WAITTIN_GET_LOCK_TIME = 10;
    //单位：秒 锁的持有时间
    public static final long LOCK_EXPIRE_TIME = 1;
    public static final String SKULOCK_SUFFIX = ":lock";

    public static final String USER_KEY_PREFIX = "user:cart:";
    public static final String USER_CART_KEY_SUFFIX = ":cart";
    public static final long USER_CART_EXPIRE = 60 * 60 * 24 * 7;

    //用户登录
    public static final String USER_LOGIN_KEY_PREFIX = "user:login:";
    //    public static final String userinfoKey_suffix = ":info";
    public static final int USERKEY_TIMEOUT = 60 * 60 * 24 * 7;

    //秒杀商品key
    public static final String SECKILL_PRODUCT = "seckill:product";
    //预售秒杀商品用户id与商品信息对应key
    public static final String PREPARE_SECKILL_USERID_ORDER = "prepare:seckill:userId:order";
    //用户秒杀到的商品(非预售)
    public static final String BOUGHT_SECKILL_USER_ORDER = "bought:seckill:userId:order";
    //秒杀商品库存前缀
    public static final String SECKILL_STOCK_PREFIX = "seckill:stock:";
    //预售秒杀商品用户id与商品id对应key
    public static final String PREPARE_SECKILL_USERID_SKUID = "prepare:seckill:userId:skuId";

    //发布订阅channel名称
    public static final String PREPARE_PUB_SUB_SECKILL = "pub_sub_seckill";
    //可以秒杀
    public static final String CAN_SECKILL = "1";
    //不能秒杀
    public static final String CAN_NOT_SECKILL = "0";
    //秒杀商品状态位前缀
    public static final String SECKILL_STATE_PREFIX = "seckill:state:";


    //用户锁定时间 单位：秒
    public static final int PREPARE_SECKILL_LOCK_TIME = 60 * 60;


}
