package com.newx.ezapi.api.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API密钥与API关联实体
 */
@Data
@TableName("authorization_token_api")
public class TokenApiRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // API密钥ID
    private Long tokenId;
    
    // API ID
    private Long apiId;
    
    // 创建时间
    private LocalDateTime createTime;
}