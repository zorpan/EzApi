<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2>个人中心</h2>
    </div>

    <div class="profile-content">
      <div class="profile-card">
        <h3>基础信息</h3>
        <form class="form" @submit.prevent="updateProfile">
          <div class="form-row">
            <div class="form-group" style="flex: 1;margin: 10px;">
              <label>用户名 *</label>
              <input type="text" v-model="profile.username" placeholder="用户名" disabled>
            </div>
            <div class="form-group" style="flex: 1;margin: 10px;">
              <label>邮箱 *</label>
              <input type="email" v-model="profile.email" placeholder="请输入邮箱">
            </div>
          </div>
          <div class="form-row" style="display: none">
            <div class="form-group" style="flex: 1; margin-left: 15px;">
              <label>角色</label>
              <input type="text" v-model="profile.role" placeholder="角色" disabled>
            </div>
            <div class="form-group" style="flex: 1; margin-left: 15px;">
              <label>状态</label>
              <input type="text" v-model="profile.status" placeholder="状态" disabled>
            </div>
          </div>
          <div class="form-group" style="margin: 10px;">
            <label>描述</label>
            <input type="text" v-model="profile.description" placeholder="请输入描述信息">
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">更新基础信息</button>
          </div>
        </form>
      </div>

      <div class="profile-card">
        <h3>修改密码</h3>
        <form class="form" @submit.prevent="changePassword">
          <div class="form-group">
            <label>当前密码 *</label>
            <input type="password" v-model="passwordForm.currentPassword" placeholder="请输入当前密码">
          </div>
          <div class="form-row">
            <div class="form-group" style="flex: 1;">
              <label>新密码 *</label>
              <input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码">
            </div>
            <div class="form-group" style="flex: 1; margin-left: 15px;">
              <label>确认新密码 *</label>
              <input type="password" v-model="passwordForm.confirmNewPassword" placeholder="请再次输入新密码">
            </div>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">修改密码</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentUser, login } from '@/utils/auth'
import axios from 'axios'

const profile = ref({
  id: null,
  username: '',
  role: '',
  status: '',
  email: '',
  description: ''
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmNewPassword: ''
})

onMounted(() => {
  loadUserProfile()
})

const loadUserProfile = () => {
  const user = getCurrentUser()
  if (user) {
    profile.value = {
      id: user.id,
      username: user.username,
      role: user.role,
      status: user.status,
      email: user.email || '',
      description: user.description || ''
    }
  }
}

const updateProfile = async () => {
  if (!profile.value.email) {
    alert('请输入邮箱地址')
    return
  }

  try {
    const userData = {
      ...profile.value,
      password: undefined // 不包含密码字段
    }
    
    const response = await axios.put('/api/auth/user/update', userData)
    if (response.data.success) {
      alert('基础信息更新成功')
      // 更新本地存储的用户信息
      const currentUser = getCurrentUser()
      if (currentUser) {
        currentUser.email = profile.value.email
        currentUser.description = profile.value.description
        login(currentUser) // 重新登录以更新本地存储
      }
    } else {
      alert('更新失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('更新基础信息失败:', error)
    alert('更新基础信息失败: ' + (error.response?.data?.message || error.message))
  }
}

const changePassword = async () => {
  if (!passwordForm.value.currentPassword) {
    alert('请输入当前密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    alert('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmNewPassword) {
    alert('两次输入的新密码不一致')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    alert('新密码长度不能少于6位')
    return
  }

  try {
    const passwordData = {
      username: profile.value.username,
      oldPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    }
    
    const response = await axios.post('/api/auth/user/change-password', passwordData)
    if (response.data.success) {
      alert('密码修改成功，请重新登录')
      // 清除当前登录状态
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('currentUser')
      // 重置表单
      passwordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: ''
      }
      // 这里应该跳转到登录页面，但现在只是提示
    } else {
      alert('修改密码失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    alert('修改密码失败: ' + (error.response?.data?.message || error.message))
  }
}
</script>

<style scoped>
.profile-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.profile-header {
  margin-bottom: 20px;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.profile-card h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 10px;
}

.form {
  padding: 0;
}

.form-row {
  display: flex;
}

.form-group {
  margin-bottom: 15px;
  flex: 1;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #606266;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  background-color: #fff;
}

.form-group input:disabled {
  background-color: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

.form-actions {
  text-align: right;
  margin-top: 20px;
}

.btn {
  padding: 6px 12px;
  margin-right: 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  background-color: #409eff;
  color: white;
}

.btn-success {
  background-color: #67c23a;
  color: white;
}

.btn-info {
  background-color: #909399;
  color: white;
  padding: 4px 8px;
  font-size: 12px;
  margin-right: 4px;
}

.btn-warning {
  background-color: #e6a23c;
  color: white;
  padding: 4px 8px;
  font-size: 12px;
  margin-right: 4px;
}

.btn-danger {
  background-color: #f56c6c;
  color: white;
  padding: 4px 8px;
  font-size: 12px;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
}

.status-active {
  color: #67c23a;
}

.status-inactive {
  color: #909399;
}
</style>