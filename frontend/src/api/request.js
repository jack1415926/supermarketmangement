/**
 * Axios 封装模块
 * 功能：统一请求/响应拦截、自动携带JWT Token、401自动登出
 */
import axios from 'axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 Axios 实例，baseURL 通过 Vite proxy 代理到后端 8080 端口
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：自动在请求头中携带 JWT Token
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理 401 未授权和 token 过期
request.interceptors.response.use(
  response => {
    const res = response.data
    // 后端返回 code=401 表示 token 无效或过期
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      return Promise.reject(new Error(res.message || '登录已过期'))
    }
    return res
  },
  error => {
    // HTTP 状态码 401 同样处理
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default request
