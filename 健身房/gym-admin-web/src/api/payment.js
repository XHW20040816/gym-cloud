import request from './request'

export function getPaymentList(params) {
  return request({
    url: '/payment/page',
    method: 'get',
    params
  })
}

export function getPaymentDetail(paymentNo) {
  return request({
    url: `/payment/detail/${paymentNo}`,
    method: 'get'
  })
}

export function refundPayment(paymentNo) {
  return request({
    url: `/payment/${paymentNo}/refund`,
    method: 'post'
  })
}

export function getPaymentStatistics() {
  return request({
    url: '/payment/statistics',
    method: 'get'
  })
}
