var login = {

    api_name : '/user',

  login(userInfo) {
    return request({
      url: this.api_name + '/login',
      method: 'post',
      data: userInfo
    })
  },

  logout() {
    return request({
      url: this.api_name + '/logout',
      method: 'get'
    })
  }
}
