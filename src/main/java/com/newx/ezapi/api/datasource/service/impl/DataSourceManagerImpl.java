package com.newx.ezapi.api.datasource.service.impl;

import com.newx.ezapi.api.datasource.entity.DataSourceConfig;
import com.newx.ezapi.api.datasource.entity.DataSourceInfo;
import com.newx.ezapi.api.datasource.service.DataSourceInfoService;
import com.newx.ezapi.api.datasource.service.DataSourceManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DataSourceManagerImpl implements DataSourceManager {
    
    @Autowired
    private DataSourceInfoService dataSourceInfoService;
    
    // 存储实际数据源
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
    // 存储JDBC驱动类加载器
    private final Map<String, ClassLoader> driverClassLoaders = new ConcurrentHashMap<>();

    @Override
    public void addDataSource(DataSourceConfig config) {
        // 如果数据源已存在，先移除
        if (dataSources.containsKey(config.getId())) {
            removeDataSource(config.getId());
        }
        
        DataSourceInfo dataSourceInfo;
        // 检查是否为更新操作
        if (config.getId() != null) {
            try {
                Long id = Long.valueOf(config.getId());
                // 尝试获取现有的数据源信息
                DataSourceInfo existingInfo = dataSourceInfoService.getDataSourceInfoById(id);
                if (existingInfo != null) {
                    // 更新现有记录
                    existingInfo.setName(config.getName());
                    existingInfo.setDriverClassName(config.getDriverClassName());
                    existingInfo.setUrl(config.getUrl());
                    existingInfo.setUsername(config.getUsername());
                    existingInfo.setPassword(config.getPassword());
                    existingInfo.setDbType(config.getDbType());
                    existingInfo.setEnabled(config.getEnabled());
                    dataSourceInfo = existingInfo;
                } else {
                    // 如果ID不存在，创建新记录但不指定ID，让数据库自动生成
                    dataSourceInfo = new DataSourceInfo();
                    dataSourceInfo.setName(config.getName());
                    dataSourceInfo.setDriverClassName(config.getDriverClassName());
                    dataSourceInfo.setUrl(config.getUrl());
                    dataSourceInfo.setUsername(config.getUsername());
                    dataSourceInfo.setPassword(config.getPassword());
                    dataSourceInfo.setDbType(config.getDbType());
                    dataSourceInfo.setEnabled(config.getEnabled());
                }
            } catch (NumberFormatException e) {
                // 如果ID不是数字，创建新记录
                dataSourceInfo = new DataSourceInfo();
                dataSourceInfo.setName(config.getName());
                dataSourceInfo.setDriverClassName(config.getDriverClassName());
                dataSourceInfo.setUrl(config.getUrl());
                dataSourceInfo.setUsername(config.getUsername());
                dataSourceInfo.setPassword(config.getPassword());
                dataSourceInfo.setDbType(config.getDbType());
                dataSourceInfo.setEnabled(config.getEnabled());
            }
        } else {
            // 新增操作
            dataSourceInfo = new DataSourceInfo();
            dataSourceInfo.setName(config.getName());
            dataSourceInfo.setDriverClassName(config.getDriverClassName());
            dataSourceInfo.setUrl(config.getUrl());
            dataSourceInfo.setUsername(config.getUsername());
            dataSourceInfo.setPassword(config.getPassword());
            dataSourceInfo.setDbType(config.getDbType());
            dataSourceInfo.setEnabled(config.getEnabled());
        }
        
        boolean saved = dataSourceInfoService.saveDataSourceInfo(dataSourceInfo);
        if (!saved) {
            throw new RuntimeException("Failed to save data source to database");
        }
        
        // 创建数据源
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
//        hikariConfig.setDriverClassName(config.getDriverClassName());
        
        // 设置连接池参数
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setLeakDetectionThreshold(60000);
        
        try {
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            // 使用数据库生成的ID作为key
            dataSources.put(String.valueOf(dataSourceInfo.getId()), dataSource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create data source: " + e.getMessage(), e);
        }
    }

    @Override
    public DataSource getDataSource(String dataSourceId) {
        return dataSources.get(dataSourceId);
    }

    @Override
    public List<DataSourceConfig> getAllDataSources() {
        List<DataSourceInfo> dataSourceInfos = dataSourceInfoService.getAllDataSourceInfos();
        List<DataSourceConfig> configs = new ArrayList<>();
        
        for (DataSourceInfo info : dataSourceInfos) {
            DataSourceConfig config = new DataSourceConfig();
            config.setId(String.valueOf(info.getId()));
            config.setName(info.getName());
            config.setDriverClassName(info.getDriverClassName());
            config.setUrl(info.getUrl());
            config.setUsername(info.getUsername());
            config.setPassword(info.getPassword());
            config.setDbType(info.getDbType());
            config.setEnabled(info.getEnabled() != null && info.getEnabled());
            configs.add(config);
        }
        
        return configs;
    }

    @Override
    public void removeDataSource(String dataSourceId) {
        DataSource dataSource = dataSources.remove(dataSourceId);
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
        
        // 从数据库删除
        try {
            Long id = Long.valueOf(dataSourceId);
            dataSourceInfoService.deleteDataSourceInfo(id);
        } catch (NumberFormatException e) {
            // 如果ID不是数字，忽略数据库删除
        }
        
        driverClassLoaders.remove(dataSourceId);
    }

    @Override
    public boolean testConnection(String dataSourceId) throws SQLException {
        DataSource dataSource = getDataSource(dataSourceId);
        if (dataSource == null) {
            throw new RuntimeException("Data source not found: " + dataSourceId);
        }
        
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        }
    }

    @Override
    public Connection getConnection(String dataSourceId) throws SQLException {
        DataSource dataSource = getDataSource(dataSourceId);
        if (dataSource == null) {
            throw new RuntimeException("Data source not found: " + dataSourceId);
        }
        return dataSource.getConnection();
    }

    @Override
    public void reloadDataSource(String dataSourceId) {
        try {
            Long id = Long.valueOf(dataSourceId);
            DataSourceInfo dataSourceInfo = dataSourceInfoService.getDataSourceInfoById(id);
            if (dataSourceInfo != null) {
                removeDataSource(dataSourceId);
                
                // 从数据库信息创建配置对象
                DataSourceConfig config = new DataSourceConfig();
                config.setId(String.valueOf(dataSourceInfo.getId()));
                config.setName(dataSourceInfo.getName());
                config.setDriverClassName(dataSourceInfo.getDriverClassName());
                config.setUrl(dataSourceInfo.getUrl());
                config.setUsername(dataSourceInfo.getUsername());
                config.setPassword(dataSourceInfo.getPassword());
                config.setDbType(dataSourceInfo.getDbType());
                config.setEnabled(dataSourceInfo.getEnabled() != null && dataSourceInfo.getEnabled());
                
                addDataSource(config);
            }
        } catch (NumberFormatException e) {
            // 如果ID不是数字，无法从数据库加载
        }
    }

    @Override
    public void loadJDBCDriver(String driverClassName, String jarPath) throws Exception {
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            throw new RuntimeException("JAR file does not exist: " + jarPath);
        }
        
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, 
                                                         Thread.currentThread().getContextClassLoader());
        
        // 尝试加载驱动类以验证
        Class<?> driverClass = classLoader.loadClass(driverClassName);
        if (driverClass == null) {
            throw new RuntimeException("Could not load driver class: " + driverClassName);
        }
        
        // 保存类加载器以便后续使用
        driverClassLoaders.put(driverClassName, classLoader);
    }
    
    @Override
    public void reloadAllDataSourcesFromDb() {
        // 先关闭所有现有的数据源连接
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            DataSource dataSource = entry.getValue();
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        }
        
        // 清空现有缓存
        dataSources.clear();
        
        // 从数据库加载所有数据源配置并重新创建
        List<DataSourceInfo> dataSourceInfos = dataSourceInfoService.getAllDataSourceInfos();
        for (DataSourceInfo info : dataSourceInfos) {
            if (info.getEnabled() != null && info.getEnabled()) {  // 只加载启用的数据源
                DataSourceConfig config = new DataSourceConfig();
                config.setId(String.valueOf(info.getId()));
                config.setName(info.getName());
                config.setDriverClassName(info.getDriverClassName());
                config.setUrl(info.getUrl());
                config.setUsername(info.getUsername());
                config.setPassword(info.getPassword());
                config.setDbType(info.getDbType());
                config.setEnabled(info.getEnabled());
                
                addDataSource(config);
            }
        }
    }
}