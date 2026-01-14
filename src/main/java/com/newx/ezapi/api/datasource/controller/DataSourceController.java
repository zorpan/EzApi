package com.newx.ezapi.api.datasource.controller;

import com.newx.ezapi.api.datasource.constant.DatabaseType;
import com.newx.ezapi.api.datasource.entity.DataSourceConfig;
import com.newx.ezapi.api.datasource.service.DatabaseQueryService;
import com.newx.ezapi.api.datasource.service.DataSourceManager;
import com.newx.ezapi.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datasource")
public class DataSourceController {

    @Autowired
    private DataSourceManager dataSourceManager;
    
    @Autowired
    private DatabaseQueryService databaseQueryService;

    /**
     * 获取所有数据源类型
     */
    @GetMapping("/types")
    public Result<List<DatabaseType>> getDatabaseTypes() {
        return Result.success(Arrays.asList(DatabaseType.values()));
    }

    /**
     * 添加数据源
     */
    @PostMapping("/add")
    public Result<String> addDataSource(@RequestBody DataSourceConfig config) {
        try {
            dataSourceManager.addDataSource(config);
            return Result.success("Data source added successfully", "数据源添加成功");
        } catch (Exception e) {
            return Result.error("Error adding data source: " + e.getMessage());
        }
    }

    /**
     * 获取所有数据源
     */
    @GetMapping("/list")
    public Result<List<DataSourceConfig>> getAllDataSources() {
        try {
            List<DataSourceConfig> dataSources = dataSourceManager.getAllDataSources();
            return Result.success(dataSources);
        } catch (Exception e) {
            return Result.error("获取数据源列表失败: " + e.getMessage());
        }
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/test/{id}")
    public Result<String> testConnection(@PathVariable String id) {
        try {
            boolean connected = dataSourceManager.testConnection(id);
            if (connected) {
                return Result.success("Connection successful", "连接测试成功");
            } else {
                return Result.error(503, "Connection failed");
            }
        } catch (SQLException e) {
            return Result.error(500, "Connection error: " + e.getMessage());
        }
    }

    /**
     * 移除数据源
     */
    @DeleteMapping("/remove/{id}")
    public Result<String> removeDataSource(@PathVariable String id) {
        try {
            dataSourceManager.removeDataSource(id);
            return Result.success("Data source removed successfully", "数据源移除成功");
        } catch (Exception e) {
            return Result.error("Error removing data source: " + e.getMessage());
        }
    }

    /**
     * 执行查询SQL
     */
    @PostMapping("/query/{dataSourceId}")
    public Result<List<Map<String, Object>>> executeQuery(@PathVariable String dataSourceId, 
                                               @RequestBody Map<String, String> request) {
        try {
            String sql = request.get("sql");
            if (sql == null || sql.trim().isEmpty()) {
                return Result.error("SQL query is required");
            }
            
            List<Map<String, Object>> result = databaseQueryService.executeQuery(dataSourceId, sql);
            return Result.success(result);
        } catch (SQLException e) {
            return Result.error(500, "SQL execution error: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "Error executing query: " + e.getMessage());
        }
    }

    /**
     * 执行更新SQL
     */
    @PostMapping("/update/{dataSourceId}")
    public Result<Map<Object, Object>> executeUpdate(@PathVariable String dataSourceId, 
                                                @RequestBody Map<String, String> request) {
        try {
            String sql = request.get("sql");
            if (sql == null || sql.trim().isEmpty()) {
                return Result.error("SQL query is required");
            }
            
            int result = databaseQueryService.executeUpdate(dataSourceId, sql);
            HashMap<Object, Object> resultMap = new HashMap<>();
            resultMap.put("affectedRows", result);

            return Result.success(resultMap);
        } catch (SQLException e) {
            return Result.error(500, "SQL execution error: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "Error executing update: " + e.getMessage());
        }
    }

    /**
     * 获取表结构信息
     */
    @GetMapping("/table/{dataSourceId}/{tableName}")
    public Result<List<Map<String, Object>>> getTableInfo(@PathVariable String dataSourceId, 
                                               @PathVariable String tableName) {
        try {
            List<Map<String, Object>> result = databaseQueryService.getTableInfo(dataSourceId, tableName);
            return Result.success(result);
        } catch (SQLException e) {
            return Result.error(500, "Error getting table info: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "Error getting table info: " + e.getMessage());
        }
    }

    /**
     * 获取数据库元数据
     */
    @GetMapping("/metadata/{dataSourceId}")
    public Result<Map<String, Object>> getDatabaseMetadata(@PathVariable String dataSourceId) {
        try {
            Map<String, Object> result = databaseQueryService.getDatabaseMetadata(dataSourceId);
            return Result.success(result);
        } catch (SQLException e) {
            return Result.error(500, "Error getting database metadata: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "Error getting database metadata: " + e.getMessage());
        }
    }
}