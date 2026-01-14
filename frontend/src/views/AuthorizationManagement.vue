<template>
  <div class="authorization-management-container">
    <div class="toolbar">
      <button class="btn btn-primary" @click="openAddEditDialog()">新增API密钥</button>
      <button class="btn btn-success" @click="loadTokens">刷新</button>
    </div>

    <vxe-table
      :data="tokens"
      :loading="loading"
      height="600"
      stripe
      border
      resizable
    >
      <vxe-column type="seq" width="60" title="序号"></vxe-column>
      <vxe-column field="id" title="ID" width="80"></vxe-column>
      <vxe-column field="tokenName" title="密钥名称" width="200"></vxe-column>
      <vxe-column field="tokenValue" title="密钥值" width="300" show-overflow>
        <template #default="{ row }">
          <span>{{ maskTokenValue(row.tokenValue) }}</span>
          <button class="btn btn-sm btn-info" @click="copyToken(row.tokenValue)" style="margin-left: 10px;">复制</button>
        </template>
      </vxe-column>
      <vxe-column field="tokenType" title="类型" width="120"></vxe-column>
      <vxe-column field="status" title="状态" width="150">
        <template #default="{ row }">
          <span :class="row.status === 'ACTIVE' ? 'status-active' : 'status-inactive'" style="display: inline-block; min-width: 60px;">
            {{ row.status }}
          </span>
          <button 
            @click="toggleTokenStatus(row)" 
            :class="['status-toggle-btn', row.status === 'ACTIVE' ? 'btn-inactive' : 'btn-active']"
            style="margin-left: 10px;"
          >
            {{ row.status === 'ACTIVE' ? '停用' : '激活' }}
          </button>
        </template>
      </vxe-column>
      <vxe-column field="createTime" title="创建时间" width="180"></vxe-column>
      <vxe-column title="操作" width="200">
        <template #default="{ row }">
          <button class="btn btn-sm btn-info" @click="viewTokenDetails(row)">查看</button>
          <button class="btn btn-sm btn-warning" @click="openAddEditDialog(row)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="deleteToken(row)">删除</button>
        </template>
      </vxe-column>
    </vxe-table>

    <!-- API密钥详情对话框 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="showTokenDialog" @click="closeTokenDialog">
        <div class="modal-dialog modal-xl" @click.stop>
          <div class="modal-header">
            <h3>{{ currentToken.id ? '编辑API密钥' : '新增API密钥' }}</h3>
            <button class="modal-close" @click="closeTokenDialog">&times;</button>
          </div>
          <div class="modal-body">
            <form class="form">
              <div class="form-row">
                <div class="form-group" style="flex: 1;">
                  <label>密钥名称 *</label>
                  <input type="text" v-model="currentToken.tokenName" placeholder="请输入密钥名称">
                </div>
                <div class="form-group" style="flex: 1;">
                  <label>密钥类型 *</label>
                  <select v-model="currentToken.tokenType">
                    <option value="API_KEY">API_KEY</option>
                    <option value="JWT">JWT</option>
                    <option value="BASIC_AUTH">BASIC_AUTH</option>
                  </select>
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>
                    <input type="checkbox" v-model="currentToken.enableRateLimit"> 启用频率限制
                  </label>
                </div>
                <div class="form-group" v-show="currentToken.enableRateLimit">
                  <label>每小时限制</label>
                  <input type="number" v-model.number="currentToken.rateLimitPerHour" :min="1" :max="10000" placeholder="请输入每小时限制次数">
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>关联API IDs</label>
                  <el-transfer
                      v-model="selectedApiIds"
                      filterable
                      :data="apiTransferData"
                      :titles="['可选API', '已选API']"
                      :button-texts="['移除', '添加']"
                      :format="{
                    noChecked: '${total}',
                    hasChecked: '${checked}/${total}'
                  }"
                  />
                </div>
              </div>
              <div class="form-group">
                <label>描述</label>
                <input type="text" v-model="currentToken.description" placeholder="请输入描述信息">
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button class="btn btn-primary" @click="saveToken">保存</button>
            <button class="btn btn-secondary" @click="closeTokenDialog">取消</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 令牌详情对话框 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="tokenDetailDialogVisible" @click="closeTokenDetailDialog">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3>API密钥详情</h3>
            <button class="modal-close" @click="closeTokenDetailDialog">&times;</button>
          </div>
          <div class="modal-body" v-if="selectedToken">
            <div class="detail-item">
              <label>ID:</label>
              <span>{{ selectedToken.id }}</span>
            </div>
            <div class="detail-item">
              <label>密钥名称:</label>
              <span>{{ selectedToken.tokenName }}</span>
            </div>
            <div class="detail-item">
              <label>密钥值:</label>
              <span>{{ selectedToken.tokenValue }}</span>
              <button class="btn btn-sm btn-info" @click="copyToken(selectedToken.tokenValue)" style="margin-left: 10px;">复制</button>
            </div>
            <div class="detail-item">
              <label>关联API IDs:</label>
              <span>{{ selectedToken.apiIds && selectedToken.apiIds.length > 0 ? selectedToken.apiIds.join(', ') : '通用密钥' }}</span>
            </div>
            <div class="detail-item">
              <label>类型:</label>
              <span>{{ selectedToken.tokenType }}</span>
            </div>
            <div class="detail-item">
              <label>状态:</label>
              <span :class="selectedToken.status === 'ACTIVE' ? 'status-active' : 'status-inactive'">
                {{ selectedToken.status }}
              </span>
            </div>
            <div class="detail-item">
              <label>启用频率限制:</label>
              <span :class="selectedToken.enableRateLimit ? 'status-active' : 'status-inactive'">
                {{ selectedToken.enableRateLimit ? '是' : '否' }}
              </span>
            </div>
            <div class="detail-item" v-if="selectedToken.enableRateLimit">
              <label>每小时限制:</label>
              <span>{{ selectedToken.rateLimitPerHour }}</span>
            </div>
            <div class="detail-item">
              <label>创建时间:</label>
              <span>{{ selectedToken.createTime }}</span>
            </div>
            <div class="detail-item">
              <label>过期时间:</label>
              <span>{{ selectedToken.expireTime || '无' }}</span>
            </div>
            <div class="detail-item">
              <label>描述:</label>
              <span>{{ selectedToken.description }}</span>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeTokenDetailDialog">关闭</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const tokens = ref([])
const apis = ref([]) // 存储API列表
const loading = ref(false)
const showTokenDialog = ref(false)
const tokenDetailDialogVisible = ref(false)
const currentToken = ref({
  id: null,
  tokenName: '',
  tokenType: 'API_KEY',
  status: 'ACTIVE',
  enableRateLimit: false,
  rateLimitPerHour: 1000,
  description: '',
  apiIds: [] // 存储关联的API ID列表
})
const selectedApiIds = ref([]) // 当前选中的API ID列表
const selectedToken = ref({})

onMounted(() => {
  loadTokens()
  loadApis() // 加载API列表
})

// 计算属性：转换API数据为el-transfer所需格式
const apiTransferData = computed(() => {
  return apis.value.map(api => ({
    key: api.id,
    label: `${api.apiName} (${api.apiPath})`,
    ...api // 保留原始api对象的所有属性
  }))
})

const loadTokens = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/auth/tokens') // 获取所有令牌
    tokens.value = response.data.data || []
  } catch (error) {
    console.error('加载API密钥失败:', error)
    alert('加载API密钥失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const loadApis = async () => {
  try {
    // 获取所有API列表
    const response = await axios.get('/api/management/apis')
    apis.value = response.data || []
  } catch (error) {
    console.error('加载API列表失败:', error)
    // 如果获取API列表失败，也可以继续，用户可以选择通用密钥
    apis.value = []
  }
}

const openAddEditDialog = (token = null) => {
  if (token) {
    // 深拷贝API密钥对象，避免直接修改原对象
    currentToken.value = { ...token }
    // 设置选中的API IDs
    selectedApiIds.value = token.apiIds ? [...token.apiIds] : []
  } else {
    currentToken.value = {
      id: null,
      tokenName: '',
      tokenType: 'API_KEY',
      status: 'ACTIVE',
      enableRateLimit: false,
      rateLimitPerHour: 1000,
      description: '',
      apiIds: []
    }
    selectedApiIds.value = []
  }
  showTokenDialog.value = true
}

const closeTokenDialog = () => {
  showTokenDialog.value = false
}

const closeTokenDetailDialog = () => {
  tokenDetailDialogVisible.value = false
}

const saveToken = async () => {
  try {
    // 将选中的API IDs赋值给token对象
    currentToken.value.apiIds = selectedApiIds.value
    
    let response
    if (currentToken.value.id) {
      // 更新现有令牌
      response = await axios.post('/api/auth/token/create', currentToken.value)
    } else {
      // 创建新令牌
      response = await axios.post('/api/auth/token/create', currentToken.value)
    }
    
    if (response.data.success) {
      alert(currentToken.value.id ? '更新API密钥成功' : '创建API密钥成功')
      closeTokenDialog()
      await loadTokens()
      if (!currentToken.value.id) {
        // 如果是新创建的，显示令牌值
        alert(`新API密钥值: ${response.data.data.tokenValue}\n请妥善保管此密钥，它只会显示一次。`)
      }
    } else {
      alert('操作失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('保存API密钥失败:', error)
    alert('保存API密钥失败: ' + (error.response?.data?.message || error.message))
  }
}

const deleteToken = async (token) => {
  if (confirm(`确定要删除API密钥 "${token.tokenName}" 吗？`)) {
    try {
      const response = await axios.delete(`/api/auth/token/${token.id}`)
      if (response.data.success) {
        alert('删除API密钥成功')
        await loadTokens()
      } else {
        alert('删除失败: ' + response.data.message)
      }
    } catch (error) {
      console.error('删除API密钥失败:', error)
      alert('删除API密钥失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

const toggleTokenStatus = async (token) => {
  try {
    const response = await axios.put(`/api/auth/token/toggle-status/${token.id}`)
    if (response.data.success) {
      alert('API密钥状态切换成功')
      await loadTokens() // 重新加载数据以获取最新状态
    } else {
      alert('状态切换失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('切换API密钥状态失败:', error)
    alert('切换API密钥状态失败: ' + (error.response?.data?.message || error.message))
  }
}

const viewTokenDetails = (token) => {
  selectedToken.value = token
  tokenDetailDialogVisible.value = true
}

const maskTokenValue = (tokenValue) => {
  if (!tokenValue || tokenValue.length <= 8) return tokenValue
  return tokenValue.substring(0, 4) + '...' + tokenValue.substring(tokenValue.length - 4)
}

const copyToken = async (tokenValue) => {
  try {
    await navigator.clipboard.writeText(tokenValue)
    alert('API密钥已复制到剪贴板')
  } catch (err) {
    console.error('复制失败:', err)
    alert('复制失败')
  }
}
</script>

<style scoped>
.authorization-management-container {
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