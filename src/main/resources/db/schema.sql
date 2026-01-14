-- 创建数据源信息表
CREATE TABLE IF NOT EXISTS data_source_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '数据源名称',
    driver_class_name VARCHAR(255) NOT NULL COMMENT '驱动类名',
    url VARCHAR(500) NOT NULL COMMENT '连接URL',
    username VARCHAR(255) NOT NULL COMMENT '用户名',
    password VARCHAR(255) COMMENT '密码',
    db_type VARCHAR(50) COMMENT '数据库类型',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    description TEXT COMMENT '描述',
    created_time BIGINT COMMENT '创建时间戳',
    updated_time BIGINT COMMENT '更新时间戳'
) COMMENT='数据源信息表';

-- 创建API信息表
CREATE TABLE IF NOT EXISTS api_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_name VARCHAR(255) NOT NULL COMMENT 'API名称',
    api_path VARCHAR(500) NOT NULL COMMENT 'API路径',
    api_method VARCHAR(10) NOT NULL COMMENT '请求方法(GET, POST, PUT, DELETE等)',
    data_source_id VARCHAR(50) NOT NULL COMMENT '数据源ID',
    sql_content TEXT NOT NULL COMMENT 'SQL内容',
    description TEXT COMMENT '描述',
    status TINYINT(1) DEFAULT 0 COMMENT '状态(0-下线, 1-上线)',
    created_time BIGINT COMMENT '创建时间戳',
    updated_time BIGINT COMMENT '更新时间戳',
    created_by VARCHAR(100) COMMENT '创建人',
    updated_by VARCHAR(100) COMMENT '更新人'
) COMMENT='API信息表';

-- 创建API参数表
CREATE TABLE IF NOT EXISTS api_parameter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_id BIGINT NOT NULL COMMENT 'API ID',
    param_name VARCHAR(100) NOT NULL COMMENT '参数名称',
    param_type VARCHAR(50) NOT NULL COMMENT '参数类型(STRING, INTEGER, BOOLEAN, DATE等)',
    required TINYINT(1) DEFAULT 0 COMMENT '是否必填(0-否, 1-是)',
    default_value VARCHAR(500) COMMENT '默认值',
    description TEXT COMMENT '参数描述',
    validation_rule VARCHAR(500) COMMENT '验证规则',
    created_time BIGINT COMMENT '创建时间戳',
    updated_time BIGINT COMMENT '更新时间戳',
    FOREIGN KEY (api_id) REFERENCES api_info(id) ON DELETE CASCADE
) COMMENT='API参数表';

-- 创建授权令牌表
CREATE TABLE IF NOT EXISTS authorization_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_name VARCHAR(255) NOT NULL COMMENT '令牌名称',
    token_value VARCHAR(500) NOT NULL COMMENT '令牌值',
    api_id BIGINT COMMENT '关联的API ID',
    token_type VARCHAR(50) DEFAULT 'API_KEY' COMMENT '令牌类型',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '令牌状态',
    create_time DATETIME COMMENT '创建时间',
    expire_time DATETIME COMMENT '过期时间',
    description TEXT COMMENT '描述',
    enable_rate_limit TINYINT(1) DEFAULT 0 COMMENT '是否启用访问频率限制',
    rate_limit_per_hour INT DEFAULT 0 COMMENT '访问频率限制（每小时请求数）'
) COMMENT='授权令牌表';

-- 创建授权规则表
CREATE TABLE IF NOT EXISTS authorization_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(255) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型',
    api_id BIGINT COMMENT '关联的API ID',
    rule_content TEXT COMMENT '规则内容',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '规则状态',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    description TEXT COMMENT '描述'
) COMMENT='授权规则表';

-- 创建API密钥与API关联表
CREATE TABLE IF NOT EXISTS authorization_token_api (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_id BIGINT NOT NULL COMMENT 'API密钥ID',
    api_id BIGINT NOT NULL COMMENT 'API ID',
    create_time DATETIME COMMENT '创建时间',
    FOREIGN KEY (token_id) REFERENCES authorization_token(id) ON DELETE CASCADE,
    FOREIGN KEY (api_id) REFERENCES api_info(id) ON DELETE CASCADE,
    UNIQUE KEY uk_token_api (token_id, api_id)
) COMMENT='API密钥与API关联表';

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role VARCHAR(50) DEFAULT 'USER' COMMENT '用户角色',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '用户状态',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    email VARCHAR(100) COMMENT '邮箱',
    description TEXT COMMENT '描述'
) COMMENT='用户表';