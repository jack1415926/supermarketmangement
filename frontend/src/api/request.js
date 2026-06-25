import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('supermarket_token')
    if (token) config.headers.Authorization = 'Bearer ' + token
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 401) {
      localStorage.removeItem('supermarket_token')
      localStorage.removeItem('supermarket_user')
      window.location.href = '/login'
      return Promise.reject(new Error('登录已过期'))
    }
    return res
  },
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('supermarket_token')
      localStorage.removeItem('supermarket_user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
