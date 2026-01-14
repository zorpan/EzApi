package com.newx.api.datasource.service;

import com.newx.api.datasource.entity.DataSourceConfig;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DataSourceManager {
    /**
     * 添加数据源配置
     */
    void addDataSource(DataSourceConfig config);

    /**
     * 获取数据源
     */
    DataSource getDataSource(String dataSourceId);

    /**
     * 获取所有数据源配置
     */
    List<DataSourceConfig> getAllDataSources();

    /**
     * 移除数据源
     */
    void removeDataSource(String dataSourceId);

    /**
     * 测试数据源连接
     */
    boolean testConnection(String dataSourceId) throws SQLException;

    /**
     * 获取数据源连接
     */
    Connection getConnection(String dataSourceId) throws SQLException;

    /**
     * 重新加载数据源
     */
    void reloadDataSource(String dataSourceId);

    /**
     * 加载JDBC驱动
     */
    void loadJDBCDriver(String driverClassName, String jarPath) throws Exception;
    
    /**
     * 从数据库重新加载所有数据源
     */
    void reloadAllDataSourcesFromDb();
}