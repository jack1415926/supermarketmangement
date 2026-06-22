import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/api/request'

const TOKEN_KEY = 'supermarket_token'
const USER_KEY = 'supermarket_user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role || '')
  const username = computed(() => user.value?.username || '')

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem(TOKEN_KEY, res.data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(res.data.user))
    return res.data.user
  }

  async function fetchUserInfo() {
    const res = await request.get('/auth/me')
    user.value = res.data
    localStorage.setItem(USER_KEY, JSON.stringify(res.data))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, user, isLoggedIn, role, username, login, fetchUserInfo, logout }
})
