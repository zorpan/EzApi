# 授权管理模块

## 概述

为了增强API的安全性，我们实现了全面的授权管理模块，提供多种授权验证方法，包括API密钥、IP白名单、用户角色和访问频率限制。

## 功能特性

### 1. API密钥授权
- 支持创建和管理API密钥
- 可以为特定API设置专属密钥
- 支持通用密钥访问所有API

### 2. IP白名单授权
- 支持配置IP白名单
- 限制仅允许特定IP地址访问API

### 3. 用户角色授权
- 基于用户角色的权限控制
- 支持多角色配置

### 4. 访问频率限制
- 控制每个令牌的API调用频率
- 防止API滥用

## 数据模型

### AuthorizationToken（授权令牌）
- `id`: 令牌ID
- `tokenName`: 令牌名称
- `tokenValue`: 令牌值
- `apiId`: 绑定的API ID（可选）
- `tokenType`: 令牌类型
- `status`: 令牌状态
- `enableRateLimit`: 是否启用频率限制
- `rateLimitPerHour`: 每小时访问限制

### AuthorizationRule（授权规则）
- `id`: 规则ID
- `ruleName`: 规则名称
- `ruleType`: 规则类型（IP_WHITELIST, ROLE_BASED等）
- `apiId`: 绑定的API ID（可选）
- `ruleContent`: 规则内容（JSON格式）

### User（用户）
- `id`: 用户ID
- `username`: 用户名
- `password`: 密码（加密存储）
- `role`: 用户角色

## API接口

### 授权管理接口
- `POST /api/auth/token/create` - 创建API密钥
- `GET /api/auth/token/{id}` - 获取API密钥详情
- `GET /api/auth/tokens/api/{apiId}` - 获取API相关密钥
- `DELETE /api/auth/token/{id}` - 删除API密钥
- `POST /api/auth/rule/create` - 创建授权规则
- `GET /api/auth/rule/{id}` - 获取授权规则详情
- `GET /api/auth/rules/api/{apiId}` - 获取API相关规则
- `DELETE /api/auth/rule/{id}` - 删除授权规则
- `POST /api/auth/user/create` - 创建用户
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/token/validate` - 验证令牌

### API调用接口
- `GET /api/call/{path}` - GET请求调用API
- `POST /api/call/{path}` - POST请求调用API
- `PUT /api/call/{path}` - PUT请求调用API
- `DELETE /api/call/{path}` - DELETE请求调用API

## 使用方法

### 1. 创建API密钥

```bash
curl -X POST http://localhost:8080/api/auth/token/create \
  -H "Content-Type: application/json" \
  -d '{
    "tokenName": "My API Key",
    "tokenType": "API_KEY",
    "status": "ACTIVE",
    "description": "Key for accessing my APIs",
    "enableRateLimit": true,
    "rateLimitPerHour": 1000
  }'
```

### 2. 调用受保护的API

使用以下任一方式提供API密钥：

#### 方式1：Authorization头
```bash
curl -X GET http://localhost:8080/api/call/my-api \
  -H "Authorization: Bearer YOUR_TOKEN_VALUE"
```

#### 方式2：X-API-Key头
```bash
curl -X GET http://localhost:8080/api/call/my-api \
  -H "X-API-Key: YOUR_TOKEN_VALUE"
```

#### 方式3：URL参数
```bash
curl -X GET "http://localhost:8080/api/call/my-api?token=YOUR_TOKEN_VALUE"
```

### 3. 创建IP白名单规则

```bash
curl -X POST http://localhost:8080/api/auth/rule/create \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "IP Whitelist",
    "ruleType": "IP_WHITELIST",
    "apiId": 1,
    "ruleContent": "{\"whitelist\": \"192.168.1.100,10.0.0.50\"}",
    "status": "ACTIVE",
    "description": "Allow only specific IPs"
  }'
```

### 4. 创建角色权限规则

```bash
curl -X POST http://localhost:8080/api/auth/rule/create \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "Admin Role Access",
    "ruleType": "ROLE_BASED",
    "apiId": 1,
    "ruleContent": "{\"allowedRoles\": \"ADMIN,MANAGER\"}",
    "status": "ACTIVE",
    "description": "Allow only admin roles"
  }'
```

## 配置

在`application.properties`中可以配置以下参数：

```properties
# 数据库初始化
spring.datasource.initialization-mode=always
spring.datasource.schema=classpath:db/schema.sql
spring.jpa.hibernate.ddl-auto=update
```

## 安全考虑

1. 所有敏感数据（如密码）都会进行加密存储
2. API密钥在数据库中也进行了适当保护
3. 支持HTTPS传输加密
4. 实现了访问频率限制防止滥用
5. 详细的日志记录用于安全审计

## 扩展性

该模块设计具有良好的扩展性，可以轻松添加新的授权方式，例如：
- OAuth2集成
- JWT令牌支持
- 多因子认证
- 时间窗口访问控制