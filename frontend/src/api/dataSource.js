import request from '@/utils/request.js'

// 获取数据库类型
export const getDatabaseTypes = () => {
  return request({
    url: '/api/datasource/types',
    method: 'get'
  })
}

// 添加数据源
export const addDataSource = (config) => {
  return request({
    url: '/api/datasource/add',
    method: 'post',
    data: config
  })
}

// 获取所有数据源
export const getAllDataSources = () => {
  return request({
    url: '/api/datasource/list',
    method: 'get'
  })
}

// 测试数据源连接
export const testConnection = (id) => {
  return request({
    url: `/api/datasource/test/${id}`,
    method: 'post'
  })
}

// 移除数据源
export const removeDataSource = (id) => {
  return request({
    url: `/api/datasource/remove/${id}`,
    method: 'delete'
  })
}

// 执行查询SQL
export const executeQuery = (dataSourceId, requestData) => {
  return request({
    url: `/api/datasource/query/${dataSourceId}`,
    method: 'post',
    data: requestData
  })
}

// 执行更新SQL
export const executeUpdate = (dataSourceId, requestData) => {
  return request({
    url: `/api/datasource/update/${dataSourceId}`,
    method: 'post',
    data: requestData
  })
}

// 获取表结构信息
export const getTableInfo = (dataSourceId, tableName) => {
  return request({
    url: `/api/datasource/table/${dataSourceId}/${tableName}`,
    method: 'get'
  })
}

// 获取数据库元数据
export const getDatabaseMetadata = (dataSourceId) => {
  return request({
    url: `/api/datasource/metadata/${dataSourceId}`,
    method: 'get'
  })
}