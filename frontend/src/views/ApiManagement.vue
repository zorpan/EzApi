<template>
  <div class="api-management-container">
    <div class="toolbar">
      <button class="btn btn-primary" @click="openAddEditDialog()">新增接口</button>
      <button class="btn btn-success" @click="loadApis">刷新</button>
    </div>

    <vxe-table
      :data="apis"
      :loading="loading"
      height="600"
      stripe
      border
      resizable
    >
      <vxe-column type="seq" width="60" title="序号"></vxe-column>
      <vxe-column field="apiName" title="接口名称" width="150"></vxe-column>
      <vxe-column field="apiPath" title="接口路径" width="200"></vxe-column>
      <vxe-column field="apiMethod" title="请求方法" width="100"></vxe-column>
      <vxe-column field="dataSourceId" title="数据源" width="120"></vxe-column>
      <vxe-column field="description" title="描述" width="200" show-overflow></vxe-column>
      <vxe-column field="status" title="状态" width="80">
        <template #default="{ row }">
          <span :class="row.status === 1 ? 'status-active' : 'status-inactive'">
            {{ row.status === 1 ? '上线' : '下线' }}
          </span>
        </template>
      </vxe-column>
      <vxe-column title="操作" width="300">
        <template #default="{ row }">
          <button class="btn btn-sm btn-info" @click="toggleApiStatus(row)">切换状态</button>
          <button class="btn btn-sm btn-warning" @click="openAddEditDialog(row)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="deleteApi(row)">删除</button>
        </template>
      </vxe-column>
    </vxe-table>

    <!-- 接口详情对话框，包含参数配置 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="showApiDialog" @click="closeApiDialog">
        <div class="modal-dialog modal-xl" @click.stop>
          <div class="modal-header">
            <h3>{{ currentApi.id ? '编辑接口' : '新增接口' }}</h3>
            <button class="modal-close" @click="closeApiDialog">&times;</button>
          </div>
          <div class="modal-body">
            <form class="form">
              <div class="form-row">
                <div class="form-group" style="flex: 1;">
                  <label>接口名称 *</label>
                  <input type="text" v-model="currentApi.apiName" placeholder="请输入接口名称">
                </div>
                <div class="form-group" style="flex: 1; margin-left: 15px;">
                  <label>接口路径 *</label>
                  <div class="api-path-container">
                    <span class="api-path-prefix">/api/call</span>
                    <input type="text" v-model="currentApi.apiPath" placeholder="/users" class="api-path-input">
                  </div>
                </div>
                <div class="form-group" style="flex: 1; margin-left: 15px;">
                  <label>请求方法 *</label>
                  <select v-model="currentApi.apiMethod">
                    <option value="GET">GET</option>
                    <option value="POST">POST</option>
                    <option value="PUT">PUT</option>
                    <option value="DELETE">DELETE</option>
                  </select>
                </div>
                <div class="form-group" style="flex: 1; margin-left: 15px;">
                  <label>数据源 *</label>
                  <select v-model="currentApi.dataSourceId">
                    <option value="">请选择数据源</option>
                    <option v-for="ds in dataSources" :key="ds.id" :value="ds.id">
                      {{ ds.name }} ({{ ds.dbType }})
                    </option>
                  </select>
                </div>
              </div>
              <div class="form-group">
                <label>描述</label>
                <input type="text" v-model="currentApi.description" placeholder="接口描述">
              </div>
              <div class="form-group">
                <label>SQL内容 *</label>
                <textarea v-model="currentApi.sqlContent" class="form-textarea" rows="8" placeholder="请输入SQL语句"></textarea>
              </div>
              <div class="form-group">
                <label>状态</label>
                <select v-model="currentApi.status">
                  <option value="0">下线</option>
                  <option value="1">上线</option>
                </select>
              </div>
              
              <!-- 参数配置区域 -->
              <div class="parameter-section">
                <div class="parameter-header">
                  <h4>参数配置</h4>
                  <button type="button" class="btn btn-primary" @click="addParameter">添加参数</button>
                </div>
                
                <vxe-table
                  :data="currentApi.parameters"
                  height="300"
                  stripe
                  border
                  resizable
                >
                  <vxe-column type="seq" width="60" title="序号"></vxe-column>
                  <vxe-column field="paramName" title="参数名" width="120">
                    <template #default="{ row, rowIndex }">
                      <input v-if="editingParamIndex === rowIndex" type="text" v-model="row.paramName" placeholder="参数名">
                      <span v-else>{{ row.paramName }}</span>
                    </template>
                  </vxe-column>
                  <vxe-column field="paramType" title="类型" width="100">
                    <template #default="{ row, rowIndex }">
                      <select v-if="editingParamIndex === rowIndex" v-model="row.paramType">
                        <option value="STRING">STRING</option>
                        <option value="INTEGER">INTEGER</option>
                        <option value="BOOLEAN">BOOLEAN</option>
                        <option value="DATE">DATE</option>
                      </select>
                      <span v-else>{{ row.paramType }}</span>
                    </template>
                  </vxe-column>
                  <vxe-column field="required" title="必填" width="80">
                    <template #default="{ row, rowIndex }">
                      <input v-if="editingParamIndex === rowIndex" type="checkbox" v-model="row.required">
                      <span v-else>{{ row.required ? '是' : '否' }}</span>
                    </template>
                  </vxe-column>
                  <vxe-column field="defaultValue" title="默认值" width="150">
                    <template #default="{ row, rowIndex }">
                      <input v-if="editingParamIndex === rowIndex" type="text" v-model="row.defaultValue" placeholder="默认值">
                      <span v-else>{{ row.defaultValue }}</span>
                    </template>
                  </vxe-column>
                  <vxe-column field="validationRule" title="验证规则" width="200">
                    <template #default="{ row, rowIndex }">
                      <input v-if="editingParamIndex === rowIndex" type="text" v-model="row.validationRule" placeholder="正则表达式">
                      <span v-else>{{ row.validationRule }}</span>
                    </template>
                  </vxe-column>
                  <vxe-column field="description" title="描述" min-width="150">
                    <template #default="{ row, rowIndex }">
                      <input v-if="editingParamIndex === rowIndex" type="text" v-model="row.description" placeholder="参数描述">
                      <span v-else>{{ row.description }}</span>
                    </template>
                  </vxe-column>
                  <vxe-column title="操作" width="150">
                    <template #default="{ row, rowIndex }">
                      <button v-if="editingParamIndex !== rowIndex" class="btn btn-sm btn-warning" @click="editParameter(rowIndex)">编辑</button>
                      <button v-if="editingParamIndex === rowIndex" class="btn btn-sm btn-primary" @click="saveParameterEdit">保存</button>
                      <button v-if="editingParamIndex === rowIndex" class="btn btn-sm btn-secondary" @click="editingParamIndex = -1">取消</button>
                      <button class="btn btn-sm btn-danger" @click="removeParameter(rowIndex)">删除</button>
                    </template>
                  </vxe-column>
                </vxe-table>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button class="btn btn-primary" @click="saveApi">保存</button>
            <button class="btn btn-secondary" @click="closeApiDialog">取消</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const apis = ref([])
const loading = ref(false)
const showApiDialog = ref(false)
const editingParamIndex = ref(-1)  // 当前正在编辑的参数索引
const currentApi = ref({
  id: null,
  apiName: '',
  apiPath: '',
  apiMethod: 'GET',
  dataSourceId: '',
  sqlContent: '',
  description: '',
  status: 0,
  parameters: []  // 参数列表
})
const dataSources = ref([])

onMounted(() => {
  loadApis()
  loadDataSources()
})

const loadDataSources = async () => {
  try {
    const response = await axios.get('/api/datasource/list')
    dataSources.value = response.data
  } catch (error) {
    console.error('加载数据源失败:', error)
  }
}

const loadApis = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/management/apis')
    apis.value = response.data
  } catch (error) {
    console.error('加载接口失败:', error)
  } finally {
    loading.value = false
  }
}

const openAddEditDialog = (api = null) => {
  if (api) {
    // 深拷贝接口对象，避免直接修改原对象
    currentApi.value = { 
      ...api, 
      parameters: api.parameters ? [...api.parameters] : []  // 确保参数数组也被复制
    }
  } else {
    currentApi.value = {
      id: null,
      apiName: '',
      apiPath: '',
      apiMethod: 'GET',
      dataSourceId: '',
      sqlContent: '',
      description: '',
      status: 0,
      parameters: []
    }
  }
  showApiDialog.value = true
}

const closeApiDialog = () => {
  showApiDialog.value = false
  editingParamIndex.value = -1
}

const saveApi = async () => {
  try {
    const apiData = { ...currentApi.value }
    // 如果是新建接口，删除id字段，让后端自动生成
    if (!apiData.id) {
      delete apiData.id
    }
    
    let response
    if (currentApi.value.id) {
      response = await axios.post('/api/management/api', apiData)
    } else {
      response = await axios.post('/api/management/api', apiData)
    }
    
    alert(response.data)
    closeApiDialog()
    await loadApis()
  } catch (error) {
    console.error('保存接口失败:', error)
    alert(error.response?.data || '保存接口失败')
  }
}

const toggleApiStatus = async (api) => {
  try {
    const newStatus = api.status === 1 ? 0 : 1
    const response = await axios.put(`/api/management/api/${api.id}/status`, {
      status: newStatus
    })
    
    alert(response.data)
    await loadApis()
  } catch (error) {
    console.error('切换接口状态失败:', error)
    alert(error.response?.data || '切换接口状态失败')
  }
}

const deleteApi = async (api) => {
  if (confirm(`确定要删除接口 "${api.apiName}" 吗？`)) {
    try {
      const response = await axios.delete(`/api/management/api/${api.id}`)
      alert(response.data)
      await loadApis()
    } catch (error) {
      console.error('删除接口失败:', error)
      alert(error.response?.data || '删除接口失败')
    }
  }
}

const addParameter = () => {
  const newParam = {
    id: null,
    apiId: currentApi.value.id,
    paramName: '',
    paramType: 'STRING',
    required: false,
    defaultValue: '',
    description: '',
    validationRule: ''
  }
  currentApi.value.parameters.push(newParam)
}

const editParameter = (index) => {
  editingParamIndex.value = index
}

const saveParameterEdit = () => {
  editingParamIndex.value = -1
}

const removeParameter = (index) => {
  currentApi.value.parameters.splice(index, 1)
}
</script>

<style scoped>
.api-management-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.toolbar {
  margin-bottom: 20px;
}

.parameter-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.parameter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.parameter-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
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

.api-path-container {
  display: flex;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.api-path-prefix {
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-right: 1px solid #dcdfe6;
  white-space: nowrap;
  display: flex;
  align-items: center;
}

.api-path-input {
  flex: 1;
  border: none;
  padding: 8px;
  outline: none;
}

.form-textarea {
  font-family: monospace;
  resize: vertical;
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