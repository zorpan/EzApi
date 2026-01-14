<template>
  <div class="user-management-container">
    <div class="toolbar">
      <button class="btn btn-primary" @click="openAddEditDialog()">新增用户</button>
      <button class="btn btn-success" @click="loadUsers">刷新</button>
    </div>

    <vxe-table
      :data="users"
      :loading="loading"
      height="600"
      stripe
      border
      resizable
    >
      <vxe-column type="seq" width="60" title="序号"></vxe-column>
      <vxe-column field="id" title="ID" width="80"></vxe-column>
      <vxe-column field="username" title="用户名" width="150"></vxe-column>
      <vxe-column field="role" title="角色" width="120">
        <template #default="{ row }">
          <span :class="'status-' + getUserRoleClass(row.role)">
            {{ row.role }}
          </span>
        </template>
      </vxe-column>
      <vxe-column field="status" title="状态" width="100">
        <template #default="{ row }">
          <span :class="row.status === 'ACTIVE' ? 'status-active' : 'status-inactive'">
            {{ row.status }}
          </span>
        </template>
      </vxe-column>
      <vxe-column field="email" title="邮箱" width="200"></vxe-column>
      <vxe-column field="createTime" title="创建时间" width="180"></vxe-column>
      <vxe-column title="操作" width="200">
        <template #default="{ row }">
          <button class="btn btn-sm btn-info" @click="viewUserDetails(row)">查看</button>
          <button class="btn btn-sm btn-warning" @click="openAddEditDialog(row)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="deleteUser(row)">删除</button>
        </template>
      </vxe-column>
    </vxe-table>

    <!-- 用户详情对话框 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="showUserDialog" @click="closeUserDialog">
        <div class="modal-dialog modal-xl" @click.stop>
          <div class="modal-header">
            <h3>{{ currentUser.id ? '编辑用户' : '新增用户' }}</h3>
            <button class="modal-close" @click="closeUserDialog">&times;</button>
          </div>
          <div class="modal-body">
            <form class="form">
              <div class="form-row">
                <div class="form-group" style="flex: 1;">
                  <label>用户名 *</label>
                  <input type="text" v-model="currentUser.username" placeholder="请输入用户名">
                </div>
                <div class="form-group" style="flex: 1; margin-left: 15px;" v-if="!currentUser.id">
                  <label>密码 *</label>
                  <input type="password" v-model="currentUser.password" placeholder="请输入密码">
                </div>
                <div class="form-group" style="flex: 1; margin-left: 15px;">
                  <label>角色 *</label>
                  <select v-model="currentUser.role">
                    <option value="ADMIN">ADMIN</option>
                    <option value="MANAGER">MANAGER</option>
                    <option value="USER">USER</option>
                    <option value="GUEST">GUEST</option>
                  </select>
                </div>
                <div class="form-group" style="flex: 1; margin-left: 15px;">
                  <label>状态 *</label>
                  <select v-model="currentUser.status">
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="INACTIVE">INACTIVE</option>
                    <option value="LOCKED">LOCKED</option>
                  </select>
                </div>
              </div>
              <div class="form-row">
                <div class="form-group" style="flex: 1;">
                  <label>邮箱</label>
                  <input type="email" v-model="currentUser.email" placeholder="请输入邮箱">
                </div>
                <div class="form-group" style="flex: 1; margin-left: 15px;">
                  <label>描述</label>
                  <input type="text" v-model="currentUser.description" placeholder="请输入描述信息">
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button class="btn btn-primary" @click="saveUser">保存</button>
            <button class="btn btn-secondary" @click="closeUserDialog">取消</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 用户详情对话框 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="userDetailDialogVisible" @click="closeUserDetailDialog">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3>用户详情</h3>
            <button class="modal-close" @click="closeUserDetailDialog">&times;</button>
          </div>
          <div class="modal-body" v-if="selectedUser">
            <div class="detail-item">
              <label>ID:</label>
              <span>{{ selectedUser.id }}</span>
            </div>
            <div class="detail-item">
              <label>用户名:</label>
              <span>{{ selectedUser.username }}</span>
            </div>
            <div class="detail-item">
              <label>角色:</label>
              <span :class="'status-' + getUserRoleClass(selectedUser.role)">
                {{ selectedUser.role }}
              </span>
            </div>
            <div class="detail-item">
              <label>状态:</label>
              <span :class="selectedUser.status === 'ACTIVE' ? 'status-active' : 'status-inactive'">
                {{ selectedUser.status }}
              </span>
            </div>
            <div class="detail-item">
              <label>邮箱:</label>
              <span>{{ selectedUser.email || '无' }}</span>
            </div>
            <div class="detail-item">
              <label>创建时间:</label>
              <span>{{ selectedUser.createTime }}</span>
            </div>
            <div class="detail-item">
              <label>更新时间:</label>
              <span>{{ selectedUser.updateTime }}</span>
            </div>
            <div class="detail-item">
              <label>描述:</label>
              <span>{{ selectedUser.description }}</span>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeUserDetailDialog">关闭</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const users = ref([])
const loading = ref(false)
const showUserDialog = ref(false)
const userDetailDialogVisible = ref(false)
const currentUser = ref({
  id: null,
  username: '',
  role: 'USER',
  status: 'ACTIVE',
  email: '',
  description: ''
})
const selectedUser = ref({})

onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/auth/users') // 获取所有用户
    users.value = response.data.data || []
  } catch (error) {
    console.error('加载用户失败:', error)
    alert('加载用户失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const openAddEditDialog = (user = null) => {
  if (user) {
    // 深拷贝用户对象，避免直接修改原对象
    currentUser.value = { ...user }
    // 编辑时不显示密码字段
    delete currentUser.value.password
  } else {
    currentUser.value = {
      id: null,
      username: '',
      role: 'USER',
      status: 'ACTIVE',
      email: '',
      description: ''
    }
  }
  showUserDialog.value = true
}

const closeUserDialog = () => {
  showUserDialog.value = false
}

const closeUserDetailDialog = () => {
  userDetailDialogVisible.value = false
}

const saveUser = async () => {
  try {
    let response
    if (currentUser.value.id) {
      // 更新现有用户 - 后端可能没有直接的更新API，我们使用创建接口
      response = await axios.post('/api/auth/user/create', currentUser.value)
    } else {
      // 创建新用户
      response = await axios.post('/api/auth/user/create', currentUser.value)
    }
    
    if (response.data.success) {
      alert(currentUser.value.id ? '更新用户成功' : '创建用户成功')
      closeUserDialog()
      await loadUsers()
    } else {
      alert('操作失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('保存用户失败:', error)
    alert('保存用户失败: ' + (error.response?.data?.message || error.message))
  }
}

const deleteUser = async (user) => {
  if (confirm(`确定要删除用户 "${user.username}" 吗？`)) {
    try {
      const response = await axios.delete(`/api/auth/user/${user.id}`)
      if (response.data.success) {
        alert('删除用户成功')
        await loadUsers()
      } else {
        alert('删除失败: ' + response.data.message)
      }
    } catch (error) {
      console.error('删除用户失败:', error)
      alert('删除用户失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

const viewUserDetails = (user) => {
  selectedUser.value = user
  userDetailDialogVisible.value = true
}

const getUserRoleClass = (role) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'MANAGER': return 'warning'
    case 'USER': return 'primary'
    case 'GUEST': return 'info'
    default: return 'info'
  }
}
</script>

<style scoped>
.user-management-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.toolbar {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.detail-item label {
  font-weight: bold;
  width: 120px;
  flex-shrink: 0;
}

.detail-item span {
  flex: 1;
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

.btn-secondary {
  background-color: #909399;
  color: white;
}

.status-active {
  color: #67c23a;
}

.status-inactive {
  color: #909399;
}

.status-primary {
  color: #409eff;
}

.status-success {
  color: #67c23a;
}

.status-warning {
  color: #e6a23c;
}

.status-danger {
  color: #f56c6c;
}

.status-info {
  color: #909399;
}

.form {
  padding: 20px;
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
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-textarea {
  font-family: monospace;
  resize: vertical;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-dialog {
  background: white;
  border-radius: 8px;
  width: 1000px;
  max-width: 95vw;
  max-height: 90vh;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-dialog.modal-xl {
  width: 1200px;
  max-width: 95vw;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fafafa;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #909399;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  color: #606266;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  background-color: #fafafa;
}
</style>