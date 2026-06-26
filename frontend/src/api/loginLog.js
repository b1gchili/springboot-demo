import request from '@/utils/request'

export function listLoginLogs(params) {
  return request({
    url: '/login-logs',
    method: 'get',
    params
  })
}
