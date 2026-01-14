import request from '@/utils/request.js'

// 用户登录
export const login = (credentials) => {
  return request({
    url: '/api/login',
    method: 'post',
    data: credentials
  })
}

// 获取当前用户信息（如果需要）
export const getCurrentUser = () => {
  // 这里可以根据实际需求实现
  const userStr = localStorage.getItem('currentUser')
  return userStr ? JSON.parse(userStr) : null
}