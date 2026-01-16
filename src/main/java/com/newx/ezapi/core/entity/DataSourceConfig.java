package com.newx.ezapi.core.entity;

import lombok.Data;

@Data
public class DataSourceConfig {
    private Long id;
    private String name;
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private String dbType;
    private Boolean enabled;
    private Long driverId;

    // 构造函数
    public DataSourceConfig() {
    }

    public DataSourceConfig(Long id, String name, String driverClassName, String url,
                            String username, String password, String dbType) {
        this.id = id;
        this.name = name;
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
        this.dbType = dbType;
        this.enabled = true;
    }
}
