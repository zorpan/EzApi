package com.newx.ezapi.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 数据库驱动实体类
 */
@Data
@TableName("database_driver")
public class DatabaseDriver {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("driver_name")
    private String driverName;

    @TableField("driver_version")
    private String driverVersion;

    @TableField("driver_description")
    private String driverDescription;

    @TableField("driver_file_name")
    private String driverFileName;

    @TableField("driver_file_path")
    private String driverFilePath;

    @TableField("driver_class_name")
    private String driverClassName;

    @TableField("example_jdbc_url")
    private String exampleJdbcUrl;

    @TableField("supported_db_types")
    private String supportedDbTypes;

    @TableField("is_active")
    private Boolean isActive;

    @TableField("created_time")
    private Long createdTime;

    @TableField("updated_time")
    private Long updatedTime;
}