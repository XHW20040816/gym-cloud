import request from './request'

export function getPaymentList(params) {
  return request({
    url: '/payment/page',
    method: 'get',
    params
  })
}

export function getPaymentById(id) {
  return request({
    url: `/payment/${id}`,
    method: 'get'
  })
}

export function refundPayment(id, data) {
  return request({
    url: `/payment/${id}/refund`,
    method: 'post',
    data
  })
}

export function getPaymentStatistics(params) {
  return request({
    url: '/payment/statistics',
    method: 'get',
    params
  })
}
