package com.newx.api.datasource.config;

import com.newx.api.datasource.service.DataSourceManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;

@Configuration
@ComponentScan(basePackages = "com.newx")
public class DataSourceModuleConfig implements SmartLifecycle {
    
    @Autowired
    private DataSourceManager dataSourceManager;
    
    private boolean isRunning = false;
    
    @Override
    public void start() {
        // 应用启动时从数据库加载所有数据源
        dataSourceManager.reloadAllDataSourcesFromDb();
        isRunning = true;
    }
    
    @Override
    public void stop() {
        isRunning = false;
    }
    
    @Override
    public boolean isRunning() {
        return isRunning;
    }
    
    @Override
    public int getPhase() {
        return 0;
    }
    
    @Override
    public boolean isAutoStartup() {
        return true;
    }
    
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
}