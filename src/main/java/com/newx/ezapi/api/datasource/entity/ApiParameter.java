package com.newx.ezapi.api.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * API参数实体类
 */
@Data
@TableName("api_parameter")
public class ApiParameter {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("api_id")
    private Long apiId;

    @TableField("param_name")
    private String paramName;

    @TableField("param_type")
    private String paramType; // STRING, INTEGER, BOOLEAN, DATE等

    @TableField("required")
    private Boolean required; // 是否必填

    @TableField("default_value")
    private String defaultValue;

    @TableField("description")
    private String description;

    @TableField("validation_rule")
    private String validationRule; // 验证规则

    @TableField("created_time")
    private Long createdTime;

    @TableField("updated_time")
    private Long updatedTime;
}