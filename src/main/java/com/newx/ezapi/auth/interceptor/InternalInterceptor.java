package com.newx.ezapi.auth.interceptor;

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
public class InternalInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthorizationTokenService tokenService;

    @Autowired
    private ApiInfoService apiInfoService;

    @Autowired
    private JwtUtil jwtUtil;

    // 不需要认证的路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
        "/api/call",
        "/api/auth/login",
        "/api/auth/user/create",
        "/api/auth/token/validate",
        "/api/database-driver",
        "/api/system/upload"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // 检查是否在排除路径中
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestURI.startsWith(excludePath)) {
                return true; // 不需要拦截
            }
        }

        return handleManagementAuthorization(request, response);

    }

    /**
     * 处理管理API认证（用户认证）
     */
    private boolean handleManagementAuthorization(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            if (username != null) {
                // 验证JWT Token是否有效
                if (jwtUtil.validateToken(token, username)) {
                    // Token有效，允许访问
                    return true;
                }
            }
        }

        // 没有有效的认证信息，返回未授权
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write("{\"error\":\"Unauthorized: Please log in\"}");
        return false;
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
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        // 从请求参数中提取令牌
        if (token == null) {
            token = request.getParameter("token");
        }

        // 从请求头X-API-Key中提取令牌
        if (token == null) {
            token = request.getHeader("X-API-Key");
        }

        return token;
    }
}