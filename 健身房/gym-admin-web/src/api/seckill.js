import request from './request'

// 秒杀活动 API
export function getSeckillActivityList(params) {
  return request({
    url: '/seckill/admin/activity/page',
    method: 'get',
    params
  })
}

export function createSeckillActivity(data) {
  return request({
    url: '/seckill/admin/activity',
    method: 'post',
    data
  })
}

export function updateSeckillActivity(data) {
  return request({
    url: '/seckill/admin/activity',
    method: 'put',
    data
  })
}

export function deleteSeckillActivity(id) {
  return request({
    url: `/seckill/admin/activity/${id}`,
    method: 'delete'
  })
}

export function updateSeckillActivityStatus(id, status) {
  return request({
    url: `/seckill/admin/activity/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 秒杀商品 API
export function getSeckillGoodsList(activityId) {
  return request({
    url: `/seckill/admin/goods/list/${activityId}`,
    method: 'get'
  })
}

export function createSeckillGoods(data) {
  return request({
    url: '/seckill/admin/goods',
    method: 'post',
    data
  })
}

export function updateSeckillGoods(data) {
  return request({
    url: '/seckill/admin/goods',
    method: 'put',
    data
  })
}

export function deleteSeckillGoods(id) {
  return request({
    url: `/seckill/admin/goods/${id}`,
    method: 'delete'
  })
}
