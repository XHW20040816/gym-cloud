import request from './request'

export function getOrderList(params) {
  return request({
    url: '/order/page',
    method: 'get',
    params
  })
}

export function getOrderById(id) {
  return request({
    url: `/order/${id}`,
    method: 'get'
  })
}

export function cancelOrder(id) {
  return request({
    url: `/order/${id}/cancel`,
    method: 'put'
  })
}

export function updateOrderStatus(id, status) {
  return request({
    url: `/order/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 秒杀活动 API
export function getSeckillActivityList(params) {
  return request({
    url: '/seckill-activity/page',
    method: 'get',
    params
  })
}

export function getSeckillActivityById(id) {
  return request({
    url: `/seckill-activity/${id}`,
    method: 'get'
  })
}

export function createSeckillActivity(data) {
  return request({
    url: '/seckill-activity',
    method: 'post',
    data
  })
}

export function updateSeckillActivity(data) {
  return request({
    url: '/seckill-activity',
    method: 'put',
    data
  })
}

export function deleteSeckillActivity(id) {
  return request({
    url: `/seckill-activity/${id}`,
    method: 'delete'
  })
}

export function updateSeckillStatus(id, status) {
  return request({
    url: `/seckill-activity/${id}/status`,
    method: 'put',
    params: { status }
  })
}
