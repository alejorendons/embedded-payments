import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/login.vue'),
    meta: {
      requiresAuth: false,
    },
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/pages/dashboard.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/pages/settings/index.vue'),
    meta: {
      requiresAuth: true,
    },
    children: [
      {
        path: 'contact',
        name: 'ContactSettings',
        component: () => import('@/pages/settings/contact.vue'),
      },
      {
        path: 'bank',
        name: 'BankSettings',
        component: () => import('@/pages/settings/bank.vue'),
      },
      {
        path: 'profile',
        name: 'ProfileSettings',
        component: () => import('@/pages/settings/profile.vue'),
      },
    ],
  },
  {
    path: '/transactions',
    name: 'Transactions',
    component: () => import('@/pages/transactions/index.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/transactions/:id',
    name: 'TransactionDetail',
    component: () => import('@/pages/transactions/[id].vue'),
    meta: {
      requiresAuth: true,
    },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  await authStore.initializeSession()
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
