<template>
  <nav class="navigation">
    <ul class="nav-list">
      <li class="nav-item">
        <router-link to="/datasources" class="nav-link">数据源管理</router-link>
      </li>
      <li class="nav-item">
        <router-link to="/executions" class="nav-link">SQL执行</router-link>
      </li>
      <li class="nav-item">
        <router-link to="/apis" class="nav-link">接口管理</router-link>
      </li>
      <li class="nav-item">
        <router-link to="/auth" class="nav-link">授权管理</router-link>
      </li>
      <li class="nav-item">
        <router-link to="/rules" class="nav-link">授权规则管理</router-link>
      </li>
      <li class="nav-item">
        <router-link to="/users" class="nav-link">用户管理</router-link>
      </li>
      <li class="nav-item">
        <router-link to="/profile" class="nav-link">个人中心</router-link>
      </li>
    </ul>
  </nav>
</template>

<script setup>
// Navigation 组件逻辑
</script>

<style scoped>
.navigation {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.nav-list {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-item {
  position: relative;
}

.nav-link {
  display: block;
  padding: 16px 24px;
  color: #303133;
  text-decoration: none;
  transition: color .3s;
}

.nav-link:hover {
  color: #409EFF;
}

.router-link-active,
.router-link-exact-active {
  color: #409EFF;
  font-weight: 500;
}

.router-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #409EFF;
}
</style>