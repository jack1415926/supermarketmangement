import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'pos', name: 'POS', component: () => import('@/views/POS.vue'), meta: { roles: ['cashier'] } },
      { path: 'products', name: 'Products', component: () => import('@/views/Products.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'purchases', name: 'Purchases', component: () => import('@/views/Purchases.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'inventory', name: 'Inventory', component: () => import('@/views/Inventory.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'members', name: 'Members', component: () => import('@/views/Members.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'employees', name: 'Employees', component: () => import('@/views/Employees.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'suppliers', name: 'Suppliers', component: () => import('@/views/Suppliers.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'sales', name: 'Sales', component: () => import('@/views/Sales.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'reports', name: 'Reports', component: () => import('@/views/Reports.vue'), meta: { roles: ['admin', 'superadmin'] } },
      { path: 'system', name: 'System', component: () => import('@/views/System.vue'), meta: { roles: ['superadmin'] } }
    ]
  },
  {
    path: '/shift',
    name: 'Shift',
    component: () => import('@/views/Shift.vue'),
    meta: { roles: ['cashier'] }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.noAuth) {
    return next()
  }

  if (!userStore.isLoggedIn) {
    return next('/login')
  }

  const allowedRoles = to.meta.roles
  if (allowedRoles && !allowedRoles.includes(userStore.role)) {
    return next('/dashboard')
  }

  next()
})

export default router
