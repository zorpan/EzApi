<template>
  <div class="rule-management-container">
    <div class="toolbar">
      <button class="btn btn-primary" @click="openAddEditDialog()">新增规则</button>
      <button class="btn btn-success" @click="loadRules">刷新</button>
    </div>

    <vxe-table
      :data="rules"
      :loading="loading"
      height="600"
      stripe
      border
      resizable
    >
      <vxe-column type="seq" width="60" title="序号"></vxe-column>
      <vxe-column field="id" title="ID" width="80"></vxe-column>
      <vxe-column field="ruleName" title="规则名称" width="200"></vxe-column>
      <vxe-column field="ruleType" title="规则类型" width="150">
        <template #default="{ row }">
          <span :class="'status-' + getRuleTypeClass(row.ruleType)">
            {{ row.ruleType }}
          </span>
        </template>
      </vxe-column>
      <vxe-column field="apiId" title="关联API ID" width="120"></vxe-column>
      <vxe-column field="status" title="状态" width="100">
        <template #default="{ row }">
          <span :class="row.status === 'ACTIVE' ? 'status-active' : 'status-inactive'">
            {{ row.status }}
          </span>
        </template>
      </vxe-column>
      <vxe-column field="createTime" title="创建时间" width="180"></vxe-column>
      <vxe-column title="操作" width="200">
        <template #default="{ row }">
          <button class="btn btn-sm btn-info" @click="viewRuleDetails(row)">查看</button>
          <button class="btn btn-sm btn-warning" @click="openAddEditDialog(row)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="deleteRule(row)">删除</button>
        </template>
      </vxe-column>
    </vxe-table>

    <!-- 规则详情对话框 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="showRuleDialog" @click="closeRuleDialog">
        <div class="modal-dialog modal-xl" @click.stop>
          <div class="modal-header">
            <h3>{{ currentRule.id ? '编辑授权规则' : '新增授权规则' }}</h3>
            <button class="modal-close" @click="closeRuleDialog">&times;</button>
          </div>
          <div class="modal-body">
            <form class="form">
              <div class="form-row">
                <div class="form-group" style="flex: 1;">
                  <label>规则名称 *</label>
                  <input type="text" v-model="currentRule.ruleName" placeholder="请输入规则名称">
                </div>
                <div class="form-group" style="flex: 1; margin-left: 15px;">
                  <label>规则类型 *</label>
                  <select v-model="currentRule.ruleType">
                    <option value="IP_WHITELIST">IP白名单</option>
                    <option value="ROLE_BASED">角色权限</option>
                    <option value="TOKEN_REQUIRED">TOKEN_REQUIRED</option>
                  </select>
                </div>
                <div class="form-group" style="flex: 1; margin-left: 15px;">
                  <label>关联API ID</label>
                  <input type="number" v-model.number="currentRule.apiId" placeholder="留空表示全局规则">
                </div>
                <div class="form-group" style="flex: 1; margin-left: 15px;">
                  <label>状态 *</label>
                  <select v-model="currentRule.status">
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="INACTIVE">INACTIVE</option>
                  </select>
                </div>
              </div>
              <div class="form-group">
                <label>规则内容</label>
                <textarea v-model="currentRule.ruleContent" class="form-textarea" rows="4" placeholder="请输入规则内容(JSON格式)"></textarea>
              </div>
              <div class="form-group">
                <label>描述</label>
                <input type="text" v-model="currentRule.description" placeholder="请输入描述信息">
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button class="btn btn-primary" @click="saveRule">保存</button>
            <button class="btn btn-secondary" @click="closeRuleDialog">取消</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 规则详情对话框 -->
    <Teleport to="body">
      <div class="modal-overlay" v-if="ruleDetailDialogVisible" @click="closeRuleDetailDialog">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3>规则详情</h3>
            <button class="modal-close" @click="closeRuleDetailDialog">&times;</button>
          </div>
          <div class="modal-body" v-if="selectedRule">
            <div class="detail-item">
              <label>ID:</label>
              <span>{{ selectedRule.id }}</span>
            </div>
            <div class="detail-item">
              <label>规则名称:</label>
              <span>{{ selectedRule.ruleName }}</span>
            </div>
            <div class="detail-item">
              <label>规则类型:</label>
              <span :class="'status-' + getRuleTypeClass(selectedRule.ruleType)">
                {{ selectedRule.ruleType }}
              </span>
            </div>
            <div class="detail-item">
              <label>关联API ID:</label>
              <span>{{ selectedRule.apiId || '无' }}</span>
            </div>
            <div class="detail-item">
              <label>状态:</label>
              <span :class="selectedRule.status === 'ACTIVE' ? 'status-active' : 'status-inactive'">
                {{ selectedRule.status }}
              </span>
            </div>
            <div class="detail-item">
              <label>规则内容:</label>
              <pre>{{ selectedRule.ruleContent }}</pre>
            </div>
            <div class="detail-item">
              <label>创建时间:</label>
              <span>{{ selectedRule.createTime }}</span>
            </div>
            <div class="detail-item">
              <label>描述:</label>
              <span>{{ selectedRule.description }}</span>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeRuleDetailDialog">关闭</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const rules = ref([])
const loading = ref(false)
const showRuleDialog = ref(false)
const ruleDetailDialogVisible = ref(false)
const currentRule = ref({
  id: null,
  ruleName: '',
  ruleType: 'IP_WHITELIST',
  status: 'ACTIVE',
  ruleContent: '',
  description: ''
})
const selectedRule = ref({})

onMounted(() => {
  loadRules()
})

const loadRules = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/auth/rules') // 获取所有规则
    rules.value = response.data.data || []
  } catch (error) {
    console.error('加载授权规则失败:', error)
    alert('加载授权规则失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const openAddEditDialog = (rule = null) => {
  if (rule) {
    // 深拷贝规则对象，避免直接修改原对象
    currentRule.value = { ...rule }
  } else {
    currentRule.value = {
      id: null,
      ruleName: '',
      ruleType: 'IP_WHITELIST',
      status: 'ACTIVE',
      ruleContent: '',
      description: ''
    }
  }
  showRuleDialog.value = true
}

const closeRuleDialog = () => {
  showRuleDialog.value = false
}

const closeRuleDetailDialog = () => {
  ruleDetailDialogVisible.value = false
}

const saveRule = async () => {
  try {
    let response
    if (currentRule.value.id) {
      // 更新现有规则 - 使用新的更新API
      response = await axios.put('/api/auth/rule/update', currentRule.value)
    } else {
      // 创建新规则
      response = await axios.post('/api/auth/rule/create', currentRule.value)
    }
    
    if (response.data.success) {
      alert(currentRule.value.id ? '更新授权规则成功' : '创建授权规则成功')
      closeRuleDialog()
      await loadRules()
    } else {
      alert('操作失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('保存授权规则失败:', error)
    alert('保存授权规则失败: ' + (error.response?.data?.message || error.message))
  }
}

const deleteRule = async (rule) => {
  if (confirm(`确定要删除规则 "${rule.ruleName}" 吗？`)) {
    try {
      const response = await axios.delete(`/api/auth/rule/${rule.id}`)
      if (response.data.success) {
        alert('删除授权规则成功')
        await loadRules()
      } else {
        alert('删除失败: ' + response.data.message)
      }
    } catch (error) {
      console.error('删除授权规则失败:', error)
      alert('删除授权规则失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

const viewRuleDetails = (rule) => {
  selectedRule.value = rule
  ruleDetailDialogVisible.value = true
}

const getRuleTypeClass = (ruleType) => {
  switch (ruleType) {
    case 'IP_WHITELIST': return 'primary'
    case 'ROLE_BASED': return 'success'
    case 'TOKEN_REQUIRED': return 'warning'
    default: return 'info'
  }
}
</script>

<style scoped>
.rule-management-container {
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