import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  {
    path: '/', component: () => import('@/components/Layout.vue'),
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'pos', name: 'POS', component: () => import('@/views/POS.vue'), meta: { roles: ['cashier', 'admin', 'superadmin'] } },
      { path: 'products', name: 'Products', component: () => import('@/views/Products.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'purchases', name: 'Purchases', component: () => import('@/views/Purchases.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'inventory', name: 'Inventory', component: () => import('@/views/Inventory.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'members', name: 'Members', component: () => import('@/views/Members.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'employees', name: 'Employees', component: () => import('@/views/Employees.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'suppliers', name: 'Suppliers', component: () => import('@/views/Suppliers.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'sales', name: 'Sales', component: () => import('@/views/Sales.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'reports', name: 'Reports', component: () => import('@/views/Reports.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'system', name: 'System', component: () => import('@/views/System.vue'), meta: { roles: ['superadmin'] } },
      { path: 'shift', name: 'Shift', component: () => import('@/views/Shift.vue'), meta: { roles: ['cashier', 'admin', 'superadmin'] } }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from) => {
  const token = localStorage.getItem('supermarket_token')
  const user = JSON.parse(localStorage.getItem('supermarket_user') || 'null')
  const role = user?.role || ''

  // 未登录访问非登录页 → 跳登录
  if (to.path !== '/login' && !token) return '/login'
  // 已登录访问登录页 → 跳首页
  if (to.path === '/login' && token) return '/'

  // 角色权限检查
  const allowed = to.meta.roles
  if (!allowed) return  // 无限制页面直接放行

  // 有 token 但 role 为空 → 数据异常，踢回登录
  if (!role) {
    localStorage.removeItem('supermarket_token')
    localStorage.removeItem('supermarket_user')
    return '/login'
  }

  // 角色不匹配 → 按角色跳对应首页
  if (!allowed.includes(role)) {
    if (role === 'cashier') return '/pos'
    return '/'
  }
})

export default router
