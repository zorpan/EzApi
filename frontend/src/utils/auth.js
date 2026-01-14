// 认证工具函数

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

  // 令牌会在request拦截器中自动添加，无需手动设置
}

// 登出
export const logout = () => {
  localStorage.removeItem('isLoggedIn')
  localStorage.removeItem('currentUser')
  localStorage.removeItem('authToken')

  // 认证头会在request拦截器中自动处理
}

// 设置认证请求头
// 注意：现在认证头由request拦截器自动处理
export const setAuthHeader = (token) => {
  // 认证头会在request拦截器中自动从localStorage获取
  // 这个函数保留是为了向后兼容
}

// 初始化认证状态
export const initAuth = () => {
  const token = getAuthToken()
  if (token) {
    setAuthHeader(token)
  }
}