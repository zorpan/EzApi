package com.newx.ezapi.core.entity;

public class DataSourceConfig {
    private Long id;
    private String name;
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private String dbType;
    private Boolean enabled;

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

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}