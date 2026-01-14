package com.newx.api.datasource.controller;

import com.newx.api.datasource.entity.DataSourceConfig;
import com.newx.api.datasource.service.DataSourceManager;
import com.newx.api.datasource.service.DatabaseQueryService;
import com.newx.api.datasource.constant.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;

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
    public ResponseEntity<List<DatabaseType>> getDatabaseTypes() {
        return ResponseEntity.ok(Arrays.asList(DatabaseType.values()));
    }

    /**
     * 添加数据源
     */
    @PostMapping("/add")
    public ResponseEntity<String> addDataSource(@RequestBody DataSourceConfig config) {
        try {
            dataSourceManager.addDataSource(config);
            return ResponseEntity.ok("Data source added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding data source: " + e.getMessage());
        }
    }

    /**
     * 获取所有数据源
     */
    @GetMapping("/list")
    public ResponseEntity<List<DataSourceConfig>> getAllDataSources() {
        try {
            List<DataSourceConfig> dataSources = dataSourceManager.getAllDataSources();
            return ResponseEntity.ok(dataSources);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/test/{id}")
    public ResponseEntity<String> testConnection(@PathVariable String id) {
        try {
            boolean connected = dataSourceManager.testConnection(id);
            if (connected) {
                return ResponseEntity.ok("Connection successful");
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Connection failed");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Connection error: " + e.getMessage());
        }
    }

    /**
     * 移除数据源
     */
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> removeDataSource(@PathVariable String id) {
        try {
            dataSourceManager.removeDataSource(id);
            return ResponseEntity.ok("Data source removed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing data source: " + e.getMessage());
        }
    }

    /**
     * 执行查询SQL
     */
    @PostMapping("/query/{dataSourceId}")
    public ResponseEntity<Object> executeQuery(@PathVariable String dataSourceId, 
                                               @RequestBody Map<String, String> request) {
        try {
            String sql = request.get("sql");
            if (sql == null || sql.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("SQL query is required");
            }
            
            List<Map<String, Object>> result = databaseQueryService.executeQuery(dataSourceId, sql);
            return ResponseEntity.ok(result);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SQL execution error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error executing query: " + e.getMessage());
        }
    }

    /**
     * 执行更新SQL
     */
    @PostMapping("/update/{dataSourceId}")
    public ResponseEntity<Object> executeUpdate(@PathVariable String dataSourceId, 
                                                @RequestBody Map<String, String> request) {
        try {
            String sql = request.get("sql");
            if (sql == null || sql.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("SQL query is required");
            }
            
            int result = databaseQueryService.executeUpdate(dataSourceId, sql);
            HashMap<Object, Object> resultMap = new HashMap<>();
            resultMap.put("affectedRows", result);

            return ResponseEntity.ok(resultMap);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SQL execution error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error executing update: " + e.getMessage());
        }
    }

    /**
     * 获取表结构信息
     */
    @GetMapping("/table/{dataSourceId}/{tableName}")
    public ResponseEntity<Object> getTableInfo(@PathVariable String dataSourceId, 
                                               @PathVariable String tableName) {
        try {
            List<Map<String, Object>> result = databaseQueryService.getTableInfo(dataSourceId, tableName);
            return ResponseEntity.ok(result);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting table info: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting table info: " + e.getMessage());
        }
    }

    /**
     * 获取数据库元数据
     */
    @GetMapping("/metadata/{dataSourceId}")
    public ResponseEntity<Object> getDatabaseMetadata(@PathVariable String dataSourceId) {
        try {
            Map<String, Object> result = databaseQueryService.getDatabaseMetadata(dataSourceId);
            return ResponseEntity.ok(result);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting database metadata: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting database metadata: " + e.getMessage());
        }
    }
}