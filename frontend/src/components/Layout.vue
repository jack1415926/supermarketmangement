<template>
  <div class="layout">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <h2 v-show="!sidebarCollapsed">超市管理系统</h2>
        <span v-show="sidebarCollapsed" class="logo-mini">SM</span>
      </div>
      <nav class="sidebar-nav">
        <template v-for="item in menuItems" :key="item.path">
          <router-link
            v-if="item.roles.includes(userStore.role)"
            :to="item.path"
            class="nav-item"
            active-class="active"
          >
            <span class="nav-icon">{{ item.icon }}</span>
            <span v-show="!sidebarCollapsed" class="nav-label">{{ item.label }}</span>
          </router-link>
        </template>
      </nav>
      <div class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
        {{ sidebarCollapsed ? '▶' : '◀' }}
      </div>
    </aside>

    <div class="main-area">
      <header class="topbar">
        <div class="topbar-left">
          <span class="role-badge">{{ roleLabel }}</span>
        </div>
        <div class="topbar-right">
          <span class="user-name">{{ userStore.username }}</span>
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const sidebarCollapsed = ref(false)

const roleLabel = computed(() => {
  const map = { cashier: '收银员', admin: '管理员', superadmin: '系统管理员' }
  return map[userStore.role] || userStore.role
})

const menuItems = [
  // 收银员菜单
  { path: '/pos', label: 'POS收银台', icon: '💰', roles: ['cashier'] },
  // 管理员菜单
  { path: '/dashboard', label: '工作台', icon: '📊', roles: ['admin', 'superadmin'] },
  { path: '/products', label: '商品管理', icon: '📦', roles: ['admin', 'superadmin'] },
  { path: '/purchases', label: '进货管理', icon: '📥', roles: ['admin', 'superadmin'] },
  { path: '/inventory', label: '库存管理', icon: '🏪', roles: ['admin', 'superadmin'] },
  { path: '/members', label: '会员管理', icon: '👤', roles: ['admin', 'superadmin'] },
  { path: '/employees', label: '人员管理', icon: '👥', roles: ['admin', 'superadmin'] },
  { path: '/suppliers', label: '供应商管理', icon: '🏭', roles: ['admin', 'superadmin'] },
  { path: '/sales', label: '销售记录', icon: '📋', roles: ['admin', 'superadmin'] },
  { path: '/reports', label: '销售分析', icon: '📈', roles: ['admin', 'superadmin'] },
  // 系统管理员菜单
  { path: '/system', label: '系统管理', icon: '⚙️', roles: ['superadmin'] }
]

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  background: #1a1a2e;
  color: #eee;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  position: relative;
}

.sidebar.collapsed {
  width: 60px;
}

.sidebar-header {
  padding: 20px 16px;
  text-align: center;
  border-bottom: 1px solid #2a2a4e;
}

.sidebar-header h2 {
  font-size: 16px;
  white-space: nowrap;
}

.logo-mini {
  font-size: 20px;
  font-weight: bold;
  color: #667eea;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 0;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #aaa;
  text-decoration: none;
  transition: all 0.2s;
  white-space: nowrap;
}

.nav-item:hover {
  background: #16213e;
  color: #fff;
}

.nav-item.active {
  background: #667eea;
  color: #fff;
}

.nav-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.nav-label {
  margin-left: 10px;
  font-size: 14px;
}

.sidebar-toggle {
  padding: 12px;
  text-align: center;
  cursor: pointer;
  border-top: 1px solid #2a2a4e;
  color: #aaa;
  font-size: 12px;
  user-select: none;
}

.sidebar-toggle:hover {
  background: #16213e;
  color: #fff;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.topbar {
  height: 56px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 10;
}

.role-badge {
  background: #e8f0fe;
  color: #667eea;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-name {
  color: #333;
  font-size: 14px;
}

.logout-btn {
  padding: 6px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  color: #666;
  cursor: pointer;
  font-size: 13px;
}

.logout-btn:hover {
  color: #ff4d4f;
  border-color: #ff4d4f;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #f5f6fa;
}
</style>
