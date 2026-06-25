/**
 * 用户状态管理 (Pinia Store)
 * 功能：登录状态、Token 持久化、后端角色→前端角色映射
 * 后端角色：ROLE_ADMIN / ROLE_MANAGER / ROLE_CASHIER → 前端角色：superadmin / admin / cashier
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/api/request'

const TOKEN_KEY = 'supermarket_token'
const USER_KEY = 'supermarket_user'

// 角色映射表：后端 Spring Security 角色 → 前端简化角色
const ROLE_MAP = {
  ROLE_ADMIN: 'superadmin',
  ROLE_MANAGER: 'admin',
  ROLE_CASHIER: 'cashier',
  // 兼容不带前缀的旧格式
  ADMIN: 'superadmin',
  MANAGER: 'admin',
  CASHIER: 'cashier'
}

/** 从 localStorage 恢复用户信息并转换角色格式 */
function loadUser() {
  const raw = JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  if (raw && raw.role) {
    raw.role = ROLE_MAP[raw.role] || raw.role
  }
  return raw
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(loadUser())

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role || '')
  const username = computed(() => user.value?.username || '')

  /** 登录，返回映射后的用户对象 */
  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    // 后端返回格式：{ code: 200, message: "...", data: { token, username, displayName, role: "ROLE_ADMIN" } }
    const mapped = {
      username: res.data.username,
      displayName: res.data.displayName,
      role: ROLE_MAP[res.data.role] || res.data.role
    }
    token.value = res.data.token
    user.value = mapped
    localStorage.setItem(TOKEN_KEY, res.data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(mapped))
    return mapped
  }

  /** 获取当前用户信息（/auth/me） */
  async function fetchUserInfo() {
    const res = await request.get('/auth/me')
    const mapped = {
      username: res.data.username,
      displayName: res.data.displayName,
      role: ROLE_MAP[res.data.role] || res.data.role
    }
    user.value = mapped
    localStorage.setItem(USER_KEY, JSON.stringify(mapped))
  }

  /** 退出登录 */
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, user, isLoggedIn, role, username, login, fetchUserInfo, logout }
})
