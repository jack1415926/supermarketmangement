<!--
  主布局组件
  功能：左侧可折叠侧边栏 + 顶部导航栏 + 根据用户角色动态显示菜单
  角色-菜单对应：
    cashier (收银员)  → POS收银台
    admin   (管理员)  → 工作台/商品/进货/库存/会员/人员/供应商/销售/报表
    superadmin(系统管理员) → 管理员所有菜单 + 系统管理
-->
<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>超市管理系统</h2>
      </div>
      <nav class="sidebar-nav">
        <template v-for="item in menuItems" :key="item.path">
          <router-link
            v-if="item.roles.includes(userStore.role)"
            :to="item.path"
            class="nav-item"
            active-class="active"
          >
            {{ item.label }}
          </router-link>
        </template>
      </nav>
    </aside>

    <div class="main-area">
      <header class="topbar">
        <span class="role-badge">{{ roleLabel }}</span>
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
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 角色中文映射
const roleLabel = computed(() => {
  const map = { cashier: '收银员', admin: '管理员', superadmin: '系统管理员' }
  return map[userStore.role] || userStore.role
})

// 菜单配置：每个菜单项指定可访问的角色
const menuItems = [
  { path: '/pos',       label: 'POS收银台',   roles: ['cashier'] },
  { path: '/dashboard', label: '工作台',       roles: ['admin', 'superadmin'] },
  { path: '/products',  label: '商品管理',     roles: ['admin', 'superadmin'] },
  { path: '/members',   label: '会员管理',     roles: ['admin', 'superadmin'] },
  { path: '/employees', label: '人员管理',     roles: ['admin', 'superadmin'] },
  { path: '/suppliers', label: '供应商管理',   roles: ['admin', 'superadmin'] },
  { path: '/purchases', label: '进货管理',     roles: ['admin', 'superadmin'] },
  { path: '/inventory', label: '库存管理',     roles: ['admin', 'superadmin'] },
  { path: '/sales',     label: '销售记录',     roles: ['admin', 'superadmin'] },
  { path: '/reports',   label: '销售分析',     roles: ['admin', 'superadmin'] },
  { path: '/system',    label: '系统管理',     roles: ['superadmin'] }
]

/** 退出登录 */
function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { display: flex; min-height: 100vh; }

/* 侧边栏 */
.sidebar { width: 220px; background: #1a1a2e; color: #eee; display: flex; flex-direction: column; }
.sidebar-header { padding: 20px 16px; text-align: center; border-bottom: 1px solid #2a2a4e; }
.sidebar-header h2 { font-size: 16px; color: #eee; }
.sidebar-nav { flex: 1; padding: 12px 0; overflow-y: auto; }

/* 菜单项 */
.nav-item { display: flex; padding: 12px 20px; color: #aaa; text-decoration: none; transition: all 0.2s; }
.nav-item:hover { background: #16213e; color: #fff; }
.nav-item.active { background: #667eea; color: #fff; }

/* 主区域 */
.main-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }

/* 顶部栏 */
.topbar { height: 56px; background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.role-badge { background: #e8f0fe; color: #667eea; padding: 4px 12px; border-radius: 12px; font-size: 13px; }
.topbar-right { display: flex; align-items: center; gap: 16px; }
.user-name { color: #333; font-size: 14px; }
.logout-btn { padding: 6px 16px; border: 1px solid #ddd; border-radius: 6px; background: #fff; color: #666; cursor: pointer; font-size: 13px; }
.logout-btn:hover { color: #ff4d4f; border-color: #ff4d4f; }

/* 内容区 */
.content { flex: 1; padding: 24px; overflow-y: auto; background: #f5f6fa; }
</style>
