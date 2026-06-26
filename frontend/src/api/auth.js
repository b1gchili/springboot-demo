import request from '@/utils/request'

export function loginByPassword(data) {
  return request({
    url: '/auth/login/password',
    method: 'post',
    data
  })
}

export function sendSmsCode(data) {
  return request({
    url: '/auth/sms/code',
    method: 'post',
    data
  })
}

export function loginBySms(data) {
  return request({
    url: '/auth/login/sms',
    method: 'post',
    data
  })
}
