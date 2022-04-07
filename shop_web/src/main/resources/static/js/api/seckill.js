var seckill = {

    api_name: '/seckill',

    // 添加购物车
    findAll() {
        return request({
            url: this.api_name + '/findAll',
            method: 'get'
        })
    },

    // 获取秒杀商品
    getSeckillGoods(skuId) {
        return request({
            url: this.api_name + '/getSeckillGoods/' + skuId,
            method: 'get'
        })
    },

    // 获取秒杀参数
    generateSeckillCode(skuId) {
        return request({
            url: this.api_name + '/generateSeckillCode/' + skuId,
            method: 'get'
        })
    },

    //预下单
    prepareSeckill(skuId, seckillCode) {
        return request({
            url: this.api_name + '/prepareSeckill/' + skuId + '?seckillCode=' + seckillCode,
            method: 'post'
        })
    },

    // 查询订单
    hasQualified(skuId) {
        return request({
            url: this.api_name + '/hasQualified/' + skuId,
            method: 'get'
        })
    },

    // 提交订单
    submitSecKillOrder(order) {
        return request({
            url: this.api_name + '/submitSecKillOrder',
            method: 'post',
            data: order
        })
    },
}
