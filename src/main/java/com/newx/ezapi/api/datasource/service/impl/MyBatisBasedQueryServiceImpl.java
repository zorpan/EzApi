package com.newx.ezapi.api.datasource.service.impl;

import com.newx.ezapi.api.datasource.service.DataSourceManager;
import com.newx.ezapi.api.datasource.service.MyBatisQueryService;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 基于真正的MyBatis框架实现的查询服务
 */
@Service
public class MyBatisBasedQueryServiceImpl implements MyBatisQueryService {

    @Autowired
    private DataSourceManager dataSourceManager;

    @Override
    public List<Map<String, Object>> executeMyBatisQuery(String dataSourceId, String sql, Map<String, Object> parameters) {
        try {
            DataSource dataSource = dataSourceManager.getDataSource(dataSourceId);
            if (dataSource == null) {
                throw new RuntimeException("数据源不存在: " + dataSourceId);
            }

            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory(dataSource);
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                return executeQueryWithSqlSession(sqlSession, sql, parameters);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error executing MyBatis query: " + e.getMessage(), e);
        }
    }

    @Override
    public int executeMyBatisUpdate(String dataSourceId, String sql, Map<String, Object> parameters) {
        try {
            DataSource dataSource = dataSourceManager.getDataSource(dataSourceId);
            if (dataSource == null) {
                throw new RuntimeException("数据源不存在: " + dataSourceId);
            }

            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory(dataSource);
            try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) { // 自动提交
                return executeUpdateWithSqlSession(sqlSession, sql, parameters);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error executing MyBatis update: " + e.getMessage(), e);
        }
    }

    @Override
    public int executeMyBatisDelete(String dataSourceId, String sql, Map<String, Object> parameters) {
        return executeMyBatisUpdate(dataSourceId, sql, parameters);
    }

    @Override
    public int executeMyBatisInsert(String dataSourceId, String sql, Map<String, Object> parameters) {
        return executeMyBatisUpdate(dataSourceId, sql, parameters);
    }

    /**
     * 创建SqlSessionFactory
     */
    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource) {
        Environment environment = new Environment("dynamic", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        
        // 设置其他配置选项
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        configuration.setLazyLoadingEnabled(false);
        configuration.setDefaultExecutorType(ExecutorType.SIMPLE);
        
        // 显式设置默认的脚本语言驱动
        configuration.setDefaultScriptingLanguage(XMLLanguageDriver.class);
        
        return new org.apache.ibatis.session.defaults.DefaultSqlSessionFactory(configuration);
    }

    /**
     * 使用SqlSession执行查询
     */
    private List<Map<String, Object>> executeQueryWithSqlSession(SqlSession sqlSession, String sql, Map<String, Object> parameters) {
        Configuration configuration = sqlSession.getConfiguration();
        
        try {
            // 先使用XML语言驱动来解析动态SQL
            LanguageDriver languageDriver = configuration.getDefaultScriptingLanuageInstance();
            
            // 创建SQL源，使用Object.class作为参数类型
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Object.class);
            
            // 生成一个唯一的MappedStatement ID
            String mappedStatementId = UUID.randomUUID().toString();
            
            // 创建MappedStatement
            MappedStatement.Builder statementBuilder = new MappedStatement.Builder(
                configuration, 
                mappedStatementId, 
                sqlSource, 
                SqlCommandType.SELECT
            );
            
            // 配置MappedStatement
            MappedStatement mappedStatement = statementBuilder
                .resultMaps(new ArrayList<ResultMap>() {{
                    add(new ResultMap.Builder(
                        configuration, 
                        "defaultResultMap", 
                        Map.class, 
                        new ArrayList<ResultMapping>()
                    ).build());
                }})
                .build();
            
            // 将MappedStatement添加到Configuration中
            configuration.addMappedStatement(mappedStatement);
            
            // 执行查询 - 直接使用SqlSession提供的方法
            return sqlSession.selectList(mappedStatementId, parameters != null ? parameters : Collections.emptyMap());
            
        } catch (Exception e) {
            // 输出详细错误信息以帮助调试
            System.err.println("Error executing dynamic SQL: " + e.getMessage());
            System.err.println("SQL: " + sql);
            System.err.println("Parameters: " + parameters);
            throw new RuntimeException("Error executing dynamic SQL: " + e.getMessage(), e);
        }
    }

    /**
     * 使用SqlSession执行更新
     */
    private int executeUpdateWithSqlSession(SqlSession sqlSession, String sql, Map<String, Object> parameters) {
        Configuration configuration = sqlSession.getConfiguration();
        
        try {
            // 先使用XML语言驱动来解析动态SQL
            LanguageDriver languageDriver = configuration.getDefaultScriptingLanuageInstance();
            
            // 创建SQL源，使用Object.class作为参数类型
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Object.class);
            
            // 生成一个唯一的MappedStatement ID
            String mappedStatementId = UUID.randomUUID().toString();
            
            // 创建MappedStatement
            MappedStatement.Builder statementBuilder = new MappedStatement.Builder(
                configuration, 
                mappedStatementId, 
                sqlSource, 
                SqlCommandType.UPDATE
            );
            
            // 配置MappedStatement
            MappedStatement mappedStatement = statementBuilder
                .resultMaps(new ArrayList<ResultMap>() {{
                    add(new ResultMap.Builder(
                        configuration, 
                        "defaultResultMap", 
                        Map.class, 
                        new ArrayList<ResultMapping>()
                    ).build());
                }})
                .build();
            
            // 将MappedStatement添加到Configuration中
            configuration.addMappedStatement(mappedStatement);
            
            // 执行更新 - 直接使用SqlSession提供的方法
            return sqlSession.update(mappedStatementId, parameters != null ? parameters : Collections.emptyMap());
            
        } catch (Exception e) {
            // 输出详细错误信息以帮助调试
            System.err.println("Error executing dynamic SQL: " + e.getMessage());
            System.err.println("SQL: " + sql);
            System.err.println("Parameters: " + parameters);
            throw new RuntimeException("Error executing dynamic SQL: " + e.getMessage(), e);
        }
    }
}