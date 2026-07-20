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

// 获取协议类型列表
export const getProtocolTypes = () => {
  // 目前后端没有专门的枚举查询接口，直接返回前端预定义的类型
  return Promise.resolve({
    data: {
      code: 200,
      data: [
        { code: 'SQL', description: '数据库查询' },
        { code: 'WS', description: 'WebService' }
      ]
    }
  })
}