// 认证工具函数
import axios from 'axios'

// 检查用户是否已登录
export const isAuthenticated = () => {
  return localStorage.getItem('isLoggedIn') === 'true'
}

// 获取当前登录用户信息
export const getCurrentUser = () => {
  const userStr = localStorage.getItem('currentUser')
  return userStr ? JSON.parse(userStr) : null
}

// 获取认证令牌
export const getAuthToken = () => {
  return localStorage.getItem('authToken')
}

// 登录
export const login = (userData) => {
  localStorage.setItem('isLoggedIn', 'true')
  localStorage.setItem('currentUser', JSON.stringify(userData))
  
  // 如果有新的令牌，也保存它
  const token = getAuthToken()
  if (token) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
  }
}

// 登出
export const logout = () => {
  localStorage.removeItem('isLoggedIn')
  localStorage.removeItem('currentUser')
  localStorage.removeItem('authToken')
  
  // 删除认证头
  delete axios.defaults.headers.common['Authorization']
}

// 设置认证请求头
export const setAuthHeader = (token) => {
  if (token) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
  } else {
    delete axios.defaults.headers.common['Authorization']
  }
}

// 初始化认证状态
export const initAuth = () => {
  const token = getAuthToken()
  if (token) {
    setAuthHeader(token)
  }
}