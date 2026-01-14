import request from '@/utils/request.js'

// 创建授权令牌
export const createToken = (tokenData) => {
  return request({
    url: '/api/auth/token/create',
    method: 'post',
    data: tokenData
  })
}

// 获取授权令牌详情
export const getToken = (id) => {
  return request({
    url: `/api/auth/token/${id}`,
    method: 'get'
  })
}

// 根据API ID获取相关的授权令牌
export const getTokensByApiId = (apiId) => {
  return request({
    url: `/api/auth/tokens/api/${apiId}`,
    method: 'get'
  })
}

// 获取所有授权令牌
export const getAllTokens = () => {
  return request({
    url: '/api/auth/tokens',
    method: 'get'
  })
}

// 删除授权令牌
export const deleteToken = (id) => {
  return request({
    url: `/api/auth/token/${id}`,
    method: 'delete'
  })
}

// 切换API密钥状态
export const toggleTokenStatus = (id) => {
  return request({
    url: `/api/auth/token/toggle-status/${id}`,
    method: 'put'
  })
}