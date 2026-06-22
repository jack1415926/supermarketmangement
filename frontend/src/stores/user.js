/**
 * 用户状态管理 (Pinia Store)
 * 功能：登录状态、Token 管理、用户信息持久化（localStorage）
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/api/request'

// localStorage 存储键名
const TOKEN_KEY = 'supermarket_token'
const USER_KEY = 'supermarket_user'

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 恢复登录状态（刷新页面不丢失）
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  // 计算属性：是否已登录
  const isLoggedIn = computed(() => !!token.value)
  // 计算属性：当前用户角色（cashier / admin / superadmin）
  const role = computed(() => user.value?.role || '')
  // 计算属性：当前用户名
  const username = computed(() => user.value?.username || '')

  /**
   * 登录
   * @param {string} username - 用户名
   * @param {string} password - 密码
   * @returns {object} 用户信息
   */
  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.data.token
    user.value = res.data.user
    // 持久化到 localStorage
    localStorage.setItem(TOKEN_KEY, res.data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(res.data.user))
    return res.data.user
  }

  /** 获取当前用户最新信息（刷新用户状态） */
  async function fetchUserInfo() {
    const res = await request.get('/auth/me')
    user.value = res.data
    localStorage.setItem(USER_KEY, JSON.stringify(res.data))
  }

  /** 登出：清除 token 和用户信息 */
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, user, isLoggedIn, role, username, login, fetchUserInfo, logout }
})
