import request from '@/utils/request.js'

// 获取所有用户
export const getAllUsers = () => {
  return request({
    url: '/sys/user/users',
    method: 'get'
  })
}

// 创建用户
export const createUser = (userData) => {
  return request({
    url: '/sys/user/user/create',
    method: 'post',
    data: userData
  })
}

// 更新用户
export const updateUser = (userData) => {
  return request({
    url: '/sys/user/user/update',
    method: 'put',
    data: userData
  })
}

// 删除用户
export const deleteUser = (userId) => {
  return request({
    url: `/sys/user/user/${userId}`,
    method: 'delete'
  })
}

// 修改密码
export const changePassword = (passwordData) => {
  return request({
    url: '/sys/user/user/change-password',
    method: 'post',
    data: passwordData
  })
}