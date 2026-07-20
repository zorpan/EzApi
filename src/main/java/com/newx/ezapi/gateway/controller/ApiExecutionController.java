package com.newx.ezapi.gateway.controller;

import com.newx.ezapi.auth.service.AuthorizationTokenService;
import com.newx.ezapi.common.result.Result;
import com.newx.ezapi.core.entity.ApiInfo;
import com.newx.ezapi.core.entity.ApiParameter;
import com.newx.ezapi.core.service.ApiExecuteOrchestrator;
import com.newx.ezapi.core.service.ApiInfoService;
import com.newx.ezapi.core.service.ApiParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/call")
public class ApiExecutionController {

    @Autowired
    private ApiInfoService apiInfoService;

    @Autowired
    private ApiParameterService apiParameterService;

    @Autowired
    private ApiExecuteOrchestrator apiExecuteOrchestrator;
    
    @Autowired
    private AuthorizationTokenService authorizationTokenService;

    /**
     * 动态GET API
     */
    @GetMapping("/{path}")
    public Result<Object> dynamicGetApi(
            HttpServletRequest request,
            @PathVariable String path,
            @RequestParam Map<String, String> queryParams) {
        return executeDynamicApi(request, "GET", "/" + path, queryParams);
    }

    /**
     * 动态POST API
     */
    @PostMapping("/{path}")
    public Result<Object> dynamicPostApi(
            HttpServletRequest request,
            @PathVariable String path,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        Map<String, String> params = new HashMap<>();
        if (requestBody != null) {
            for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
                params.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return executeDynamicApi(request, "POST", "/" + path, params);
    }

    /**
     * 动态PUT API
     */
    @PutMapping("/{path}")
    public Result<Object> dynamicPutApi(
            HttpServletRequest request,
            @PathVariable String path,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, String> params = new HashMap<>();
        if (requestBody != null) {
            for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
                params.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return executeDynamicApi(request, "PUT", "/" + path, params);
    }

    /**
     * 动态DELETE API
     */
    @DeleteMapping("/{path}")
    public Result<Object> dynamicDeleteApi(
            HttpServletRequest request,
            @PathVariable String path,
            @RequestParam Map<String, String> queryParams) {
        return executeDynamicApi(request, "DELETE", "/" + path, queryParams);
    }

    /**
     * 执行动态API
     */
    private Result<Object> executeDynamicApi(HttpServletRequest request, String method, String path, Map<String, String> params) {
        try {
            // 查找匹配的API
            ApiInfo apiInfo = apiInfoService.findByApiPathAndMethod(path, method);
            if (apiInfo == null) {
                return Result.error(404, "API不存在: " + path);
            }

            // 检查API是否上线
            if (apiInfo.getStatus() != 1) {
                return Result.error(404, "API未上线");
            }

            // 授权检查
            if (!checkAuthorization(request, apiInfo.getId())) {
                return Result.error(401, "Unauthorized: Access denied");
            }

            // 获取API参数定义
            List<ApiParameter> apiParameters = apiParameterService.getParametersByApiId(apiInfo.getId());

            // 验证参数
            for (ApiParameter apiParam : apiParameters) {
                if (apiParam.getRequired() != null && apiParam.getRequired() && !params.containsKey(apiParam.getParamName())) {
                    return Result.error(400, "缺少必需参数: " + apiParam.getParamName());
                }
            }

            // 参数验证
            for (Map.Entry<String, String> paramEntry : params.entrySet()) {
                String paramName = paramEntry.getKey();
                String paramValue = paramEntry.getValue();

                ApiParameter apiParam = null;
                for (ApiParameter p : apiParameters) {
                    if (p.getParamName().equals(paramName)) {
                        apiParam = p;
                        break;
                    }
                }

                if (apiParam != null) {
                    // 验证参数类型
                    if (!validateParameter(apiParam, paramValue)) {
                        return Result.error(400, "参数类型错误: " + paramName);
                    }

                    // 验证参数规则
                    if (apiParam.getValidationRule() != null && !apiParam.getValidationRule().isEmpty()) {
                        if (!validateParameterRule(apiParam.getValidationRule(), paramValue)) {
                            return Result.error(400, "参数验证失败: " + paramName);
                        }
                    }
                }
            }

            // 将字符串参数转换为适当的类型
            Map<String, Object> convertedParams = convertParameters(params, apiParameters);

            // 由编排器根据 protocolType 路由到对应的协议执行器
            Object result = apiExecuteOrchestrator.execute(apiInfo, convertedParams);

            // 如果使用了令牌，增加访问计数
            String tokenValue = extractTokenFromRequest(request);
            if (tokenValue != null) {
                authorizationTokenService.incrementAccessCount(tokenValue, apiInfo.getId());
            }

            return Result.success(result);
        } catch (Exception e) {
            String message = e.getMessage();
            // 根据异常类型给出友好的错误提示
            if (message != null && message.contains("WebService")) {
                return Result.error(502, "WebService调用错误: " + message);
            }
            return Result.error(500, "API执行错误: " + message);
        }
    }

    /**
     * 检查请求授权
     */
    private boolean checkAuthorization(HttpServletRequest request, Long apiId) {
        // 从请求中提取令牌
        String tokenValue = extractTokenFromRequest(request);
        
        if (tokenValue != null && !tokenValue.isEmpty()) {
            // 验证令牌是否有效
            if (authorizationTokenService.isValidToken(tokenValue)) {
                // 检查令牌是否有访问此API的权限
                return authorizationTokenService.hasAccessPermission(tokenValue, apiId);
            }
        }
        
        // 如果没有提供令牌，或者令牌无效，拒绝访问
        // 注意：在实际应用中，可以根据API配置决定是否需要授权
        return false;
    }
    
    /**
     * 从请求中提取令牌
     */
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

    /**
     * 验证参数类型
     */
    private boolean validateParameter(ApiParameter apiParam, String value) {
        if (value == null) {
            return !apiParam.getRequired(); // 如果是可选参数，null值是允许的
        }

        String paramType = apiParam.getParamType();
        switch (paramType.toUpperCase()) {
            case "INTEGER":
                try {
                    Integer.parseInt(value);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "BOOLEAN":
                return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
            case "DATE":
                // 简单的日期格式验证 (YYYY-MM-DD)
                Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
                return datePattern.matcher(value).matches();
            default:
                return true; // STRING类型或其他类型默认通过
        }
    }

    /**
     * 验证参数规则
     */
    private boolean validateParameterRule(String rule, String value) {
        // 简单的正则表达式验证规则
        try {
            Pattern pattern = Pattern.compile(rule);
            Matcher matcher = pattern.matcher(value);
            return matcher.matches();
        } catch (Exception e) {
            // 如果规则无效，则跳过验证
            return true;
        }
    }

    /**
     * 将字符串参数转换为适当的类型
     */
    private Map<String, Object> convertParameters(Map<String, String> params, List<ApiParameter> apiParameters) {
        Map<String, Object> convertedParams = new HashMap<>();
        
        for (Map.Entry<String, String> paramEntry : params.entrySet()) {
            String paramName = paramEntry.getKey();
            String paramValue = paramEntry.getValue();
            
            // 查找参数定义以确定类型
            ApiParameter apiParam = null;
            for (ApiParameter p : apiParameters) {
                if (p.getParamName().equals(paramName)) {
                    apiParam = p;
                    break;
                }
            }
            
            // 根据参数类型转换值
            if (apiParam != null) {
                convertedParams.put(paramName, convertParameterValue(paramValue, apiParam.getParamType()));
            } else {
                // 如果没有定义参数类型，默认作为字符串处理
                convertedParams.put(paramName, paramValue);
            }
        }
        
        return convertedParams;
    }
    
    /**
     * 根据参数类型转换参数值
     */
    private Object convertParameterValue(String value, String paramType) {
        if (value == null || paramType == null) {
            return value;
        }
        
        switch (paramType.toUpperCase()) {
            case "INTEGER":
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return value; // 如果转换失败，返回原始值
                }
            case "BOOLEAN":
                return Boolean.parseBoolean(value);
            case "DATE":
                return value; // 日期类型按字符串处理
            default:
                return value; // 默认为字符串类型
        }
    }
}