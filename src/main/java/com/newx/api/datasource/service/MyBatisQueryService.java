package com.newx.api.datasource.service;

import java.util.List;
import java.util.Map;

public interface MyBatisQueryService {
    /**
     * 执行MyBatis风格的SQL查询
     */
    List<Map<String, Object>> executeMyBatisQuery(String dataSourceId, String sql, Map<String, Object> parameters);

    /**
     * 执行MyBatis风格的SQL更新
     */
    int executeMyBatisUpdate(String dataSourceId, String sql, Map<String, Object> parameters);

    /**
     * 执行MyBatis风格的SQL删除
     */
    int executeMyBatisDelete(String dataSourceId, String sql, Map<String, Object> parameters);

    /**
     * 执行MyBatis风格的SQL插入
     */
    int executeMyBatisInsert(String dataSourceId, String sql, Map<String, Object> parameters);
}