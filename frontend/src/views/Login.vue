<template>
  <div class="login-container">
    <div class="login-form">
      <h2>用户登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            type="text" 
            id="username" 
            v-model="credentials.username" 
            placeholder="请输入用户名"
            required
          >
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="credentials.password" 
            placeholder="请输入密码"
            required
          >
        </div>
        <div class="form-group">
          <button type="submit" class="btn-login" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </div>
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'
import { login as authLogin, setAuthHeader } from '../utils/auth.js'

const router = useRouter()
const credentials = ref({
  username: '',
  password: ''
})
const loading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  loading.value = true
  errorMessage.value = ''
  
  try {
    const response = await authApi.login(credentials.value)
    
    // 适配新的Result响应结构
    if (response.data.code === 200) {
      // 保存用户信息和令牌到本地存储
      authLogin(response.data.data.user)
      
      // 保存JWT令牌到localStorage
      if (response.data.data && response.data.data.token) {
        localStorage.setItem('authToken', response.data.data.token)
        // 设置认证请求头
        setAuthHeader(response.data.data.token)
      }
      
      // 跳转到首页
      router.push('/')
    } else {
      errorMessage.value = response.data.message || '登录失败，请重试'
    }
  } catch (error) {
    console.error('登录错误:', error)
    errorMessage.value = error.response?.data?.message || error.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-form {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #606266;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 14px;
}

.form-group input:focus {
  outline: none;
  border-color: #409eff;
}

.btn-login {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.btn-login:hover:not(:disabled) {
  background-color: #66b1ff;
}

.btn-login:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.error-message {
  color: #f56c6c;
  font-size: 14px;
  text-align: center;
  margin-top: 15px;
}
</style>