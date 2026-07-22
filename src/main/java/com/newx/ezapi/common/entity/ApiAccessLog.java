package com.newx.ezapi.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API访问日志实体类
 */
@Data
@TableName("api_access_log")
public class ApiAccessLog {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("request_id")
    private String requestId; // 请求ID（UUID，用于追踪）
    
    @TableField("api_id")
    private Long apiId; // API ID
    
    @TableField("api_path")
    private String apiPath; // API路径
    
    @TableField("api_method")
    private String apiMethod; // 请求方法
    
    @TableField("http_method")
    private String httpMethod; // HTTP请求方法
    
    @TableField("request_url")
    private String requestUrl; // 请求URL
    
    @TableField("request_headers")
    private String requestHeaders; // 请求头（JSON格式）
    
    @TableField("request_params")
    private String requestParams; // 请求参数（JSON格式）
    
    @TableField("request_body")
    private String requestBody; // 请求体
    
    @TableField("response_status")
    private Integer responseStatus; // 响应状态码
    
    @TableField("response_body")
    private String responseBody; // 响应体
    
    @TableField("client_ip")
    private String clientIp; // 客户端IP
    
    @TableField("user_agent")
    private String userAgent; // 用户代理
    
    @TableField("token_value")
    private String tokenValue; // 使用的令牌
    
    @TableField("user_id")
    private Long userId; // 用户ID
    
    @TableField("username")
    private String username; // 用户名
    
    @TableField("execution_time")
    private Long executionTime; // 执行时间（毫秒）
    
    @TableField("error_message")
    private String errorMessage; // 错误信息
    
    @TableField("create_time")
    private LocalDateTime createTime; // 创建时间
}
