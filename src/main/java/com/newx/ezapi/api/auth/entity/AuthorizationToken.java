package com.newx.ezapi.api.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 授权令牌实体
 */
@Data
@TableName("authorization_token")
public class AuthorizationToken {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 令牌名称
    private String tokenName;
    
    // 令牌值
    private String tokenValue;
    
    // 令牌类型：API_KEY, JWT, BASIC_AUTH等
    private String tokenType;
    
    // 令牌状态：ACTIVE, INACTIVE, EXPIRED
    private String status;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 过期时间
    private LocalDateTime expireTime;
    
    // 描述
    private String description;
    
    // 是否启用访问频率限制
    private Boolean enableRateLimit;
    
    // 访问频率限制（每小时请求数）
    private Integer rateLimitPerHour;
    
    // 关联的API IDs（非数据库字段，用于传输）
    @TableField(exist = false)
    private List<Long> apiIds;
}