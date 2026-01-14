<template>
  <div class="datasource-list-container">
    <div class="toolbar">
      <button class="btn btn-primary" @click="openAddDialog">新增数据源</button>
      <button class="btn btn-success" @click="loadDataSources">刷新</button>
    </div>

    <vxe-table
      :data="dataSources"
      :loading="loading"
      height="600"
      stripe
      border
      resizable
    >
      <vxe-column type="seq" width="60" title="序号"></vxe-column>
      <vxe-column field="name" title="名称" width="150"></vxe-column>
      <vxe-column field="dbType" title="数据库类型" width="120"></vxe-column>
      <vxe-column field="url" title="连接URL" width="300" show-overflow></vxe-column>
      <vxe-column field="username" title="用户名" width="120"></vxe-column>
      <vxe-column field="driverClassName" title="驱动类" width="200"></vxe-column>
      <vxe-column field="enabled" title="状态" width="80">
        <template #default="{ row }">
          <span :class="row.enabled ? 'status-active' : 'status-inactive'">
            {{ row.enabled ? '启用' : '禁用' }}
          </span>
        </template>
      </vxe-column>
      <vxe-column title="操作" width="200">
        <template #default="{ row }">
          <button class="btn btn-sm btn-info" @click="testConnection(row)">测试连接</button>
          <button class="btn btn-sm btn-warning" @click="editDataSource(row)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="deleteDataSource(row)">删除</button>
        </template>
      </vxe-column>
    </vxe-table>

    <!-- 新增/编辑对话框 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="showModal" @click="showModal = false">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3>{{ currentDataSource.id ? '编辑数据源' : '新增数据源' }}</h3>
            <button class="modal-close" @click="showModal = false">&times;</button>
          </div>
          <div class="modal-body">
            <form class="form">
              <div class="form-group">
                <label>名称:</label>
                <input type="text" v-model="currentDataSource.name" placeholder="数据源名称">
              </div>
              <div class="form-group">
                <label>数据库类型:</label>
                <select v-model="currentDataSource.dbType" @change="onDbTypeChange" placeholder="选择数据库类型">
                  <option value="">请选择数据库类型</option>
                  <option v-for="dbType in databaseTypes" :key="dbType.type" :value="dbType.type">
                    {{ dbType.type }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label>驱动类:</label>
                <input type="text" v-model="currentDataSource.driverClassName" placeholder="驱动类名">
              </div>
              <div class="form-group">
                <label>连接URL:</label>
                <input type="text" v-model="currentDataSource.url" placeholder="数据库连接地址">
              </div>
              <div class="form-group">
                <label>用户名:</label>
                <input type="text" v-model="currentDataSource.username" placeholder="数据库用户名">
              </div>
              <div class="form-group">
                <label>密码:</label>
                <input type="password" v-model="currentDataSource.password" placeholder="数据库密码">
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button class="btn btn-primary" @click="saveDataSource">保存</button>
            <button class="btn btn-secondary" @click="showModal = false">取消</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const dataSources = ref([])
const loading = ref(false)
const showModal = ref(false)
const databaseTypes = ref([]) // 数据库类型列表
const currentDataSource = ref({
  id: '',
  name: '',
  driverClassName: '',
  url: '',
  username: '',
  password: '',
  dbType: '',
  enabled: true
})

onMounted(() => {
  loadDataSources()
  loadDatabaseTypes()
})

const loadDatabaseTypes = async () => {
  try {
    const response = await axios.get('/api/datasource/types')
    databaseTypes.value = response.data
  } catch (error) {
    console.error('加载数据库类型失败:', error)
  }
}

const loadDataSources = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/datasource/list')
    dataSources.value = response.data
  } catch (error) {
    console.error('加载数据源失败:', error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  currentDataSource.value = {
    id: '',
    name: '',
    driverClassName: '',
    url: '',
    username: '',
    password: '',
    dbType: '',
    enabled: true
  }
  showModal.value = true
}

const editDataSource = (row) => {
  currentDataSource.value = { ...row }
  showModal.value = true
}

const onDbTypeChange = () => {
  if (currentDataSource.value.dbType) {
    const selectedDbType = databaseTypes.value.find(type => type.type === currentDataSource.value.dbType)
    if (selectedDbType) {
      currentDataSource.value.driverClassName = selectedDbType.driverClass
      // 使用默认模板，实际应用中可能需要用户输入host、port、database等参数
      currentDataSource.value.url = selectedDbType.connectionTemplate
    }
  }
}

const saveDataSource = async () => {
  try {
    await axios.post('/api/datasource/add', currentDataSource.value)
    showModal.value = false
    await loadDataSources()
  } catch (error) {
    console.error('保存数据源失败:', error)
  }
}

const deleteDataSource = async (row) => {
  if (confirm(`确定要删除数据源 "${row.name}" 吗？`)) {
    try {
      await axios.delete(`/api/datasource/remove/${row.id}`)
      await loadDataSources()
    } catch (error) {
      console.error('删除数据源失败:', error)
    }
  }
}

const testConnection = async (row) => {
  try {
    const response = await axios.post(`/api/datasource/test/${row.id}`)
    alert(response.data)
  } catch (error) {
    console.error('测试连接失败:', error)
    alert(error.response?.data || '连接测试失败')
  }
}
</script>

<style scoped>
.datasource-list-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.toolbar {
  margin-bottom: 20px;
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

.form {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group select,
.form-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group input[type="checkbox"] {
  width: auto;
}

.form-actions {
  text-align: right;
  margin-top: 20px;
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
  width: 600px;
  max-width: 90vw;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  overflow: hidden;
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
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>