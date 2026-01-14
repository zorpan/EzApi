package com.newx.api.auth.config;

import com.newx.api.auth.interceptor.AuthorizationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthWebConfig implements WebMvcConfigurer {
    
    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册授权拦截器，应用于所有需要保护的路径
        registry.addInterceptor(authorizationInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有API请求
                .excludePathPatterns("/api/login")  // 排除登录接口
                .excludePathPatterns("/api/auth/**"); // 排除其他授权管理相关的路径
    }
}