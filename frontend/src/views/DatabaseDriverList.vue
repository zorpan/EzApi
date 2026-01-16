<template>
  <div class="driver-list-container">
    <div class="toolbar">
      <button class="btn btn-primary" @click="openAddDialog()">新增驱动</button>
      <button class="btn btn-success" @click="loadDrivers()">刷新</button>
    </div>

    <vxe-table
      :data="drivers"
      :loading="loading"
      height="600"
      stripe
      border
      resizable
    >
      <vxe-column type="seq" width="60" title="序号"></vxe-column>
      <vxe-column field="driverName" title="驱动名称" width="200"></vxe-column>
      <vxe-column field="driverClassName" title="驱动类名" width="250"></vxe-column>
      <vxe-column field="exampleJdbcUrl" title="示例JDBC URL" width="300" show-overflow></vxe-column>
      <vxe-column field="isActive" title="状态" width="100">
        <template #default="{ row }">
          <span :class="row.isActive ? 'status-active' : 'status-inactive'">
            {{ row.isActive ? '启用' : '禁用' }}
          </span>
        </template>
      </vxe-column>
      <vxe-column field="driverFileName" title="驱动文件" width="200"></vxe-column>
      <vxe-column title="操作" width="300">
        <template #default="{ row }">
          <button class="btn btn-sm btn-info" @click="downloadDriver(row)">下载</button>
          <button class="btn btn-sm btn-warning" @click="editDriver(row)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="removeDriver(row)">删除</button>
          <button 
            :class="['btn', 'btn-sm', row.isActive ? 'btn-inactive' : 'btn-active']"
            @click="toggleDriverStatus(row)"
          >
            {{ row.isActive ? '禁用' : '启用' }}
          </button>
        </template>
      </vxe-column>
    </vxe-table>

    <!-- 驱动配置对话框 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="showModal" @click="closeModal">
        <div class="modal-dialog" @click.stop style="width: 800px;">
          <div class="modal-header">
            <h3>{{ currentDriver.id ? '编辑驱动' : '新增驱动' }}</h3>
            <button class="modal-close" @click="closeModal">&times;</button>
          </div>
          <div class="modal-body">
            <el-form 
              ref="driverFormRef"
              :model="currentDriver" 
              :rules="driverRules"
              label-position="top"
              @submit.prevent
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="驱动名称 *" prop="driverName">
                    <el-input 
                      v-model="currentDriver.driverName" 
                      placeholder="请输入驱动名称"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="驱动类名 *" prop="driverClassName">
                <el-input 
                  v-model="currentDriver.driverClassName" 
                  placeholder="例如: com.mysql.cj.jdbc.Driver"
                />
              </el-form-item>
              
              <el-form-item label="示例JDBC URL *" prop="exampleJdbcUrl">
                <el-input 
                  v-model="currentDriver.exampleJdbcUrl" 
                  placeholder="例如: jdbc:mysql://localhost:3306/database"
                />
              </el-form-item>
              
              <el-form-item label="驱动描述" prop="driverDescription">
                <el-input 
                  v-model="currentDriver.driverDescription" 
                  type="textarea"
                  :rows="3"
                  placeholder="请输入驱动描述"
                />
              </el-form-item>
              
              <el-form-item label="驱动文件 *" prop="file">
                <input 
                  type="file" 
                  @change="handleFileUpload"
                  accept=".jar,.zip,.rar"
                  ref="fileInputRef"
                />
                <div v-if="currentDriver.driverFileName" class="file-info">
                  当前文件: {{ currentDriver.driverFileName }}
                </div>
              </el-form-item>
            </el-form>
          </div>
          <div class="modal-footer">
            <el-button class="btn btn-primary" @click="saveDriver">保存</el-button>
            <el-button class="btn btn-secondary" @click="closeModal">取消</el-button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElForm, ElFormItem, ElInput, ElButton, ElRow, ElCol } from 'element-plus'
import { driverApi } from '../api'

const drivers = ref([])
const loading = ref(false)
const showModal = ref(false)
const submitted = ref(false)
const currentDriver = ref({
  id: '',
  driverName: '',
  driverVersion: '',
  driverDescription: '',
  driverFileName: '',
  driverFilePath: '',
  driverClassName: '',
  exampleJdbcUrl: '',
  supportedDbTypes: '',
  isActive: true,
  createdTime: '',
  updatedTime: ''
})

// 用于文件上传
const fileInputRef = ref(null)
const uploadedFile = ref(null)

// 表单验证规则
const driverRules = {
  driverName: [
    { required: true, message: '请输入驱动名称', trigger: 'blur' }
  ],
  driverClassName: [
    { required: true, message: '请输入驱动类名', trigger: 'blur' }
  ],
  exampleJdbcUrl: [
    { required: true, message: '请输入示例JDBC URL', trigger: 'blur' }
  ]
}

const driverFormRef = ref(null)

onMounted(() => {
  loadDrivers()
})

const loadDrivers = async () => {
  loading.value = true
  try {
    const response = await driverApi.getAllDrivers()
    // 适配新的Result响应结构
    drivers.value = response.data.data || []
  } catch (error) {
    console.error('加载驱动失败:', error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  currentDriver.value = {
    id: '',
    driverName: '',
    driverVersion: '',
    driverDescription: '',
    driverFileName: '',
    driverFilePath: '',
    driverClassName: '',
    exampleJdbcUrl: '',
    supportedDbTypes: '',
    isActive: true,
    createdTime: '',
    updatedTime: ''
  }
  uploadedFile.value = null
  submitted.value = false
  showModal.value = true
  // 清除表单验证
  if (driverFormRef.value) {
    driverFormRef.value.clearValidate()
  }
  // 清除文件输入
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const editDriver = (row) => {
  currentDriver.value = { ...row }
  uploadedFile.value = null
  submitted.value = false
  showModal.value = true
  // 清除表单验证
  if (driverFormRef.value) {
    driverFormRef.value.clearValidate()
  }
  // 清除文件输入
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const closeModal = () => {
  showModal.value = false
  uploadedFile.value = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 验证文件类型
    const allowedExtensions = ['.jar', '.zip', '.rar']
    const fileName = file.name.toLowerCase()
    const isValidType = allowedExtensions.some(ext => fileName.endsWith(ext))
    
    if (!isValidType) {
      alert('请选择有效的驱动文件 (.jar, .zip, .rar)')
      event.target.value = '' // 清空文件输入
      return
    }
    
    // 验证文件大小 (限制为50MB)
    const maxSize = 50 * 1024 * 1024 // 50MB
    if (file.size > maxSize) {
      alert('文件大小不能超过50MB')
      event.target.value = '' // 清空文件输入
      return
    }
    
    uploadedFile.value = file
    console.log('已选择文件:', file.name, '大小:', file.size, '类型:', file.type)
  } else {
    uploadedFile.value = null
  }
}

const validateForm = () => {
  if (!currentDriver.value.driverName) {
    return false
  }
  if (!currentDriver.value.driverClassName) {
    return false
  }
  if (!currentDriver.value.exampleJdbcUrl) {
    return false
  }
  // 编辑时不强制要求上传文件
  if (!currentDriver.value.id && !uploadedFile.value) {
    return false
  }
  return true
}

const saveDriver = async () => {
  if (!driverFormRef.value) return
  
  // 使用 Element Plus 的表单验证
  const valid = await driverFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('driverName', currentDriver.value.driverName)
    formData.append('driverVersion', currentDriver.value.driverVersion || '')
    formData.append('driverDescription', currentDriver.value.driverDescription || '')
    formData.append('driverClassName', currentDriver.value.driverClassName)
    formData.append('exampleJdbcUrl', currentDriver.value.exampleJdbcUrl)
    formData.append('supportedDbTypes', currentDriver.value.supportedDbTypes || '')
    
    if (uploadedFile.value) {
      formData.append('file', uploadedFile.value)
    }
    
    let response
    if (currentDriver.value.id) {
      // 编辑模式
      response = await driverApi.updateDriver(currentDriver.value.id, formData)
    } else {
      // 新增模式
      response = await driverApi.addDriver(formData)
    }
    
    // 适配新的Result响应结构
    if (response.data.code === 200) {
      alert(currentDriver.value.id ? '更新驱动成功' : '新增驱动成功')
      closeModal()
      await loadDrivers()
    } else {
      alert('操作失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('保存驱动失败:', error)
    alert('保存驱动失败: ' + (error.response?.data?.message || error.message))
  } finally {
    // 清除表单验证
    if (driverFormRef.value) {
      driverFormRef.value.clearValidate()
    }
  }
}

const removeDriver = async (row) => {
  if (confirm(`确定要删除驱动 "${row.driverName}" 吗？`)) {
    try {
      const response = await driverApi.removeDriver(row.id)
      if (response.data.code === 200) {
        alert('删除驱动成功')
        await loadDrivers()
      } else {
        alert('删除失败: ' + response.data.message)
      }
    } catch (error) {
      console.error('删除驱动失败:', error)
      alert('删除驱动失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

const toggleDriverStatus = async (row) => {
  try {
    const response = await driverApi.toggleDriverStatus(row.id, !row.isActive)
    if (response.data.code === 200) {
      alert('切换状态成功')
      await loadDrivers()
    } else {
      alert('切换状态失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    alert('切换状态失败: ' + (error.response?.data?.message || error.message))
  }
}

const downloadDriver = (row) => {
  if (row.driverFilePath) {
    debugger
    // 创建一个隐藏的链接来触发下载
    const link = document.createElement('a')
    link.href = row.driverFilePath
    link.download = row.driverFileName
    link.click()
  } else {
    alert('驱动文件不可用')
  }
}
</script>

<style scoped>
.driver-list-container {
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

.btn-active {
  background-color: #e6a23c;
  color: white;
}

.btn-inactive {
  background-color: #909399;
  color: white;
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

.file-info {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
}
</style>