import request from './request'

// 认证
export const login = (data) => request({ url: '/auth/login', method: 'post', data })
export const logout = () => request({ url: '/auth/logout', method: 'post' })
export const getAuthInfo = () => request({ url: '/auth/info', method: 'get' })

// 用户
export const getMyInfo = () => request({ url: '/user/info', method: 'get' })

// 商品
export const getGoodsPage = (params) => request({ url: '/goods/page', method: 'get', params })
export const getGoodsById = (id) => request({ url: `/goods/${id}`, method: 'get' })
export const getCategories = () => request({ url: '/goods/category/list', method: 'get' })

// 搜索
export const searchGoods = (params) => request({ url: '/search/goods', method: 'get', params })
export const suggestGoods = (prefix) =>
  request({ url: '/search/suggest', method: 'get', params: { prefix, size: 8 } })

// 秒杀
export const getSeckillActivities = () =>
  request({ url: '/seckill/public/activities', method: 'get' })
export const getSeckillGoods = (activityId) =>
  request({ url: `/seckill/public/goods/${activityId}`, method: 'get' })
export const doSeckill = (seckillGoodsId) =>
  request({ url: '/seckill/order', method: 'post', params: { seckillGoodsId } })
export const getSeckillResult = (seckillGoodsId) =>
  request({ url: `/seckill/order/result/${seckillGoodsId}`, method: 'get' })
export const getMySeckillOrders = (params) =>
  request({ url: '/seckill/order/my', method: 'get', params })

// 课程
export const getCoursePage = (params) => request({ url: '/course/page', method: 'get', params })
export const getSchedulePage = (params) =>
  request({ url: '/course/schedule/page', method: 'get', params })
export const enrollSchedule = (scheduleId) =>
  request({ url: '/course/schedule/enroll', method: 'post', params: { scheduleId } })

// 餐食
export const getFoods = () => request({ url: '/food/list', method: 'get', params: { status: 1 } })

// 会员卡
export const getMemberCards = () => request({ url: '/member/card/list', method: 'get' })
export const openMemberCard = (cardId) =>
  request({ url: '/member/open', method: 'post', params: { cardId } })
export const getMyCards = () => request({ url: '/member/user/myCards', method: 'get' })

// 订单
export const createOrder = (data) => request({ url: '/order/create', method: 'post', data })
export const getMyOrders = (params) => request({ url: '/order/my', method: 'get', params })
export const getOrderDetail = (orderNo) => request({ url: `/order/${orderNo}`, method: 'get' })
export const cancelOrder = (orderNo) =>
  request({ url: `/order/cancel/${orderNo}`, method: 'post' })

// 支付
export const pay = (data) => request({ url: '/payment/pay', method: 'post', data })
export const mockCallback = (payType, orderNo, tradeNo) =>
  request({
    url: payType === 2 ? '/payment/callback/alipay' : '/payment/callback/wechat',
    method: 'post',
    params: payType === 2 ? { outTradeNo: orderNo, tradeNo } : { outTradeNo: orderNo, transactionId: tradeNo }
  })
export const getMyPayments = (params) =>
  request({ url: '/payment/my', method: 'get', params })
