package com.newx.ezapi.api.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 授权规则实体
 */
@Data
@TableName("authorization_rule")
public class AuthorizationRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 规则名称
    private String ruleName;
    
    // 规则类型：IP_WHITELIST, ROLE_BASED, TOKEN_REQUIRED等
    private String ruleType;
    
    // 关联的API ID，如果为空则表示全局规则
    private Long apiId;
    
    // 规则内容（JSON格式存储具体规则）
    private String ruleContent;
    
    // 规则状态：ACTIVE, INACTIVE
    private String status;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
    
    // 描述
    private String description;
}