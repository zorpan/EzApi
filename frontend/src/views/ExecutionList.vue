<template>
  <div class="execution-list-container">
    <div class="toolbar">
      <button class="btn btn-primary" @click="openExecuteDialog">执行SQL</button>
    </div>

    <div class="execute-panel">
      <div class="panel-left">
        <div class="form-group">
          <label>选择数据源:</label>
          <select v-model="selectedDataSource" class="form-select" @change="onDataSourceChange">
            <option value="">请选择数据源</option>
            <option v-for="ds in dataSources" :key="ds.id" :value="ds.id">{{ ds.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>SQL类型:</label>
          <select v-model="sqlType" class="form-select">
            <option value="select">查询(SELECT)</option>
            <option value="update">更新(UPDATE)</option>
            <option value="insert">插入(INSERT)</option>
            <option value="delete">删除(DELETE)</option>
            <option value="mybatis">MyBatis风格</option>
          </select>
        </div>
        <div class="form-group">
          <label>SQL语句:</label>
          <textarea 
            v-model="sqlStatement" 
            rows="10" 
            placeholder="请输入SQL语句"
            class="form-textarea"
          ></textarea>
        </div>
        <div class="form-group" v-if="sqlType === 'mybatis'">
          <label>参数 (JSON格式):</label>
          <textarea 
            v-model="myBatisParams" 
            rows="5" 
            placeholder="例如: {&quot;name&quot;: &quot;张三&quot;, &quot;age&quot;: 25}"
            class="form-textarea"
          ></textarea>
        </div>
        <button class="btn btn-primary" @click="executeSQL" :disabled="!canExecute">执行SQL</button>
      </div>
      <div class="panel-right">
        <div class="result-header">
          <h3>执行结果</h3>
          <div class="result-stats" v-if="executionStats">
            <span>执行时间: {{ executionStats.duration }} ms</span>
            <span>影响行数: {{ executionStats.rowsAffected }}</span>
          </div>
        </div>
        <vxe-table
          v-if="queryResult && queryResult.length > 0"
          :data="queryResult"
          height="500"
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
        <div v-else-if="executionMessage" class="no-result">
          {{ executionMessage }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'

const dataSources = ref([])
const selectedDataSource = ref('')
const sqlType = ref('select')
const sqlStatement = ref('')
const myBatisParams = ref('{}')
const queryResult = ref([])
const resultColumns = ref([])
const executionMessage = ref('')
const executionStats = ref(null)

// 计算属性：判断是否可以执行
const canExecute = computed(() => {
  return selectedDataSource.value && sqlStatement.value.trim()
})

// 加载数据源
const loadDataSources = async () => {
  try {
    const response = await axios.get('/api/datasource/list')
    dataSources.value = response.data
  } catch (error) {
    console.error('加载数据源失败:', error)
  }
}

// 数据源变更事件
const onDataSourceChange = () => {
  // 重置结果
  queryResult.value = []
  resultColumns.value = []
  executionMessage.value = ''
  executionStats.value = null
}

// 执行SQL
const executeSQL = async () => {
  if (!canExecute.value) {
    alert('请选择数据源并输入SQL语句')
    return
  }

  try {
    const startTime = Date.now()
    
    let response
    if (sqlType.value === 'mybatis') {
      // 执行MyBatis风格的SQL
      const params = JSON.parse(myBatisParams.value || '{}')
      response = await axios.post('/api/query/mybatis-execute', {
        dataSourceId: selectedDataSource.value,
        myBatisSql: sqlStatement.value,
        parameters: params
      })
    } else {
      // 执行普通SQL
      response = await axios.post('/api/query/execute', {
        dataSourceId: selectedDataSource.value,
        sql: sqlStatement.value
      })
    }
    
    const endTime = Date.now()
    
    // 更新执行统计
    executionStats.value = {
      duration: endTime - startTime,
      rowsAffected: response.data.length || 0
    }
    
    // 生成列定义
    if (Array.isArray(response.data) && response.data.length > 0) {
      resultColumns.value = Object.keys(response.data[0]).map(key => ({
        field: key,
        title: key,
        width: 150
      }))
      queryResult.value = response.data
      executionMessage.value = ''
    } else {
      queryResult.value = []
      resultColumns.value = []
      executionMessage.value = response.data.message || '执行完成，无返回数据'
    }
  } catch (error) {
    console.error('执行SQL失败:', error)
    queryResult.value = []
    resultColumns.value = []
    executionMessage.value = `执行失败: ${error.response?.data || error.message}`
    executionStats.value = null
  }
}

// 打开执行对话框
const openExecuteDialog = () => {
  // 重置表单
  sqlStatement.value = ''
  myBatisParams.value = '{}'
  queryResult.value = []
  resultColumns.value = []
  executionMessage.value = ''
  executionStats.value = null
}

// 页面加载时获取数据源列表
loadDataSources()
</script>

<style scoped>
.execution-list-container {
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

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-select {
  width: 100%;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  height: 34px;
}

.form-textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  font-family: monospace;
}

.execute-panel {
  display: flex;
  gap: 20px;
}

.panel-left {
  flex: 1;
  min-width: 400px;
}

.panel-right {
  flex: 2;
  min-width: 600px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.result-stats {
  font-size: 12px;
  color: #909399;
}

.result-stats span {
  margin-left: 15px;
}

.no-result {
  padding: 20px;
  text-align: center;
  color: #909399;
}
</style>