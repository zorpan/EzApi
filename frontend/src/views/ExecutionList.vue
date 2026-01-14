<template>
  <div class="execution-list-container">
    <div class="toolbar">
      <button class="btn btn-primary" @click="openExecuteDialog()">执行SQL</button>
    </div>

    <div class="execute-panel">
      <div class="panel-left">
        <div class="form-group">
          <label>选择数据源</label>
          <select v-model="selectedDataSource" class="form-select" @change="onDataSourceChange">
            <option value="">请选择数据源</option>
            <option v-for="ds in dataSources" :key="ds.id" :value="ds.id">{{ ds.name }}</option>
          </select>
        </div>

        <div class="form-group">
          <label>SQL类型</label>
          <select v-model="sqlType">
            <option value="select">SELECT</option>
            <option value="insert">INSERT</option>
            <option value="update">UPDATE</option>
            <option value="delete">DELETE</option>
          </select>
        </div>

        <div class="form-group">
          <label>SQL语句</label>
          <textarea 
            v-model="sqlStatement" 
            rows="8" 
            placeholder="请输入SQL语句"
            class="form-textarea"
          ></textarea>
        </div>

        <div class="form-group" v-if="sqlType !== 'select'">
          <label>参数 (JSON格式)</label>
          <textarea 
            v-model="myBatisParams" 
            rows="4" 
            placeholder="例如: {&quot;id&quot;: 1, &quot;name&quot;: &quot;test&quot;}"
            class="form-textarea"
          ></textarea>
        </div>

        <button 
          class="btn btn-primary" 
          @click="executeSQL" 
          :disabled="!canExecute"
          style="width: 100%; padding: 12px; font-size: 16px;"
        >
          执行SQL
        </button>
      </div>

      <div class="panel-right">
        <div class="result-header">
          <h3>执行结果</h3>
          <div class="result-stats" v-if="executionStats">
            <span>耗时: {{ executionStats.duration }}ms</span>
            <span>影响行数: {{ executionStats.rowsAffected }}</span>
          </div>
        </div>

        <div v-if="executionMessage" class="alert" :class="executionMessage.includes('成功') ? 'alert-success' : 'alert-error'">
          {{ executionMessage }}
        </div>

        <div v-if="queryResult.length === 0" class="no-result">
          <p>暂无执行结果</p>
          <p>请选择数据源并输入SQL语句后点击执行</p>
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
import { dataSourceApi } from '../api'

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
    const response = await dataSourceApi.getAllDataSources()
    // 适配新的Result响应结构
    dataSources.value = response.data.data || []
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
    // 解析参数
    let params = {}
    if (myBatisParams.value.trim()) {
      try {
        params = JSON.parse(myBatisParams.value)
      } catch (e) {
        alert('参数JSON格式错误: ' + e.message)
        return
      }
    }

    // 准备请求数据
    const requestData = {
      sql: sqlStatement.value,
      ...params
    }

    let response
    if (['insert', 'update', 'delete'].includes(sqlType.value.toLowerCase())) {
      // 执行更新操作
      response = await dataSourceApi.executeUpdate(selectedDataSource.value, requestData)
    } else {
      // 执行查询操作
      response = await dataSourceApi.executeQuery(selectedDataSource.value, requestData)
    }
    if (response.data.code === 200) {
      const resultData = response.data.data
      
      if (Array.isArray(resultData)) {
        // 查询结果
        queryResult.value = resultData
        if (resultData.length > 0) {
          // 生成列配置
          resultColumns.value = Object.keys(resultData[0]).map(key => ({
            field: key,
            title: key,
            minWidth: 100
          }))
        } else {
          resultColumns.value = []
        }
        executionMessage.value = `查询成功，共返回 ${resultData.length} 条记录`
      } else {
        // 更新结果
        executionMessage.value = `执行成功，影响 ${resultData.affectedRows} 行`
      }
      
      // 记录执行统计
      executionStats.value = {
        duration: new Date().getTime() - Date.now(), // 简化的耗时计算
        rowsAffected: resultData.affectedRows || resultData.length || 0
      }
    } else {
      executionMessage.value = '执行失败: ' + response.data.message
    }
  } catch (error) {
    console.error('执行SQL失败:', error)
    executionMessage.value = '执行失败: ' + (error.response?.data?.message || error.message)
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

.alert {
  padding: 10px;
  margin-bottom: 15px;
  border-radius: 4px;
}

.alert-success {
  background-color: #f0f9ff;
  border: 1px solid #409eff;
  color: #409eff;
}

.alert-error {
  background-color: #fef0f0;
  border: 1px solid #f56c6c;
  color: #f56c6c;
}
</style>