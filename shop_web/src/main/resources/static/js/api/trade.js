var trade = {

    api_name : '/order',

  // 添加购物车
  trade() {
    return request({
      url: this.api_name + '/confirm',
      method: 'get'
    })
  },

  // 提交订单
  submitOrder(order, tradeNo) {
    return request({
      url: this.api_name + '/submitOrder?tradeNo=' + tradeNo,
      method: 'post',
      data: order
    })
  },

  // 获取订单
  getPayOrderInfo(orderId) {
    return request({
      url: this.api_name + '/getPayOrderInfo/' + orderId,
      method: 'get'
    })
  },

  // 获取订单
  getOrderPageList(page, limit) {
    return request({
      url: this.api_name + `/${page}/${limit}`,
      method: 'get'
    })
  }
}
