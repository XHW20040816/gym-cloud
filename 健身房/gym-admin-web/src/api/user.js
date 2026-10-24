import request from './request'

export function getUserList(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params
  })
}

export function getUserById(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export function createUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: '/user',
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/user/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function resetPassword(id) {
  return request({
    url: `/user/${id}/reset-password`,
    method: 'put'
  })
}
