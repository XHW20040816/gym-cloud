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

export function updateUserStatus(userId, status) {
  return request({
    url: '/user/status',
    method: 'put',
    params: { userId, status }
  })
}

export function resetPassword(id) {
  return request({
    url: `/user/${id}/reset-password`,
    method: 'put'
  })
}

export function getUserStatistics() {
  return request({
    url: '/user/statistics',
    method: 'get'
  })
}
