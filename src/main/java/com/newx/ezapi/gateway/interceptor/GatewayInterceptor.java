package com.newx.ezapi.gateway.interceptor;

import com.newx.ezapi.auth.service.AuthorizationTokenService;
import com.newx.ezapi.auth.util.JwtUtil;
import com.newx.ezapi.core.entity.ApiInfo;
import com.newx.ezapi.core.service.ApiInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 *
 * 内部专用拦截器, 用于系统管理
 * @author panxingya
 */
@Component
public class GatewayInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthorizationTokenService tokenService;

    @Autowired
    private ApiInfoService apiInfoService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // 检查是否是API执行路径（需要API令牌认证）
        if (requestURI.startsWith("/api/call/")) {
            return handleApiCallAuthorization(request, response);
        }
        // 其他路径默认通过（前端路由由前端处理）
        return true;
    }

    /**
     * 处理API调用认证（API令牌认证）
     */
    private boolean handleApiCallAuthorization(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 提取API路径（去掉 /api/call/ 前缀）
        String apiPath = requestURI.substring("/api/call".length());

        // 根据路径和方法查找API信息
        ApiInfo apiInfo = apiInfoService.findByApiPathAndMethod(apiPath, method);
        if (apiInfo == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.getWriter().write("{\"error\":\"API not found\"}");
            return false;
        }

        // 验证授权
        if (!validateApiTokenAuthorization(request, apiInfo.getId())) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("{\"error\":\"Unauthorized: Access denied\"}");
            return false;
        }

        // 检查速率限制
        String tokenValue = extractTokenFromRequest(request);
        if (tokenValue != null && !tokenService.checkRateLimit(tokenValue, apiInfo.getId())) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return false;
        }

        return true;
    }


    /**
     * 验证API令牌授权
     */
    private boolean validateApiTokenAuthorization(HttpServletRequest request, Long apiId) {
        // 从请求头或参数中提取令牌
        String tokenValue = extractTokenFromRequest(request);

        if (tokenValue != null && !tokenValue.isEmpty()) {
            // 验证令牌是否有效
            if (tokenService.isValidToken(tokenValue)) {
                // 检查令牌是否有访问此API的权限
                return tokenService.hasAccessPermission(tokenValue, apiId);
            }
        }
        return false; // 默认情况下，没有有效令牌不允许访问
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        // 从请求头中提取令牌
        String token = request.getHeader("X-API-Key");

        // 从请求参数中提取令牌
        if (token == null) {
            token = request.getParameter("token");
        }

        return token;
    }
}