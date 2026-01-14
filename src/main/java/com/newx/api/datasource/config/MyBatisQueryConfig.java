package com.newx.api.datasource.config;

import com.newx.api.datasource.service.MyBatisQueryService;
import com.newx.api.datasource.service.impl.MyBatisBasedQueryServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MyBatisQueryConfig {

    /**
     * 配置主要的MyBatis查询服务实现，使用真正的MyBatis框架
     */
    @Bean
    @Primary
    public MyBatisQueryService myBatisQueryService(MyBatisBasedQueryServiceImpl impl) {
        return impl;
    }
}