import request from '@/utils/request'

export function listLoginLogs() {
  return request({
    url: '/login-logs',
    method: 'get'
  })
}
