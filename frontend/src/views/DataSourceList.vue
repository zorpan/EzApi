<template>
  <div class="datasource-list-container">
    <div class="toolbar">
      <button class="btn btn-primary" @click="openAddDialog()">新增数据源</button>
      <button class="btn btn-success" @click="loadDataSources()">刷新</button>
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
      <vxe-column field="id" title="ID" width="120"></vxe-column>
      <vxe-column field="name" title="数据源名称" width="200"></vxe-column>
      <vxe-column field="driverClassName" title="驱动类" width="200"></vxe-column>
      <vxe-column field="url" title="连接URL" width="300" show-overflow></vxe-column>
      <vxe-column field="dbType" title="数据库类型" width="120"></vxe-column>
      <vxe-column field="enabled" title="状态" width="100">
        <template #default="{ row }">
          <span :class="row.enabled ? 'status-active' : 'status-inactive'">
            {{ row.enabled ? '启用' : '禁用' }}
          </span>
        </template>
      </vxe-column>
      <vxe-column title="操作" width="250">
        <template #default="{ row }">
          <button class="btn btn-sm btn-info" @click="testConnection(row)">测试</button>
          <button class="btn btn-sm btn-warning" @click="editDataSource(row)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="removeDataSource(row)">删除</button>
          <button 
            :class="['btn', 'btn-sm', row.enabled ? 'btn-inactive' : 'btn-active']"
            @click="toggleDataSourceStatus(row)"
          >
            {{ row.enabled ? '禁用' : '启用' }}
          </button>
        </template>
      </vxe-column>
    </vxe-table>

    <!-- 数据源配置对话框 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="showModal" @click="closeModal">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3>{{ currentDataSource.id ? '编辑数据源' : '新增数据源' }}</h3>
            <button class="modal-close" @click="closeModal">&times;</button>
          </div>
          <div class="modal-body">
            <el-form 
              ref="dataSourceFormRef"
              :model="currentDataSource" 
              :rules="dataSourceRules"
              label-position="top"
              @submit.prevent
            >
              <el-form-item label="数据源名称 *" prop="name">
                <el-input 
                  v-model="currentDataSource.name" 
                  placeholder="请输入数据源名称"
                />
              </el-form-item>
              
              <el-form-item label="数据库类型 *" prop="dbType">
                <el-select 
                  v-model="currentDataSource.dbType" 
                  @change="onDbTypeChange" 
                  placeholder="请选择数据库类型"
                  style="width: 100%"
                >
                  <el-option 
                    v-for="dbType in databaseTypes" 
                    :key="dbType.type" 
                    :label="dbType.type" 
                    :value="dbType.type"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="驱动类名 *" prop="driverClassName">
                <!-- 仅当数据库类型为 'other' 时显示驱动选择器 -->
                <el-select 
                  v-if="currentDataSource.dbType === 'other'"
                  v-model="currentDataSource.driverId"
                  placeholder="请选择驱动"
                  @change="onDriverChange"
                  style="width: 100%"
                >
                  <el-option 
                    v-for="driver in availableDrivers" 
                    :key="driver.id" 
                    :label="`${driver.driverName} (${driver.driverClassName})`" 
                    :value="driver.id"
                  />
                </el-select>
                
                <!-- 当数据库类型不为 'other' 时显示输入框 -->
                <el-input 
                  v-else
                  v-model="currentDataSource.driverClassName" 
                  placeholder="例如: com.mysql.cj.jdbc.Driver"
                />
                
                <!-- 当没有可用驱动时的提示 -->
                <div v-if="currentDataSource.dbType === 'other' && availableDrivers.length === 0" class="driver-hint">
                  没有可用的自定义驱动，请先在驱动管理页面上传驱动文件。
                </div>
              </el-form-item>
              
              <el-form-item label="连接URL *" prop="url">
                <el-input 
                  v-model="currentDataSource.url" 
                  placeholder="例如: jdbc:mysql://localhost:3306/database"
                />
              </el-form-item>
              
              <el-form-item label="用户名 *" prop="username">
                <el-input 
                  v-model="currentDataSource.username" 
                  placeholder="数据库用户名"
                  autocomplete="off"
                  :autocorrect="off"
                  :spellcheck="false"
                />
              </el-form-item>
              
              <el-form-item label="密码" prop="password">
                <el-input 
                  v-model="currentDataSource.password" 
                  type="password"
                  placeholder="数据库密码"
                  autocomplete="new-password"
                  :autocorrect="off"
                  :spellcheck="false"
                />
              </el-form-item>
            </el-form>
          </div>
          <div class="modal-footer">
            <el-button class="btn btn-primary" @click="saveDataSource">保存</el-button>
            <el-button class="btn btn-secondary" @click="closeModal">取消</el-button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dataSourceApi } from '../api'
import { driverApi } from '../api' // 添加驱动API导入
import { ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElButton } from 'element-plus'

const dataSources = ref([])
const loading = ref(false)
const showModal = ref(false)
const submitted = ref(false)
const databaseTypes = ref([]) // 数据库类型列表
const availableDrivers = ref([]) // 可用驱动列表
const showDriverSelector = ref(false) // 是否显示驱动选择器
const currentDataSource = ref({
  id: '',
  name: '',
  driverClassName: '',
  url: '',
  username: '',
  password: '',
  dbType: '',
  enabled: true,
  driverId:''
})

// 表单验证规则
const dataSourceRules = {
  name: [
    { required: true, message: '请输入数据源名称', trigger: 'blur' }
  ],
  dbType: [
    { required: true, message: '请选择数据库类型', trigger: 'change' }
  ],
  driverId: [
    { 
      validator: (rule, value, callback) => {
        if (currentDataSource.value.dbType === 'other') {
          // 如果是other类型，必须从下拉框选择驱动
          if (!value || value.trim() === '') {
            callback(new Error('请选择驱动'))
          } else {
            callback()
          }
        } else {
          // 如果不是other类型，必须填写驱动类名
          if (!value || value.trim() === '') {
            callback(new Error('请输入驱动类名'))
          } else {
            callback()
          }
        }
      },
      trigger: 'blur change'
    }
  ],
  url: [
    { required: true, message: '请输入连接URL', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ]
}

const dataSourceFormRef = ref(null)

onMounted(() => {
  loadDataSources()
  loadDatabaseTypes()
})

const loadDatabaseTypes = async () => {
  try {
    const response = await dataSourceApi.getDatabaseTypes()
    // 适配新的Result响应结构
    databaseTypes.value = response.data.data || []
  } catch (error) {
    console.error('加载数据库类型失败:', error)
  }
}

// 加载可用的驱动列表
const loadAvailableDrivers = async () => {
  try {
    const response = await driverApi.getAllDrivers()
    debugger
    // 仅加载激活状态的驱动
    availableDrivers.value = (response.data.data || []).filter(driver => driver.isActive)
  } catch (error) {
    console.error('加载可用驱动失败:', error)
    availableDrivers.value = [] // 设置为空数组
  }
}

const loadDataSources = async () => {
  loading.value = true
  try {
    const response = await dataSourceApi.getAllDataSources()
    // 适配新的Result响应结构
    dataSources.value = response.data.data || []
  } catch (error) {
    console.error('加载数据源失败:', error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = async () => {
  currentDataSource.value = {
    name: '',
    driverClassName: '',
    url: '',
    username: '',
    password: '',
    dbType: '',
    enabled: true
  }
  submitted.value = false
  showModal.value = true
  // 清除表单验证
  if (dataSourceFormRef.value) {
    dataSourceFormRef.value.clearValidate()
  }
}

const editDataSource = async (row) => {
  currentDataSource.value = { ...row }
  // 密码不应在编辑时显示，除非需要更新
  currentDataSource.value.password = '' // 清空密码字段以避免暴露
  submitted.value = false
  showModal.value = true
  
  // 如果编辑的是 'other' 类型的数据源，加载驱动列表
  if (row.dbType === 'other') {
    await loadAvailableDrivers()
  }
  
  // 清除表单验证
  if (dataSourceFormRef.value) {
    dataSourceFormRef.value.clearValidate()
  }
}

const onDbTypeChange = async () => {
  if (currentDataSource.value.dbType) {
    if (currentDataSource.value.dbType === 'other') {
      // 如果选择了 'other' 类型，加载可用驱动
      await loadAvailableDrivers()
    }
    
    const selectedDbType = databaseTypes.value.find(type => type.type === currentDataSource.value.dbType)
    if (selectedDbType && currentDataSource.value.dbType !== 'other') {
      currentDataSource.value.driverClassName = selectedDbType.driverClass
      // 使用默认模板，实际应用中可能需要用户输入host、port、database等参数
      currentDataSource.value.url = selectedDbType.connectionTemplate
    }
  }
}

// 当选择驱动时更新URL
const onDriverChange = () => {
  if (currentDataSource.value.dbType === 'other' && currentDataSource.value.driverId) {
    // 查找所选驱动类名对应的驱动
    const selectedDriver = availableDrivers.value.find(driver => 
      driver.id === currentDataSource.value.driverId
    )
    
    if (selectedDriver && selectedDriver.exampleJdbcUrl) {
      // 将驱动的示例JDBC URL填充到连接URL字段
      currentDataSource.value.url = selectedDriver.exampleJdbcUrl
      currentDataSource.value.driverClassName = selectedDriver.driverClassName
    }
  }
}

const closeModal = () => {
  showModal.value = false
}

const validateForm = () => {
  if (!currentDataSource.value.name) {
    return false;
  }
  if (!currentDataSource.value.dbType) {
    return false;
  }
  if (!currentDataSource.value.driverClassName) {
    return false;
  }
  if (!currentDataSource.value.url) {
    return false;
  }
  if (!currentDataSource.value.username) {
    return false;
  }
  return true;
};

const saveDataSource = async () => {
  if (!dataSourceFormRef.value) return
  
  // 使用 Element Plus 的表单验证
  const valid = await dataSourceFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  
  try {
    let response = await dataSourceApi.saveDataSource(currentDataSource.value)
    // 适配新的Result响应结构
    if (response.data.code === 200) {
      alert(currentDataSource.value.id ? '更新数据源成功' : '新增数据源成功')
      closeModal()
      await loadDataSources()
    } else {
      alert('操作失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('保存数据源失败:', error)
    alert('保存数据源失败: ' + (error.response?.data?.message || error.message))
  } finally {
    // 清除表单验证
    if (dataSourceFormRef.value) {
      dataSourceFormRef.value.clearValidate()
    }
  }
}

const testConnection = async (row) => {
  try {
    const response = await dataSourceApi.testConnection(row.id)
    if (response.data.code === 200) {
      alert('连接测试成功: ' + response.data.data)
    } else {
      alert('连接测试失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('测试连接失败:', error)
    alert('测试连接失败: ' + (error.response?.data?.message || error.message))
  }
}

const removeDataSource = async (row) => {
  if (confirm(`确定要删除数据源 "${row.name}" 吗？`)) {
    try {
      const response = await dataSourceApi.removeDataSource(row.id)
      if (response.data.code === 200) {
        alert('删除数据源成功')
        await loadDataSources()
      } else {
        alert('删除失败: ' + response.data.message)
      }
    } catch (error) {
      console.error('删除数据源失败:', error)
      alert('删除数据源失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

const toggleDataSourceStatus = (row) => {
  // 这里只是临时切换状态，实际的启用/禁用逻辑需要后端支持
  alert('切换状态功能需要后端API支持')
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

.form-group input.error-input,
.form-group select.error-input {
  border-color: #f56c6c; /* 红色边框表示错误 */
  background-color: #fef0f0; /* 淡红色背景 */
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
  width: 800px;
  max-width: 95vw;
  max-height: 90vh;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  display: flex;
  flex-direction: column;
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

/* Element Plus 表单特殊样式 */
.modal-body :deep(.el-form-item__label) {
  font-weight: bold;
  color: #333;
}

.modal-body :deep(.el-input__wrapper),
.modal-body :deep(.el-select__wrapper) {
  border-radius: 4px;
}

.driver-hint {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  font-size: 14px;
  color: #909399;
}
</style>