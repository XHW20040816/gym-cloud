import request from './request'

// 课程 API
export function getCourseList(params) {
  return request({
    url: '/course/page',
    method: 'get',
    params
  })
}

export function getCourseById(id) {
  return request({
    url: `/course/${id}`,
    method: 'get'
  })
}

export function createCourse(data) {
  return request({
    url: '/course',
    method: 'post',
    data
  })
}

export function updateCourse(data) {
  return request({
    url: '/course',
    method: 'put',
    data
  })
}

export function deleteCourse(id) {
  return request({
    url: `/course/${id}`,
    method: 'delete'
  })
}

export function updateCourseStatus(id, status) {
  return request({
    url: `/course/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 课程排期 API
export function getScheduleList(params) {
  return request({
    url: '/schedule/page',
    method: 'get',
    params
  })
}

export function getScheduleById(id) {
  return request({
    url: `/schedule/${id}`,
    method: 'get'
  })
}

export function createSchedule(data) {
  return request({
    url: '/schedule',
    method: 'post',
    data
  })
}

export function updateSchedule(data) {
  return request({
    url: '/schedule',
    method: 'put',
    data
  })
}

export function deleteSchedule(id) {
  return request({
    url: `/schedule/${id}`,
    method: 'delete'
  })
}

export function getCoachList() {
  return request({
    url: '/coach/list',
    method: 'get'
  })
}
