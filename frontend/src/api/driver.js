import request from '@/utils/request.js'

// 获取所有驱动
export const getAllDrivers = () => {
  return request({
    url: '/api/database-driver/list',
    method: 'get'
  })
}

// 添加驱动
export const addDriver = (formData) => {
  return request({
    url: '/api/database-driver',
    method: 'post',
    data: formData
    // 注意：不要手动设置Content-Type，让浏览器自动设置
  })
}

// 更新驱动
export const updateDriver = (id, formData) => {
  return request({
    url: `/api/database-driver/${id}`,
    method: 'put',
    data: formData
    // 注意：不要手动设置Content-Type，让浏览器自动设置
  })
}

// 删除驱动
export const removeDriver = (id) => {
  return request({
    url: `/api/database-driver/${id}`,
    method: 'delete'
  })
}

// 切换驱动状态
export const toggleDriverStatus = (id, status) => {
  return request({
    url: `/api/database-driver/${id}/toggle-status`,
    method: 'put',
    params: {
      status: status
    }
  })
}

// 获取驱动详情
export const getDriverById = (id) => {
  return request({
    url: `/api/database-driver/${id}`,
    method: 'get'
  })
}