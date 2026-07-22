package com.newx.ezapi.core.config;

import com.newx.ezapi.auth.interceptor.InternalInterceptor;
import com.newx.ezapi.common.interceptor.ApiAccessLogInterceptor;
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
    
    @Autowired
    private ApiAccessLogInterceptor apiAccessLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        
        // 注册API访问日志拦截器（最先执行）
        registry.addInterceptor(apiAccessLogInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/login");

        // 注册接口网关授权拦截器，应用于所有需要保护的路径
        registry.addInterceptor(gatewayInterceptor)
                .addPathPatterns("/api/call/**");  // 拦截所有API请求

        // 注册内部管理授权拦截器，仅作用于后端管理接口前缀
        // 收窄到 /api/** 和 /sys/** 是为了让打包进 jar 的前端静态资源（/、/index.html、/assets/** 等）
        // 不被拦截器拦截而返回 401
        registry.addInterceptor(internalInterceptor)
                .addPathPatterns("/api/**", "/sys/**")
                .excludePathPatterns("/api/call/**")
                .excludePathPatterns("/api/login")  // 排除登录接口
                .excludePathPatterns("/api/auth/**"); // 排除其他授权管理相关的路径

    }
}