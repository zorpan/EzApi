package com.newx.ezapi.api.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 数据源信息实体类
 */
@Data
@TableName("data_source_info")
public class DataSourceInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("driver_class_name")
    private String driverClassName;

    @TableField("url")
    private String url;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("db_type")
    private String dbType;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("description")
    private String description;

    @TableField("created_time")
    private Long createdTime;

    @TableField("updated_time")
    private Long updatedTime;
}