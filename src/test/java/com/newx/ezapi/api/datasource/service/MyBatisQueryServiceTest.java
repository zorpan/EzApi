package com.newx.ezapi.api.datasource.service;

import com.newx.ezapi.core.entity.DataSourceConfig;
import com.newx.ezapi.core.service.DataSourceManager;
import com.newx.ezapi.core.service.DatabaseQueryService;
import com.newx.ezapi.core.service.MyBatisQueryService;
import com.newx.ezapi.core.service.impl.DataSourceManagerImpl;
import com.newx.ezapi.core.service.impl.MyBatisBasedQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MyBatisQueryServiceTest {

    private DataSourceManager dataSourceManager;
    private DatabaseQueryService databaseQueryService;
    private MyBatisQueryService myBatisQueryService;
    private DataSourceConfig testConfig;

    @BeforeEach
    void setUp() {
        dataSourceManager = new DataSourceManagerImpl();
        databaseQueryService = new com.newx.ezapi.core.service.impl.DatabaseQueryServiceImpl();
        myBatisQueryService = new MyBatisBasedQueryServiceImpl();
        
        // 设置依赖（通过反射）
        org.springframework.test.util.ReflectionTestUtils.setField(
            databaseQueryService, "dataSourceManager", dataSourceManager);
        org.springframework.test.util.ReflectionTestUtils.setField(
            myBatisQueryService, "dataSourceManager", dataSourceManager);
        
        // 创建测试数据源配置（使用Mysql数据库）
        testConfig = new DataSourceConfig(
                3L,
                "Test DataSource",
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://127.0.0.1:3306/test",
                "root",
                "123456",
                "MySQL"
        );
        
        // 添加数据源
        dataSourceManager.saveDataSource(testConfig);
        
        // 创建测试表（只在初始化时创建一次）
        try {
            databaseQueryService.executeUpdate("test-ds-mybatis", 
                "CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), age INT)");
            databaseQueryService.executeUpdate("test-ds-mybatis", 
                "CREATE TABLE IF NOT EXISTS products (id INT PRIMARY KEY, name VARCHAR(255), price DECIMAL(10,2), category VARCHAR(255))");
            
            // 清空表数据
            databaseQueryService.executeUpdate("test-ds-mybatis", "DELETE FROM users");
            databaseQueryService.executeUpdate("test-ds-mybatis", "DELETE FROM products");
            
            // 插入测试数据
            databaseQueryService.executeUpdate("test-ds-mybatis", 
                "INSERT INTO users VALUES (1, 'John Doe', 'john@example.com', 25), (2, 'Jane Smith', 'jane@example.com', 30)");
            databaseQueryService.executeUpdate("test-ds-mybatis", 
                "INSERT INTO products VALUES (1, 'Laptop', 999.99, 'Electronics'), (2, 'Book', 19.99, 'Education')");
        } catch (SQLException e) {
            // 忽略表已存在的错误
        }
    }

    @Test
    void testMyBatisIfCondition() throws SQLException {
        // 测试带if条件的SQL
        String myBatisSql = "SELECT * FROM users <where> <if test='name != null'>name = #{name}</if> </where>";
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "John Doe");
        
        List<Map<String, Object>> result = myBatisQueryService.executeMyBatisQuery("test-ds-mybatis", myBatisSql, params);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        
        // 检查name字段的值（考虑大小写问题）
        Object nameValue = result.get(0).get("name") != null ? result.get(0).get("name") : result.get(0).get("NAME");
        assertEquals("John Doe", nameValue);
        
        System.out.println("MyBatis query result: " + result);
    }

    @Test
    void testMyBatisWhereTag() throws SQLException {
        // 测试带where标签的SQL
        String myBatisSql = "SELECT * FROM products <where> <if test='category != null'>category = #{category}</if> <if test='price != null'>AND price &lt;= #{price}</if> </where>";
        
        Map<String, Object> params = new HashMap<>();
        params.put("category", "Electronics");
        params.put("price", 1000.00);
        
        List<Map<String, Object>> result = myBatisQueryService.executeMyBatisQuery("test-ds-mybatis", myBatisSql, params);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() >= 1); // 至少有一个电子产品价格小于1000
        
        System.out.println("MyBatis WHERE query result: " + result);
    }

    @Test
    void testMyBatisNullCondition() throws SQLException {
        // 测试当条件为null时的情况
        String myBatisSql = "SELECT * FROM users <where> <if test='name != null'>name = #{name}</if> </where>";
        
        Map<String, Object> params = new HashMap<>();
        // 不设置name参数，让它为null
        
        List<Map<String, Object>> result = myBatisQueryService.executeMyBatisQuery("test-ds-mybatis", myBatisSql, params);
        
        // 当条件不满足时，应该返回所有记录
        assertNotNull(result);
        assertEquals(2, result.size()); // 应该返回所有用户
        
        System.out.println("MyBatis null condition query result: " + result);
    }
    
    @Test
    void testMyBatisUpdate() throws SQLException {
        // 获取测试数据源ID
        List<DataSourceConfig> allDataSources = dataSourceManager.getAllDataSources();
        String dataSourceId = null;
        if (!allDataSources.isEmpty()) {
            dataSourceId = allDataSources.get(0).getId().toString();
        }
        
        if (dataSourceId != null) {
            // 测试带条件的更新
            String myBatisSql = "UPDATE users <set> <if test='name != null'>name = #{name},</if> <if test='age != null'>age = #{age}</if> </set> <where> id = #{id} </where>";
            
            Map<String, Object> params = new HashMap<>();
            params.put("id", 1);
            params.put("name", "Updated Name");
            params.put("age", 26);
            
            int result = myBatisQueryService.executeMyBatisUpdate(dataSourceId, myBatisSql, params);
            
            assertEquals(1, result); // 1行被更新
            
            // 验证更新结果
            List<Map<String, Object>> selectResult = databaseQueryService.executeQuery(dataSourceId, "SELECT * FROM users WHERE id = 1");
            Object nameValue = selectResult.get(0).get("name") != null ? selectResult.get(0).get("name") : selectResult.get(0).get("NAME");
            Object ageValue = selectResult.get(0).get("age") != null ? selectResult.get(0).get("age") : selectResult.get(0).get("AGE");
            
            assertEquals("Updated Name", nameValue);
            assertEquals(26, ageValue);
            
            System.out.println("MyBatis update executed successfully");
        }
    }
}