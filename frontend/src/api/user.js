import request from '@/utils/request'

export function listUsers(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

export function addUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

export function updateUser(userId, data) {
  return request({
    url: `/users/${userId}`,
    method: 'put',
    data
  })
}

export function deleteUser(userId) {
  return request({
    url: `/users/${userId}`,
    method: 'delete'
  })
}
