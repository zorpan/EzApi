package com.newx.ezapi.api.auth.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.newx.ezapi.api.auth.mapper")
public class MybatisAuthConfig {
    // 配置MyBatis扫描授权模块的Mapper
}