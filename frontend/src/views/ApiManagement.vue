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
      <vxe-column field="id" title="ID" width="80"></vxe-column>
      <vxe-column field="apiName" title="接口名称" width="200"></vxe-column>
      <vxe-column field="apiPath" title="接口路径" width="200">
        <template #default="{ row }">
          <span>/api/call{{ row.apiPath }}</span>
        </template>
      </vxe-column>
      <vxe-column field="apiMethod" title="请求方法" width="120"></vxe-column>
      <vxe-column field="status" title="状态" width="100">
        <template #default="{ row }">
          <span :class="row.status === 1 ? 'status-active' : 'status-inactive'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </span>
        </template>
      </vxe-column>
      <vxe-column field="description" title="描述" width="300" show-overflow></vxe-column>
      <vxe-column title="操作" width="250">
        <template #default="{ row }">
          <button class="btn btn-sm btn-info" @click="viewApiDetails(row)">查看</button>
          <button class="btn btn-sm btn-warning" @click="openAddEditDialog(row)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="deleteApi(row)">删除</button>
          <button 
            :class="['btn', 'btn-sm', row.status === 1 ? 'btn-inactive' : 'btn-active']"
            @click="toggleApiStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </button>
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
              </div>
              
              <div class="form-row">
                <div class="form-group" style="flex: 1;">
                  <label>数据源ID *</label>
                  <select v-model="currentApi.dataSourceId">
                    <option value="">请选择数据源</option>
                    <option v-for="ds in dataSources" :key="ds.id" :value="ds.id">{{ ds.name }}</option>
                  </select>
                </div>
                <div class="form-group" style="flex: 1; margin-left: 15px;">
                  <label>状态</label>
                  <select v-model="currentApi.status">
                    <option value="0">启用</option>
                    <option value="1">禁用</option>
                  </select>
                </div>
              </div>
              
              <div class="form-group">
                <label>SQL内容 *</label>
                <textarea 
                  v-model="currentApi.sqlContent" 
                  rows="6" 
                  placeholder="请输入SQL查询语句"
                  class="form-textarea"
                ></textarea>
              </div>
              
              <div class="form-group">
                <label>描述</label>
                <input type="text" v-model="currentApi.description" placeholder="请输入接口描述">
              </div>
              
              <!-- 参数配置区域 -->
              <div class="parameter-section">
                <div class="section-header">
                  <h4>参数配置</h4>
                  <button type="button" class="btn btn-sm btn-primary" @click="addParameter">添加参数</button>
                </div>
                
                <div class="parameter-table-container" v-if="currentApi.parameters && currentApi.parameters.length > 0">
                  <table class="parameter-table">
                    <thead>
                      <tr>
                        <th>参数名</th>
                        <th>参数类型</th>
                        <th>是否必填</th>
                        <th>默认值</th>
                        <th>描述</th>
                        <th width="120">操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(param, index) in currentApi.parameters" :key="index">
                        <td>
                          <input v-if="editingParamIndex === index" type="text" v-model="param.paramName" placeholder="参数名">
                          <span v-else>{{ param.paramName }}</span>
                        </td>
                        <td>
                          <select v-if="editingParamIndex === index" v-model="param.paramType">
                            <option value="STRING">STRING</option>
                            <option value="INTEGER">INTEGER</option>
                            <option value="BOOLEAN">BOOLEAN</option>
                            <option value="DATE">DATE</option>
                          </select>
                          <span v-else>{{ param.paramType }}</span>
                        </td>
                        <td>
                          <input v-if="editingParamIndex === index" type="checkbox" v-model="param.required">
                          <span v-else>{{ param.required ? '是' : '否' }}</span>
                        </td>
                        <td>
                          <input v-if="editingParamIndex === index" type="text" v-model="param.defaultValue" placeholder="默认值">
                          <span v-else>{{ param.defaultValue }}</span>
                        </td>
                        <td>
                          <input v-if="editingParamIndex === index" type="text" v-model="param.description" placeholder="描述">
                          <span v-else>{{ param.description }}</span>
                        </td>
                        <td>
                          <template v-if="editingParamIndex === index">
                            <button class="btn btn-sm btn-success" @click="saveParameter(index)">保存</button>
                            <button class="btn btn-sm btn-secondary" @click="cancelEditParameter">取消</button>
                          </template>
                          <template v-else>
                            <button class="btn btn-sm btn-warning" @click="editParameter(index)">编辑</button>
                            <button class="btn btn-sm btn-danger" @click="removeParameter(index)">删除</button>
                          </template>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div v-else class="no-parameters">
                  暂无参数配置
                </div>
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

    <!-- 接口详情对话框 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="apiDetailDialogVisible" @click="closeApiDetailDialog">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3>接口详情</h3>
            <button class="modal-close" @click="closeApiDetailDialog">&times;</button>
          </div>
          <div class="modal-body" v-if="selectedApi">
            <div class="detail-item">
              <label>ID:</label>
              <span>{{ selectedApi.id }}</span>
            </div>
            <div class="detail-item">
              <label>接口名称:</label>
              <span>{{ selectedApi.apiName }}</span>
            </div>
            <div class="detail-item">
              <label>接口路径:</label>
              <span>/api/call{{ selectedApi.apiPath }}</span>
            </div>
            <div class="detail-item">
              <label>请求方法:</label>
              <span>{{ selectedApi.apiMethod }}</span>
            </div>
            <div class="detail-item">
              <label>数据源ID:</label>
              <span>{{ selectedApi.dataSourceId }}</span>
            </div>
            <div class="detail-item">
              <label>状态:</label>
              <span :class="selectedApi.status === 1 ? 'status-active' : 'status-inactive'">
                {{ selectedApi.status === 0 ? '启用' : '禁用' }}
              </span>
            </div>
            <div class="detail-item">
              <label>SQL内容:</label>
              <pre>{{ selectedApi.sqlContent }}</pre>
            </div>
            <div class="detail-item">
              <label>描述:</label>
              <span>{{ selectedApi.description }}</span>
            </div>
            <div class="detail-item" v-if="selectedApi.parameters && selectedApi.parameters.length > 0">
              <label>参数配置:</label>
              <ul>
                <li v-for="(param, index) in selectedApi.parameters" :key="index">
                  {{ param.paramName }} ({{ param.paramType }}) - {{ param.description || '无描述' }} 
                  <span v-if="param.required" class="required-tag">必填</span>
                  <span v-if="param.defaultValue">默认值: {{ param.defaultValue }}</span>
                </li>
              </ul>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeApiDetailDialog">关闭</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { apiManagementApi, dataSourceApi } from '../api'

const apis = ref([])
const loading = ref(false)
const showApiDialog = ref(false)
const apiDetailDialogVisible = ref(false)
const currentApi = ref({
  id: null,
  apiName: '',
  apiPath: '',
  apiMethod: 'GET',
  dataSourceId: '',
  sqlContent: '',
  description: '',
  status: 0,
  parameters: []
})
const selectedApi = ref({})
const editingParamIndex = ref(-1)
const dataSources = ref([]) // 存储数据源列表

onMounted(() => {
  loadApis()
  loadDataSources() // 加载数据源列表
})

const loadDataSources = async () => {
  try {
    const response = await dataSourceApi.getAllDataSources()
    dataSources.value = response.data.data || []
  } catch (error) {
    console.error('加载数据源失败:', error)
  }
}

const loadApis = async () => {
  loading.value = true
  try {
    const response = await apiManagementApi.getAllApis()
    // 适配新的Result响应结构
    apis.value = response.data.data || []
  } catch (error) {
    console.error('加载接口失败:', error)
    alert('加载接口失败: ' + (error.response?.data?.message || error.message))
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
      response = await apiManagementApi.saveApi(apiData)
    } else {
      response = await apiManagementApi.saveApi(apiData)
    }
    
    // 适配新的Result响应结构
    if (response.data.code === 200) {
      alert(currentApi.value.id ? '更新接口成功' : '创建接口成功')
      closeApiDialog()
      await loadApis()
    } else {
      alert('操作失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('保存接口失败:', error)
    alert('保存接口失败: ' + (error.response?.data?.message || error.message))
  }
}

const deleteApi = async (api) => {
  if (confirm(`确定要删除接口 "${api.apiName}" 吗？`)) {
    try {
      const response = await apiManagementApi.deleteApi(api.id)
      // 适配新的Result响应结构
      if (response.data.code === 200) {
        alert('删除接口成功')
        await loadApis()
      } else {
        alert('删除失败: ' + response.data.message)
      }
    } catch (error) {
      console.error('删除接口失败:', error)
      alert('删除接口失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

const toggleApiStatus = async (api) => {
  try {
    const newStatus = api.status === 0 ? 1 : 0
    const response = await apiManagementApi.updateApiStatus(api.id, { status: newStatus })
    // 适配新的Result响应结构
    if (response.data.code === 200) {
      alert(`接口${newStatus === 1 ? '启用' : '禁用'}成功`)
      await loadApis()
    } else {
      alert('状态切换失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('切换接口状态失败:', error)
    alert('切换接口状态失败: ' + (error.response?.data?.message || error.message))
  }
}

const viewApiDetails = (api) => {
  selectedApi.value = api
  apiDetailDialogVisible.value = true
}

const closeApiDetailDialog = () => {
  apiDetailDialogVisible.value = false
}

// 参数管理相关方法
const addParameter = () => {
  if (!currentApi.value.parameters) {
    currentApi.value.parameters = []
  }
  currentApi.value.parameters.push({
    paramName: '',
    paramType: 'STRING',
    required: false,
    defaultValue: '',
    description: ''
  })
}

const editParameter = (index) => {
  editingParamIndex.value = index
}

const saveParameter = (index) => {
  editingParamIndex.value = -1
}

const cancelEditParameter = () => {
  editingParamIndex.value = -1
}

const removeParameter = (index) => {
  if (currentApi.value.parameters && currentApi.value.parameters.length > index) {
    currentApi.value.parameters.splice(index, 1)
  }
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

.detail-item pre {
  flex: 1;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.detail-item ul {
  flex: 1;
  margin: 0;
  padding-left: 20px;
}

.detail-item li {
  margin-bottom: 5px;
}

.required-tag {
  color: #f56c6c;
  margin-left: 5px;
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

.status-toggle-btn {
  padding: 4px 8px;
  font-size: 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-active {
  background-color: #67c23a;
  color: white;
}

.btn-inactive {
  background-color: #f56c6c;
  color: white;
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

.api-path-container {
  display: flex;
  align-items: center;
}

.api-path-prefix {
  background-color: #f5f5f5;
  border: 1px solid #dcdfe6;
  border-right: none;
  border-radius: 4px 0 0 4px;
  padding: 8px;
  font-family: monospace;
}

.api-path-input {
  border-radius: 0 4px 4px 0;
  flex: 1;
}

.parameter-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h4 {
  margin: 0;
  font-size: 16px;
}

.parameter-table-container {
  overflow-x: auto;
}

.parameter-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.parameter-table th,
.parameter-table td {
  border: 1px solid #e4e7ed;
  padding: 8px;
  text-align: left;
}

.parameter-table th {
  background-color: #f5f5f5;
}

.parameter-table input,
.parameter-table select {
  width: 100%;
  padding: 4px;
  border: 1px solid #dcdfe6;
  border-radius: 2px;
  box-sizing: border-box;
}

.no-parameters {
  text-align: center;
  color: #909399;
  padding: 20px;
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