package com.newx.ezapi.api.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 用户名
    private String username;
    
    // 密码（加密存储）
    @JsonIgnore
    private String password;
    
    // 用户角色
    private String role;
    
    // 用户状态：ACTIVE, INACTIVE, LOCKED
    private String status;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
    
    // 邮箱
    private String email;
    
    // 描述
    private String description;
}