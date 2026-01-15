import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '../utils/auth.js'

const routes = [
  {
    path: '/',
    name: 'Home',
    redirect: '/datasources'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresGuest: true } // 需要未登录状态才能访问
  },
  {
    path: '/datasources',
    name: 'DataSourceList',
    component: () => import('../views/DataSourceList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/queries',
    name: 'QueryList',
    component: () => import('../views/QueryList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/executions',
    name: 'ExecutionList',
    component: () => import('../views/ExecutionList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/apis',
    name: 'ApiManagement',
    component: () => import('../views/ApiManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/auth',
    name: 'AuthorizationManagement',
    component: () => import('../views/AuthorizationManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/users',
    name: 'UserManagement',
    component: () => import('../views/UserManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const loggedIn = isAuthenticated()
  
  // 如果目标路由需要认证，但用户未登录
  if (to.meta.requiresAuth && !loggedIn) {
    next({ name: 'Login' })
  }
  // 如果用户已登录，但尝试访问登录页
  else if (to.meta.requiresGuest && loggedIn) {
    next({ name: 'Home' })
  }
  // 其他情况正常导航
  else {
    next()
  }
})

export default router