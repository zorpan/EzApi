import axios from 'axios';
import { getAuthToken } from '@/utils/auth.js';

const service = axios.create({
  baseURL: "/",
  withCredentials: true,
  timeout: 5000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = getAuthToken();
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 统一处理响应，如果后端返回的是Result格式
    return response;
  },
  error => {
    console.error('Response error:', error);
    
    // 全局错误处理
    if (error.response?.status === 401) {
      // 认证失败，跳转到登录页
      localStorage.removeItem('authToken');
      localStorage.removeItem('isLoggedIn');
      localStorage.removeItem('currentUser');
      window.location.href = '/#/login';
      window.location.reload();
    }
    
    return Promise.reject(error);
  }
)

export default service
