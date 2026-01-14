import request from '@/utils/request.js';

// 获取所有API信息
export const getAllApis = () => {
  return request({
    url:'/api/management/apis',
    method:'get'
  })
}

// 获取单个API信息
export const getApiById = (id) => {
  return request({
    url: `/api/management/api/${id}`,
    method: 'get'
  })
}

// 保存API（创建或更新）
export const saveApi = (apiData) => {
  return request({
    url: '/api/management/api',
    method: 'post',
    data: apiData
  })
}

// 删除API
export const deleteApi = (id) => {
  return request({
    url: `/api/management/api/${id}`,
    method: 'delete'
  })
}

// 获取API参数
export const getApiParameters = (id) => {
  return request({
    url: `/api/management/api/${id}/parameters`,
    method: 'get'
  })
}

// 更新API状态
export const updateApiStatus = (id, statusData) => {
  return request({
    url: `/api/management/api/${id}/status`,
    method: 'put',
    data: statusData
  })
}