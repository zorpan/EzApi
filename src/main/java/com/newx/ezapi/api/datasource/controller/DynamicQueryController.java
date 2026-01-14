package com.newx.ezapi.api.datasource.controller;

import com.newx.ezapi.api.datasource.service.DataSourceManager;
import com.newx.ezapi.api.datasource.service.DatabaseQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 动态查询控制器 - 将SQL查询直接转换为HTTP接口
 */
@RestController
@RequestMapping("/api/query")
public class DynamicQueryController {

    @Autowired
    private DataSourceManager dataSourceManager;
    
    @Autowired
    private DatabaseQueryService databaseQueryService;

    /**
     * 通过POST请求执行动态SQL查询
     * 请求体应包含: dataSourceId, sql, params(可选)
     */
    @PostMapping("/execute")
    public ResponseEntity<Object> executeDynamicQuery(@RequestBody Map<String, Object> request) {
        try {
            String dataSourceId = (String) request.get("dataSourceId");
            String sql = (String) request.get("sql");
            List<Object> params = (List<Object>) request.get("params");
            
            if (dataSourceId == null || dataSourceId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("dataSourceId is required");
            }
            
            if (sql == null || sql.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("SQL query is required");
            }
            
            List<Map<String, Object>> result;
            if (params != null && !params.isEmpty()) {
                // 使用参数化查询
                result = databaseQueryService.executeQueryWithParams(dataSourceId, sql, params.toArray());
            } else {
                // 直接执行查询
                result = databaseQueryService.executeQuery(dataSourceId, sql);
            }
            
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
     * 通过POST请求执行MyBatis风格的动态SQL查询
     * 支持<if>, <where>, <set>等标签
     */
    @PostMapping("/mybatis-execute")
    public ResponseEntity<Object> executeMyBatisQuery(@RequestBody Map<String, Object> request) {
        try {
            String dataSourceId = (String) request.get("dataSourceId");
            String myBatisSql = (String) request.get("myBatisSql");
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            
            if (dataSourceId == null || dataSourceId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("dataSourceId is required");
            }
            
            if (myBatisSql == null || myBatisSql.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("MyBatis SQL is required");
            }
            
            List<Map<String, Object>> result = databaseQueryService.executeMyBatisQuery(
                dataSourceId, myBatisSql, parameters);
            
            return ResponseEntity.ok(result);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SQL execution error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error executing MyBatis query: " + e.getMessage());
        }
    }

    /**
     * 通过GET请求执行简单查询（仅限SELECT，且无参数）
     * 注意：出于安全考虑，只允许SELECT查询
     */
    @GetMapping("/execute")
    public ResponseEntity<Object> executeSimpleQuery(
            @RequestParam String dataSourceId,
            @RequestParam String sql) {
        try {
            // 验证SQL是否为SELECT语句（简单验证）
            String upperSql = sql.toUpperCase().trim();
            if (!upperSql.startsWith("SELECT ")) {
                return ResponseEntity.badRequest().body("Only SELECT statements are allowed in GET requests");
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
     * 测试SQL语法
     */
    @PostMapping("/test-sql")
    public ResponseEntity<Object> testSqlSyntax(@RequestBody Map<String, Object> request) {
        try {
            String dataSourceId = (String) request.get("dataSourceId");
            String sql = (String) request.get("sql");
            
            if (dataSourceId == null || dataSourceId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("dataSourceId is required");
            }
            
            if (sql == null || sql.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("SQL query is required");
            }
            
            boolean isValid = databaseQueryService.testSqlSyntax(dataSourceId, sql);


            HashMap<Object, Object> resultMap = new HashMap<>();
            resultMap.put("valid", isValid);

            return ResponseEntity.ok(resultMap);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error testing SQL syntax: " + e.getMessage());
        }
    }
}