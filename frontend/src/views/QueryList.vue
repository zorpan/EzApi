<template>
  <div class="query-list-container">
    <div class="toolbar">
      <button class="btn btn-primary" @click="openAddQueryDialog">新建查询</button>
      <button class="btn btn-success" @click="loadQueries">刷新</button>
    </div>

    <vxe-table
      :data="queries"
      :loading="loading"
      height="600"
      stripe
      border
      resizable
    >
      <vxe-column type="seq" width="60" title="序号"></vxe-column>
      <vxe-column field="name" title="查询名称" width="200"></vxe-column>
      <vxe-column field="dataSourceId" title="数据源" width="150"></vxe-column>
      <vxe-column field="sql" title="SQL语句" width="400" show-overflow></vxe-column>
      <vxe-column field="createdAt" title="创建时间" width="150"></vxe-column>
      <vxe-column title="操作" width="200">
        <template #default="{ row }">
          <button class="btn btn-sm btn-info" @click="executeQuery(row)">执行</button>
          <button class="btn btn-sm btn-warning" @click="editQuery(row)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="deleteQuery(row)">删除</button>
        </template>
      </vxe-column>
    </vxe-table>

    <!-- 新增/编辑查询对话框 -->
    <vxe-modal
      v-model="showQueryModal"
      title="查询配置"
      width="800"
      height="600"
      resize
      :show-footer="false"
    >
      <template #default>
        <form class="form">
          <div class="form-group">
            <label>查询名称:</label>
            <input type="text" v-model="currentQuery.name" placeholder="查询名称">
          </div>
          <div class="form-group">
            <label>选择数据源:</label>
            <select v-model="currentQuery.dataSourceId" class="form-select">
              <option value="">请选择数据源</option>
              <option v-for="ds in dataSources" :key="ds.id" :value="ds.id">{{ ds.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>SQL语句:</label>
            <textarea 
              v-model="currentQuery.sql" 
              rows="10" 
              placeholder="请输入SQL语句"
              class="form-textarea"
            ></textarea>
          </div>
          <div class="form-actions">
            <button class="btn btn-primary" @click="saveQuery">保存</button>
            <button class="btn btn-secondary" @click="showQueryModal = false">取消</button>
          </div>
        </form>
      </template>
    </vxe-modal>

    <!-- 执行查询结果对话框 -->
    <vxe-modal
      v-model="showResultModal"
      title="查询结果"
      width="1000"
      height="600"
      resize
      :show-footer="false"
    >
      <template #default>
        <vxe-table
          :data="queryResult"
          height="450"
          stripe
          border
          resizable
        >
          <vxe-column
            v-for="col in resultColumns"
            :key="col.field"
            :field="col.field"
            :title="col.title"
            :width="col.width"
          ></vxe-column>
        </vxe-table>
      </template>
    </vxe-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const queries = ref([])
const loading = ref(false)
const showQueryModal = ref(false)
const showResultModal = ref(false)
const currentQuery = ref({
  id: '',
  name: '',
  dataSourceId: '',
  sql: ''
})
const dataSources = ref([])
const queryResult = ref([])
const resultColumns = ref([])

onMounted(() => {
  loadQueries()
  loadDataSources()
})

const loadQueries = async () => {
  loading.value = true
  try {
    // 这拟数据，实际项目中应从后端API获取
    queries.value = [
      { id: 1, name: '用户查询', dataSourceId: 'default', sql: 'SELECT * FROM users', createdAt: new Date().toISOString() },
      { id: 2, name: '订单查询', dataSourceId: 'default', sql: 'SELECT * FROM orders', createdAt: new Date().toISOString() }
    ]
  } catch (error) {
    console.error('加载查询失败:', error)
  } finally {
    loading.value = false
  }
}

const loadDataSources = async () => {
  try {
    const response = await axios.get('/api/datasource/list')
    dataSources.value = response.data
  } catch (error) {
    console.error('加载数据源失败:', error)
  }
}

const openAddQueryDialog = () => {
  currentQuery.value = {
    id: '',
    name: '',
    dataSourceId: '',
    sql: ''
  }
  showQueryModal.value = true
}

const editQuery = (row) => {
  currentQuery.value = { ...row }
  showQueryModal.value = true
}

const saveQuery = async () => {
  try {
    // 这拟保存逻辑
    if (currentQuery.value.id) {
      // 编辑
    } else {
      // 新增
      currentQuery.value.id = queries.value.length + 1
      currentQuery.value.createdAt = new Date().toISOString()
      queries.value.push(currentQuery.value)
    }
    showQueryModal.value = false
  } catch (error) {
    console.error('保存查询失败:', error)
  }
}

const deleteQuery = async (row) => {
  if (confirm(`确定要删除查询 "${row.name}" 吗？`)) {
    try {
      // 这拟删除逻辑
      const index = queries.value.findIndex(q => q.id === row.id)
      if (index !== -1) {
        queries.value.splice(index, 1)
      }
    } catch (error) {
      console.error('删除查询失败:', error)
    }
  }
}

const executeQuery = async (row) => {
  try {
    const response = await axios.post('/api/query/execute', {
      dataSourceId: row.dataSourceId,
      sql: row.sql
    })
    
    // 生成列定义
    if (response.data && response.data.length > 0) {
      resultColumns.value = Object.keys(response.data[0]).map(key => ({
        field: key,
        title: key,
        width: 150
      }))
    } else {
      resultColumns.value = []
    }
    
    queryResult.value = response.data
    showResultModal.value = true
  } catch (error) {
    console.error('执行查询失败:', error)
    alert(error.response?.data || '执行查询失败')
  }
}
</script>

<style scoped>
.query-list-container {
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

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-select {
  height: 34px;
}

.form-textarea {
  font-family: monospace;
}

.form-actions {
  text-align: right;
  margin-top: 20px;
}
</style>