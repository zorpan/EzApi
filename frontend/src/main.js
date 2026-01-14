import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import VXETable from 'vxe-table'
import 'vxe-table/lib/style.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import axios from 'axios'
import { initAuth } from './utils/auth.js'

// 设置axios的基础URL（如果需要的话）
// axios.defaults.baseURL = 'http://localhost:8080'

// 初始化认证状态
initAuth()

// 请求拦截器 - 添加认证头
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理认证失效
axios.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (error.response?.status === 401) {
      // 认证失败，跳转到登录页
      localStorage.removeItem('isLoggedIn');
      localStorage.removeItem('currentUser');
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

const app = createApp(App)

app.use(router)
app.use(VXETable)
app.use(ElementPlus)

app.mount('#app')