import request from './request'

// 商品 API
export function getGoodsList(params) {
  return request({
    url: '/goods/page',
    method: 'get',
    params
  })
}

export function getGoodsById(id) {
  return request({
    url: `/goods/${id}`,
    method: 'get'
  })
}

export function createGoods(data) {
  return request({
    url: '/goods',
    method: 'post',
    data
  })
}

export function updateGoods(data) {
  return request({
    url: '/goods',
    method: 'put',
    data
  })
}

export function deleteGoods(id) {
  return request({
    url: `/goods/${id}`,
    method: 'delete'
  })
}

export function updateGoodsStatus(id, status) {
  return request({
    url: '/goods/status',
    method: 'put',
    params: { id, status }
  })
}

// 商品分类 API
export function getCategoryList() {
  return request({
    url: '/goods/category/list',
    method: 'get'
  })
}

export function createCategory(data) {
  return request({
    url: '/goods/category',
    method: 'post',
    data
  })
}

export function updateCategory(data) {
  return request({
    url: '/goods/category',
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/goods/category/${id}`,
    method: 'delete'
  })
}
