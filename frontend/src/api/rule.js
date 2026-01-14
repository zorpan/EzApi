import request from '@/utils/request.js'

// 创建授权规则
export const createRule = (ruleData) => {
  return request({
    url: '/api/auth/rule/create',
    method: 'post',
    data: ruleData
  })
}

// 更新授权规则
export const updateRule = (ruleData) => {
  return request({
    url: '/api/auth/rule/update',
    method: 'put',
    data: ruleData
  })
}

// 获取授权规则详情
export const getRule = (id) => {
  return request({
    url: `/api/auth/rule/${id}`,
    method: 'get'
  })
}

// 获取所有授权规则
export const getAllRules = () => {
  return request({
    url: '/api/auth/rules',
    method: 'get'
  })
}

// 删除授权规则
export const deleteRule = (id) => {
  return request({
    url: `/api/auth/rule/${id}`,
    method: 'delete'
  })
}