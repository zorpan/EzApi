package com.newx.api.datasource.service.impl;

import com.newx.api.datasource.service.DataSourceManager;
import com.newx.api.datasource.service.DatabaseQueryService;
import com.newx.api.datasource.service.MyBatisQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseQueryServiceImpl implements DatabaseQueryService {

    @Autowired
    private DataSourceManager dataSourceManager;
    
    @Autowired
    private MyBatisQueryService myBatisQueryService;

    @Override
    public List<Map<String, Object>> executeQuery(String dataSourceId, String sql) throws SQLException {
        try (Connection connection = dataSourceManager.getConnection(dataSourceId)) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    return mapResultSet(resultSet);
                }
            }
        }
    }

    @Override
    public int executeUpdate(String dataSourceId, String sql) throws SQLException {
        try (Connection connection = dataSourceManager.getConnection(dataSourceId)) {
            try (Statement statement = connection.createStatement()) {
                return statement.executeUpdate(sql);
            }
        }
    }

    @Override
    public List<Map<String, Object>> executeQueryWithParams(String dataSourceId, String sql, Object... params) throws SQLException {
        try (Connection connection = dataSourceManager.getConnection(dataSourceId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // 设置参数
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
                
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return mapResultSet(resultSet);
                }
            }
        }
    }

    @Override
    public boolean testSqlSyntax(String dataSourceId, String sql) {
        try (Connection connection = dataSourceManager.getConnection(dataSourceId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // 如果能成功准备语句，则语法基本正确
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getTableInfo(String dataSourceId, String tableName) throws SQLException {
        try (Connection connection = dataSourceManager.getConnection(dataSourceId)) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 对于H2等内存数据库，可能需要指定正确的schema
            String schemaPattern = null;
            // 尝试从连接中获取默认schema
            try {
                schemaPattern = connection.getSchema();
            } catch (AbstractMethodError | UnsupportedOperationException e) {
                // 如果不支持getSchema方法，则保持null
                schemaPattern = null;
            }
            
            // 尝试使用通配符匹配表名
            String tablePattern = tableName;
            if (tableName != null && !tableName.contains("%") && !tableName.contains("_")) {
                // 如果表名不是通配符，尝试不同的大小写形式
                try (ResultSet columns = metaData.getColumns(null, schemaPattern, tablePattern, "%")) {
                    List<Map<String, Object>> columnsInfo = new ArrayList<>();
                    boolean hasResults = false;
                    
                    while (columns.next()) {
                        Map<String, Object> columnInfo = new HashMap<>();
                        columnInfo.put("TABLE_CAT", columns.getString("TABLE_CAT"));
                        columnInfo.put("TABLE_SCHEM", columns.getString("TABLE_SCHEM"));
                        columnInfo.put("TABLE_NAME", columns.getString("TABLE_NAME"));
                        columnInfo.put("COLUMN_NAME", columns.getString("COLUMN_NAME"));
                        columnInfo.put("DATA_TYPE", columns.getInt("DATA_TYPE"));
                        columnInfo.put("TYPE_NAME", columns.getString("TYPE_NAME"));
                        columnInfo.put("COLUMN_SIZE", columns.getInt("COLUMN_SIZE"));
                        columnInfo.put("DECIMAL_DIGITS", columns.getInt("DECIMAL_DIGITS"));
                        columnInfo.put("NUM_PREC_RADIX", columns.getInt("NUM_PREC_RADIX"));
                        columnInfo.put("NULLABLE", columns.getInt("NULLABLE"));
                        columnInfo.put("REMARKS", columns.getString("REMARKS"));
                        columnInfo.put("COLUMN_DEF", columns.getString("COLUMN_DEF"));
                        columnInfo.put("SQL_DATA_TYPE", columns.getInt("SQL_DATA_TYPE"));
                        columnInfo.put("SQL_DATETIME_SUB", columns.getInt("SQL_DATETIME_SUB"));
                        columnInfo.put("CHAR_OCTET_LENGTH", columns.getInt("CHAR_OCTET_LENGTH"));
                        columnInfo.put("ORDINAL_POSITION", columns.getInt("ORDINAL_POSITION"));
                        columnInfo.put("IS_NULLABLE", columns.getString("IS_NULLABLE"));
                        columnInfo.put("SCOPE_CATALOG", columns.getString("SCOPE_CATALOG"));
                        columnInfo.put("SCOPE_SCHEMA", columns.getString("SCOPE_SCHEMA"));
                        columnInfo.put("SCOPE_TABLE", columns.getString("SCOPE_TABLE"));
                        columnInfo.put("SOURCE_DATA_TYPE", columns.getShort("SOURCE_DATA_TYPE"));
                        columnInfo.put("IS_AUTOINCREMENT", columns.getString("IS_AUTOINCREMENT"));
                        columnsInfo.add(columnInfo);
                        hasResults = true;
                    }
                    
                    if (hasResults) {
                        return columnsInfo;
                    }
                }
                
                // 如果没找到，尝试大写
                try (ResultSet columns = metaData.getColumns(null, schemaPattern, tableName.toUpperCase(), "%")) {
                    List<Map<String, Object>> columnsInfo = new ArrayList<>();
                    boolean hasResults = false;
                    
                    while (columns.next()) {
                        Map<String, Object> columnInfo = new HashMap<>();
                        columnInfo.put("TABLE_CAT", columns.getString("TABLE_CAT"));
                        columnInfo.put("TABLE_SCHEM", columns.getString("TABLE_SCHEM"));
                        columnInfo.put("TABLE_NAME", columns.getString("TABLE_NAME"));
                        columnInfo.put("COLUMN_NAME", columns.getString("COLUMN_NAME"));
                        columnInfo.put("DATA_TYPE", columns.getInt("DATA_TYPE"));
                        columnInfo.put("TYPE_NAME", columns.getString("TYPE_NAME"));
                        columnInfo.put("COLUMN_SIZE", columns.getInt("COLUMN_SIZE"));
                        columnInfo.put("DECIMAL_DIGITS", columns.getInt("DECIMAL_DIGITS"));
                        columnInfo.put("NUM_PREC_RADIX", columns.getInt("NUM_PREC_RADIX"));
                        columnInfo.put("NULLABLE", columns.getInt("NULLABLE"));
                        columnInfo.put("REMARKS", columns.getString("REMARKS"));
                        columnInfo.put("COLUMN_DEF", columns.getString("COLUMN_DEF"));
                        columnInfo.put("SQL_DATA_TYPE", columns.getInt("SQL_DATA_TYPE"));
                        columnInfo.put("SQL_DATETIME_SUB", columns.getInt("SQL_DATETIME_SUB"));
                        columnInfo.put("CHAR_OCTET_LENGTH", columns.getInt("CHAR_OCTET_LENGTH"));
                        columnInfo.put("ORDINAL_POSITION", columns.getInt("ORDINAL_POSITION"));
                        columnInfo.put("IS_NULLABLE", columns.getString("IS_NULLABLE"));
                        columnInfo.put("SCOPE_CATALOG", columns.getString("SCOPE_CATALOG"));
                        columnInfo.put("SCOPE_SCHEMA", columns.getString("SCOPE_SCHEMA"));
                        columnInfo.put("SCOPE_TABLE", columns.getString("SCOPE_TABLE"));
                        columnInfo.put("SOURCE_DATA_TYPE", columns.getShort("SOURCE_DATA_TYPE"));
                        columnInfo.put("IS_AUTOINCREMENT", columns.getString("IS_AUTOINCREMENT"));
                        columnsInfo.add(columnInfo);
                        hasResults = true;
                    }
                    
                    if (hasResults) {
                        return columnsInfo;
                    }
                }
                
                // 如果还没找到，尝试小写
                try (ResultSet columns = metaData.getColumns(null, schemaPattern, tableName.toLowerCase(), "%")) {
                    List<Map<String, Object>> columnsInfo = new ArrayList<>();
                    boolean hasResults = false;
                    
                    while (columns.next()) {
                        Map<String, Object> columnInfo = new HashMap<>();
                        columnInfo.put("TABLE_CAT", columns.getString("TABLE_CAT"));
                        columnInfo.put("TABLE_SCHEM", columns.getString("TABLE_SCHEM"));
                        columnInfo.put("TABLE_NAME", columns.getString("TABLE_NAME"));
                        columnInfo.put("COLUMN_NAME", columns.getString("COLUMN_NAME"));
                        columnInfo.put("DATA_TYPE", columns.getInt("DATA_TYPE"));
                        columnInfo.put("TYPE_NAME", columns.getString("TYPE_NAME"));
                        columnInfo.put("COLUMN_SIZE", columns.getInt("COLUMN_SIZE"));
                        columnInfo.put("DECIMAL_DIGITS", columns.getInt("DECIMAL_DIGITS"));
                        columnInfo.put("NUM_PREC_RADIX", columns.getInt("NUM_PREC_RADIX"));
                        columnInfo.put("NULLABLE", columns.getInt("NULLABLE"));
                        columnInfo.put("REMARKS", columns.getString("REMARKS"));
                        columnInfo.put("COLUMN_DEF", columns.getString("COLUMN_DEF"));
                        columnInfo.put("SQL_DATA_TYPE", columns.getInt("SQL_DATA_TYPE"));
                        columnInfo.put("SQL_DATETIME_SUB", columns.getInt("SQL_DATETIME_SUB"));
                        columnInfo.put("CHAR_OCTET_LENGTH", columns.getInt("CHAR_OCTET_LENGTH"));
                        columnInfo.put("ORDINAL_POSITION", columns.getInt("ORDINAL_POSITION"));
                        columnInfo.put("IS_NULLABLE", columns.getString("IS_NULLABLE"));
                        columnInfo.put("SCOPE_CATALOG", columns.getString("SCOPE_CATALOG"));
                        columnInfo.put("SCOPE_SCHEMA", columns.getString("SCOPE_SCHEMA"));
                        columnInfo.put("SCOPE_TABLE", columns.getString("SCOPE_TABLE"));
                        columnInfo.put("SOURCE_DATA_TYPE", columns.getShort("SOURCE_DATA_TYPE"));
                        columnInfo.put("IS_AUTOINCREMENT", columns.getString("IS_AUTOINCREMENT"));
                        columnsInfo.add(columnInfo);
                        hasResults = true;
                    }
                    
                    if (hasResults) {
                        return columnsInfo;
                    }
                }
            } else {
                // 如果已经是通配符，直接使用
                try (ResultSet columns = metaData.getColumns(null, schemaPattern, tablePattern, "%")) {
                    List<Map<String, Object>> columnsInfo = new ArrayList<>();
                    
                    while (columns.next()) {
                        Map<String, Object> columnInfo = new HashMap<>();
                        columnInfo.put("TABLE_CAT", columns.getString("TABLE_CAT"));
                        columnInfo.put("TABLE_SCHEM", columns.getString("TABLE_SCHEM"));
                        columnInfo.put("TABLE_NAME", columns.getString("TABLE_NAME"));
                        columnInfo.put("COLUMN_NAME", columns.getString("COLUMN_NAME"));
                        columnInfo.put("DATA_TYPE", columns.getInt("DATA_TYPE"));
                        columnInfo.put("TYPE_NAME", columns.getString("TYPE_NAME"));
                        columnInfo.put("COLUMN_SIZE", columns.getInt("COLUMN_SIZE"));
                        columnInfo.put("DECIMAL_DIGITS", columns.getInt("DECIMAL_DIGITS"));
                        columnInfo.put("NUM_PREC_RADIX", columns.getInt("NUM_PREC_RADIX"));
                        columnInfo.put("NULLABLE", columns.getInt("NULLABLE"));
                        columnInfo.put("REMARKS", columns.getString("REMARKS"));
                        columnInfo.put("COLUMN_DEF", columns.getString("COLUMN_DEF"));
                        columnInfo.put("SQL_DATA_TYPE", columns.getInt("SQL_DATA_TYPE"));
                        columnInfo.put("SQL_DATETIME_SUB", columns.getInt("SQL_DATETIME_SUB"));
                        columnInfo.put("CHAR_OCTET_LENGTH", columns.getInt("CHAR_OCTET_LENGTH"));
                        columnInfo.put("ORDINAL_POSITION", columns.getInt("ORDINAL_POSITION"));
                        columnInfo.put("IS_NULLABLE", columns.getString("IS_NULLABLE"));
                        columnInfo.put("SCOPE_CATALOG", columns.getString("SCOPE_CATALOG"));
                        columnInfo.put("SCOPE_SCHEMA", columns.getString("SCOPE_SCHEMA"));
                        columnInfo.put("SCOPE_TABLE", columns.getString("SCOPE_TABLE"));
                        columnInfo.put("SOURCE_DATA_TYPE", columns.getShort("SOURCE_DATA_TYPE"));
                        columnInfo.put("IS_AUTOINCREMENT", columns.getString("IS_AUTOINCREMENT"));
                        columnsInfo.add(columnInfo);
                    }
                    
                    return columnsInfo;
                }
            }
            
            // 如果所有尝试都没找到，返回空列表
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getDatabaseMetadata(String dataSourceId) throws SQLException {
        try (Connection connection = dataSourceManager.getConnection(dataSourceId)) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            Map<String, Object> metadataMap = new HashMap<>();
            metadataMap.put("URL", metaData.getURL());
            metadataMap.put("USERNAME", metaData.getUserName());
            metadataMap.put("DRIVER_NAME", metaData.getDriverName());
            metadataMap.put("DRIVER_VERSION", metaData.getDriverVersion());
            metadataMap.put("DATABASE_PRODUCT_NAME", metaData.getDatabaseProductName());
            metadataMap.put("DATABASE_PRODUCT_VERSION", metaData.getDatabaseProductVersion());
            metadataMap.put("DATABASE_MAJOR_VERSION", metaData.getDatabaseMajorVersion());
            metadataMap.put("DATABASE_MINOR_VERSION", metaData.getDatabaseMinorVersion());
            metadataMap.put("JDBC_MAJOR_VERSION", metaData.getJDBCMajorVersion());
            metadataMap.put("JDBC_MINOR_VERSION", metaData.getJDBCMinorVersion());
            
            return metadataMap;
        }
    }

    @Override
    public List<Map<String, Object>> executeMyBatisQuery(String dataSourceId, String myBatisSql, Map<String, Object> parameters) throws SQLException {
        // 使用注入的MyBatis服务来处理MyBatis风格的SQL
        try {
            return myBatisQueryService.executeMyBatisQuery(dataSourceId, myBatisSql, parameters);
        } catch (Exception e) {
            throw new SQLException("Error executing MyBatis query: " + e.getMessage(), e);
        }
    }

    /**
     * 将ResultSet转换为List<Map<String, Object>>
     */
    private List<Map<String, Object>> mapResultSet(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object value = resultSet.getObject(i);
                // 使用列标签作为键，这样可以处理别名
                row.put(columnName, value);
            }
            result.add(row);
        }

        return result;
    }
}