package com.newx.ezapi.core.service.impl;

import com.newx.ezapi.core.entity.ApiInfo;
import com.newx.ezapi.core.entity.enums.ProtocolType;
import com.newx.ezapi.core.service.ApiExecutor;
import com.newx.ezapi.core.service.DatabaseQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * SQL 协议执行器
 * <p>
 * 封装现有的 MyBatis 动态 SQL 查询逻辑。
 * 当 ApiInfo.protocolType = 'SQL' 时使用此执行器。
 * </p>
 */
@Component
public class SqlApiExecutor implements ApiExecutor {

    @Autowired
    private DatabaseQueryService databaseQueryService;

    @Override
    public Object execute(ApiInfo apiInfo, Map<String, Object> parameters) throws Exception {
        List<Map<String, Object>> result = databaseQueryService.executeMyBatisQuery(
                apiInfo.getDataSourceId(),
                apiInfo.getSqlContent(),
                parameters
        );
        return result;
    }

    @Override
    public String getProtocolType() {
        return ProtocolType.SQL.getCode();
    }
}
