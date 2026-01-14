package com.newx.ezapi.api.datasource.service;

import com.newx.ezapi.api.datasource.entity.DataSourceConfig;
import com.newx.ezapi.api.datasource.service.impl.DataSourceManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DataSourceManagerTest {

    private DataSourceManager dataSourceManager;
    private DataSourceConfig testConfig;

    @BeforeEach
    void setUp() {
        dataSourceManager = new DataSourceManagerImpl();
        
        // 创建测试数据源配置（使用H2内存数据库）
        testConfig = new DataSourceConfig(
            "test-ds-1",
            "Test DataSource",
            "com.mysql.jdbc.Driver",
            "jdbc:mysql://127.0.0.1:3306/test",
            "root",
            "123456",
            "MySQL"
        );

    }

    @Test
    void testAddDataSource() {
        assertDoesNotThrow(() -> dataSourceManager.addDataSource(testConfig));
        
        DataSource ds = dataSourceManager.getDataSource("test-ds-1");
        assertNotNull(ds);
    }

    @Test
    void testGetAllDataSources() {
        // 添加一个数据源
        dataSourceManager.addDataSource(testConfig);
        
        List<DataSourceConfig> allDataSources = dataSourceManager.getAllDataSources();
        assertEquals(1, allDataSources.size());
        assertEquals("test-ds-1", allDataSources.get(0).getId());
    }

    @Test
    void testRemoveDataSource() {
        // 添加数据源
        dataSourceManager.addDataSource(testConfig);
        
        // 验证数据源存在
        assertNotNull(dataSourceManager.getDataSource("test-ds-1"));
        
        // 移除数据源
        dataSourceManager.removeDataSource("test-ds-1");
        
        // 验证数据源已被移除
        assertNull(dataSourceManager.getDataSource("test-ds-1"));
    }

    @Test
    void testConnection() throws SQLException {
        // 添加测试数据源
        dataSourceManager.addDataSource(testConfig);
        
        // 测试连接
        boolean connected = assertDoesNotThrow(() -> dataSourceManager.testConnection("test-ds-1"));
        assertTrue(connected);
        
        // 获取连接
        Connection conn = assertDoesNotThrow(() -> dataSourceManager.getConnection("test-ds-1"));
        assertNotNull(conn);
        assertFalse(conn.isClosed());
        conn.close();
    }

    @Test
    void testReloadDataSource() {
        // 添加数据源
        dataSourceManager.addDataSource(testConfig);
        DataSource originalDataSource = dataSourceManager.getDataSource("test-ds-1");
        assertNotNull(originalDataSource);
        
        // 重载数据源
        assertDoesNotThrow(() -> dataSourceManager.reloadDataSource("test-ds-1"));
        
        // 验证重载后数据源仍然存在
        DataSource reloadedDataSource = dataSourceManager.getDataSource("test-ds-1");
        assertNotNull(reloadedDataSource);
    }
}