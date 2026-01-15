package com.newx.ezapi.core.config;

import com.newx.ezapi.auth.interceptor.InternalInterceptor;
import com.newx.ezapi.gateway.interceptor.GatewayInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author panxingya
 */
@Configuration
public class WebInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private InternalInterceptor internalInterceptor;

    @Autowired
    private GatewayInterceptor gatewayInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 注册接口网关授权拦截器，应用于所有需要保护的路径
        registry.addInterceptor(gatewayInterceptor)
                .addPathPatterns("/api/call/**");  // 拦截所有API请求

        // 注册内部管理授权拦截器，应用于所有需要保护的路径
        registry.addInterceptor(internalInterceptor)
                .addPathPatterns("/**")  // 拦截所有API请求
                .excludePathPatterns("/api/call/**")
                .excludePathPatterns("/api/login")  // 排除登录接口
                .excludePathPatterns("/api/auth/**"); // 排除其他授权管理相关的路径

    }
}