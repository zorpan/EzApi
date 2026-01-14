package com.newx.ezapi.api.datasource.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseQueryService {
    /**
     * 执行查询SQL
     */
    List<Map<String, Object>> executeQuery(String dataSourceId, String sql) throws SQLException;

    /**
     * 执行更新SQL
     */
    int executeUpdate(String dataSourceId, String sql) throws SQLException;

    /**
     * 执行SQL并返回结果，支持参数绑定
     */
    List<Map<String, Object>> executeQueryWithParams(String dataSourceId, String sql, Object... params) throws SQLException;

    /**
     * 测试SQL语法
     */
    boolean testSqlSyntax(String dataSourceId, String sql);

    /**
     * 获取表结构信息
     */
    List<Map<String, Object>> getTableInfo(String dataSourceId, String tableName) throws SQLException;

    /**
     * 获取数据库元数据
     */
    Map<String, Object> getDatabaseMetadata(String dataSourceId) throws SQLException;
    
    /**
     * 执行MyBatis风格的动态SQL查询
     */
    List<Map<String, Object>> executeMyBatisQuery(String dataSourceId, String myBatisSql, Map<String, Object> parameters) throws SQLException;
}