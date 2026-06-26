import axios from 'axios'
import { Message } from 'element-ui'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

let refreshing = false
let refreshQueue = []

function clearLoginState() {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
}

function redirectToLogin() {
  if (window.location.hash.indexOf('#/login') !== 0) {
    window.location.hash = '#/login'
  }
}

function subscribeRefresh(resolve, reject, originalRequest) {
  refreshQueue.push({ resolve, reject, originalRequest })
}

function notifyRefreshSuccess(token) {
  refreshQueue.forEach(item => {
    item.originalRequest.headers = item.originalRequest.headers || {}
    item.originalRequest.headers.Authorization = 'Bearer ' + token
    item.resolve(service(item.originalRequest))
  })
  refreshQueue = []
}

function notifyRefreshFail(error) {
  refreshQueue.forEach(item => item.reject(error))
  refreshQueue = []
}

function requestRefreshToken() {
  const refreshToken = localStorage.getItem('refreshToken')
  if (!refreshToken) {
    return Promise.reject(new Error('refreshToken不存在'))
  }
  return axios.post('/api/auth/refresh', { refreshToken })
}

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res.code !== 200 && res.code !== 0) {
      Message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response && error.response.status === 401) {
      const originalRequest = error.config || {}
      if (originalRequest.url && originalRequest.url.indexOf('/auth/refresh') >= 0) {
        clearLoginState()
        Message.error('登录已过期，请重新登录')
        redirectToLogin()
        return Promise.reject(error)
      }
      if (originalRequest._retry) {
        clearLoginState()
        Message.error('登录已过期，请重新登录')
        redirectToLogin()
        return Promise.reject(error)
      }
      originalRequest._retry = true

      if (refreshing) {
        return new Promise((resolve, reject) => {
          subscribeRefresh(resolve, reject, originalRequest)
        })
      }

      refreshing = true
      return requestRefreshToken()
        .then(response => {
          const res = response.data
          if (res.code !== 200 && res.code !== 0) {
            throw new Error(res.message || '刷新登录状态失败')
          }
          const data = res.data || {}
          localStorage.setItem('token', data.token)
          if (data.refreshToken) {
            localStorage.setItem('refreshToken', data.refreshToken)
          }
          if (data.user) {
            localStorage.setItem('user', JSON.stringify(data.user))
          }
          notifyRefreshSuccess(data.token)
          originalRequest.headers = originalRequest.headers || {}
          originalRequest.headers.Authorization = 'Bearer ' + data.token
          return service(originalRequest)
        })
        .catch(refreshError => {
          clearLoginState()
          notifyRefreshFail(refreshError)
          Message.error('登录已过期，请重新登录')
          redirectToLogin()
          return Promise.reject(refreshError)
        })
        .finally(() => {
          refreshing = false
        })
    } else {
      Message.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
