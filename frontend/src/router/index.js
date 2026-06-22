/**
 * 路由配置
 * 功能：页面路由定义、路由懒加载、路由守卫（登录验证 + 角色权限控制）
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由表定义
const routes = [
  {
    // 登录页（无需认证）
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { noAuth: true }
  },
  {
    // 主布局（所有登录后页面共用）
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      // 工作台 / 首页概览
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'),
        meta: { roles: ['admin', 'superadmin'] } },
      // POS 收银台（仅收银员）
      { path: 'pos', name: 'POS', component: () => import('@/views/POS.vue'),
        meta: { roles: ['cashier'] } },
      // 商品管理
      { path: 'products', name: 'Products', component: () => import('@/views/Products.vue'),
        meta: { roles: ['admin', 'superadmin'] } },
      // 进货管理
      { path: 'purchases', name: 'Purchases', component: () => import('@/views/Purchases.vue'),
        meta: { roles: ['admin', 'superadmin'] } },
      // 库存管理
      { path: 'inventory', name: 'Inventory', component: () => import('@/views/Inventory.vue'),
        meta: { roles: ['admin', 'superadmin'] } },
      // 会员管理
      { path: 'members', name: 'Members', component: () => import('@/views/Members.vue'),
        meta: { roles: ['admin', 'superadmin'] } },
      // 员工管理
      { path: 'employees', name: 'Employees', component: () => import('@/views/Employees.vue'),
        meta: { roles: ['admin', 'superadmin'] } },
      // 供应商管理
      { path: 'suppliers', name: 'Suppliers', component: () => import('@/views/Suppliers.vue'),
        meta: { roles: ['admin', 'superadmin'] } },
      // 销售记录
      { path: 'sales', name: 'Sales', component: () => import('@/views/Sales.vue'),
        meta: { roles: ['admin', 'superadmin'] } },
      // 销售分析（排行榜、日报/月报、业绩统计）
      { path: 'reports', name: 'Reports', component: () => import('@/views/Reports.vue'),
        meta: { roles: ['admin', 'superadmin'] } },
      // 系统管理（仅系统管理员）
      { path: 'system', name: 'System', component: () => import('@/views/System.vue'),
        meta: { roles: ['superadmin'] } }
    ]
  },
  {
    // 换班汇总页（收银员专用，独立于主布局）
    path: '/shift',
    name: 'Shift',
    component: () => import('@/views/Shift.vue'),
    meta: { roles: ['cashier'] }
  }
]

const router = createRouter({
  history: createWebHistory(),  // HTML5 History 模式
  routes
})

/**
 * 全局路由守卫
 * 1. 未登录 → 强制跳转 /login
 * 2. 已登录但角色无权限 → 跳转 /dashboard
 */
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 登录页放行
  if (to.meta.noAuth) {
    return next()
  }

  // 未登录 → 跳登录页
  if (!userStore.isLoggedIn) {
    return next('/login')
  }

  // 角色权限校验
  const allowedRoles = to.meta.roles
  if (allowedRoles && !allowedRoles.includes(userStore.role)) {
    return next('/dashboard')
  }

  next()
})

export default router
