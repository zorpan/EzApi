package com.newx.ezapi.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newx.ezapi.auth.util.JwtUtil;
import com.newx.ezapi.common.entity.ApiAccessLog;
import com.newx.ezapi.common.service.ApiAccessLogService;
import com.newx.ezapi.core.entity.ApiInfo;
import com.newx.ezapi.core.service.ApiInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * API访问日志拦截器
 */
@Component
public class ApiAccessLogInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiAccessLogInterceptor.class);
    
    @Autowired
    private ApiAccessLogService apiAccessLogService;
    
    @Autowired
    private ApiInfoService apiInfoService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String START_TIME_ATTRIBUTE = "apiLogStartTime";
    private static final String REQUEST_ID_ATTRIBUTE = "apiLogRequestId";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        
        // 生成请求ID
        String requestId = UUID.randomUUID().toString().replace("-", "");
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 计算执行时间
            long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 获取请求ID
            String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
            
            // 创建日志对象
            ApiAccessLog log = new ApiAccessLog();
            log.setRequestId(requestId);
            log.setCreateTime(LocalDateTime.now());
            log.setExecutionTime(executionTime);
            
            // 记录请求信息
            log.setHttpMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setClientIp(getClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
            
            // 记录请求头
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // 跳过一些敏感头信息
                if (!"Authorization".equalsIgnoreCase(headerName) && 
                    !"Cookie".equalsIgnoreCase(headerName)) {
                    headers.put(headerName, request.getHeader(headerName));
                }
            }
            log.setRequestHeaders(objectMapper.writeValueAsString(headers));
            
            // 记录请求参数
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (parameterMap != null && !parameterMap.isEmpty()) {
                Map<String, String> params = new HashMap<>();
                parameterMap.forEach((key, values) -> {
                    if (values != null && values.length > 0) {
                        params.put(key, values.length == 1 ? values[0] : String.join(",", values));
                    }
                });
                log.setRequestParams(objectMapper.writeValueAsString(params));
            }
            
            // 记录请求体（如果是 ContentCachingRequestWrapper）
            if (request instanceof com.newx.ezapi.common.wrapper.ContentCachingRequestWrapper) {
                com.newx.ezapi.common.wrapper.ContentCachingRequestWrapper wrapper = (com.newx.ezapi.common.wrapper.ContentCachingRequestWrapper) request;
                byte[] content = wrapper.getContentAsByteArray();
                if (content != null && content.length > 0) {
                    String body = new String(content, StandardCharsets.UTF_8);
                    // 限制长度，避免存储过大的请求体
                    if (body.length() > 10000) {
                        body = body.substring(0, 10000) + "... (truncated)";
                    }
                    log.setRequestBody(body);
                }
            }
            
            // 记录响应信息
            log.setResponseStatus(response.getStatus());
            
            // 记录响应体（如果是 ContentCachingResponseWrapper）
            if (response instanceof ContentCachingResponseWrapper) {
                ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
                byte[] content = wrapper.getContentAsByteArray();
                if (content != null && content.length > 0) {
                    String body = new String(content, StandardCharsets.UTF_8);
                    // 限制长度，避免存储过大的响应体
                    if (body.length() > 10000) {
                        body = body.substring(0, 10000) + "... (truncated)";
                    }
                    log.setResponseBody(body);
                }
            }
            
            // 记录错误信息
            if (ex != null) {
                log.setErrorMessage(ex.getMessage());
            }
            
            // 尝试从请求中提取用户信息
            extractUserInfo(request, log);
            
            // 尝试匹配 API 信息
            extractApiInfo(request, log);
            
            // 异步保存日志
            apiAccessLogService.saveLog(log);
            
        } catch (Exception e) {
            logger.error("记录访问日志失败", e);
        }
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 从请求中提取用户信息
     */
    private void extractUserInfo(HttpServletRequest request, ApiAccessLog log) {
        // 尝试从 JWT Token 中提取用户信息
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                if (username != null) {
                    log.setUsername(username);
                    log.setTokenValue(token);
                }
            } catch (Exception e) {
                // Token 无效，忽略
            }
        }
        
        // 尝试从 API Key 中提取
        if (log.getTokenValue() == null) {
            String apiKey = request.getHeader("X-API-Key");
            if (apiKey != null && !apiKey.isEmpty()) {
                log.setTokenValue(apiKey);
            }
        }
    }
    
    /**
     * 从请求中提取 API 信息
     */
    private void extractApiInfo(HttpServletRequest request, ApiAccessLog log) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // 对于 API 调用路径，提取 API 信息
        if (requestURI.startsWith("/api/call/")) {
            String apiPath = requestURI.substring("/api/call".length());
            log.setApiPath(apiPath);
            log.setApiMethod(method);
            
            try {
                ApiInfo apiInfo = apiInfoService.findByApiPathAndMethod(apiPath, method);
                if (apiInfo != null) {
                    log.setApiId(apiInfo.getId());
                }
            } catch (Exception e) {
                logger.warn("获取API信息失败: {}", e.getMessage());
            }
        }
    }
}
