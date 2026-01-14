<template>
  <div id="app">
    <header class="header">
      <h1>API管理系统</h1>
      <div class="user-info" v-if="isAuthenticated">
        <span>欢迎, {{ currentUser.username }}</span>
        <button class="btn-logout" @click="handleLogout">退出登录</button>
      </div>
    </header>
    <Navigation v-if="isAuthenticated" />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Navigation from './components/Navigation.vue'
import { isAuthenticated as authCheck, getCurrentUser, logout, initAuth } from './utils/auth.js'

const router = useRouter()
const isAuthenticated = ref(false)
const currentUser = ref(null)

onMounted(() => {
  // 初始化认证状态
  initAuth()
  checkAuthStatus()
})

const checkAuthStatus = () => {
  isAuthenticated.value = authCheck()
  if (isAuthenticated.value) {
    currentUser.value = getCurrentUser()
  } else {
    // 如果未登录且不在登录页，则跳转到登录页
    if (router.currentRoute.value.path !== '/login') {
      router.push('/login')
    }
  }
}

const handleLogout = () => {
  logout()
  isAuthenticated.value = false
  currentUser.value = null
  router.push('/login')
}
</script>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #fff;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0,21,41,.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.header h1 {
  margin: 0;
  color: #1f2d3d;
  font-size: 18px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn-logout {
  padding: 6px 12px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-logout:hover {
  background-color: #f78989;
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}
</style>