import request from './request'

export function getOrderList(params) {
  return request({
    url: '/order/page',
    method: 'get',
    params
  })
}

export function getOrderDetail(orderNo) {
  return request({
    url: `/order/${orderNo}`,
    method: 'get'
  })
}

export function cancelOrder(orderNo) {
  return request({
    url: `/order/${orderNo}/cancel`,
    method: 'post'
  })
}

export function updateOrderStatus(orderNo, status) {
  return request({
    url: `/order/${orderNo}/status`,
    method: 'put',
    params: { status }
  })
}
