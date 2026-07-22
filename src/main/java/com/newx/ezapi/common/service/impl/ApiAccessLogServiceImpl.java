package com.newx.ezapi.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.newx.ezapi.common.entity.ApiAccessLog;
import com.newx.ezapi.common.mapper.ApiAccessLogMapper;
import com.newx.ezapi.common.service.ApiAccessLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API访问日志服务实现类
 */
@Service
public class ApiAccessLogServiceImpl implements ApiAccessLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiAccessLogServiceImpl.class);
    
    @Autowired
    private ApiAccessLogMapper apiAccessLogMapper;
    
    @Override
    @Async("logTaskExecutor")
    public ApiAccessLog saveLog(ApiAccessLog log) {
        try {
            if (log.getCreateTime() == null) {
                log.setCreateTime(LocalDateTime.now());
            }
            apiAccessLogMapper.insert(log);
            logger.debug("保存访问日志成功，请求ID: {}", log.getRequestId());
        } catch (Exception e) {
            logger.error("保存访问日志失败，请求ID: {}", log.getRequestId(), e);
        }
        return log;
    }
    
    @Override
    public ApiAccessLog getLogById(Long id) {
        return apiAccessLogMapper.selectById(id);
    }
    
    @Override
    public ApiAccessLog getLogByRequestId(String requestId) {
        LambdaQueryWrapper<ApiAccessLog> wrapper = Wrappers.<ApiAccessLog>lambdaQuery()
                .eq(ApiAccessLog::getRequestId, requestId);
        return apiAccessLogMapper.selectOne(wrapper);
    }
    
    @Override
    public List<ApiAccessLog> getLogList(Map<String, Object> params) {
        LambdaQueryWrapper<ApiAccessLog> wrapper = buildQueryWrapper(params);
        wrapper.orderByDesc(ApiAccessLog::getCreateTime);
        
        // 分页处理
        Integer pageNum = (Integer) params.get("pageNum");
        Integer pageSize = (Integer) params.get("pageSize");
        if (pageNum != null && pageSize != null) {
            wrapper.last("LIMIT " + (pageNum - 1) * pageSize + ", " + pageSize);
        }
        
        return apiAccessLogMapper.selectList(wrapper);
    }
    
    @Override
    public Long getLogCount(Map<String, Object> params) {
        LambdaQueryWrapper<ApiAccessLog> wrapper = buildQueryWrapper(params);
        return apiAccessLogMapper.selectCount(wrapper);
    }
    
    @Override
    public int deleteLogsBefore(LocalDateTime beforeTime) {
        LambdaQueryWrapper<ApiAccessLog> wrapper = Wrappers.<ApiAccessLog>lambdaQuery()
                .lt(ApiAccessLog::getCreateTime, beforeTime);
        return apiAccessLogMapper.delete(wrapper);
    }
    
    @Override
    public int clearAllLogs() {
        return apiAccessLogMapper.delete(null);
    }
    
    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ApiAccessLog> buildQueryWrapper(Map<String, Object> params) {
        LambdaQueryWrapper<ApiAccessLog> wrapper = Wrappers.<ApiAccessLog>lambdaQuery();
        
        if (params.containsKey("apiId") && params.get("apiId") != null) {
            wrapper.eq(ApiAccessLog::getApiId, params.get("apiId"));
        }
        
        if (params.containsKey("apiPath") && params.get("apiPath") != null) {
            wrapper.like(ApiAccessLog::getApiPath, params.get("apiPath"));
        }
        
        if (params.containsKey("httpMethod") && params.get("httpMethod") != null) {
            wrapper.eq(ApiAccessLog::getHttpMethod, params.get("httpMethod"));
        }
        
        if (params.containsKey("clientIp") && params.get("clientIp") != null) {
            wrapper.eq(ApiAccessLog::getClientIp, params.get("clientIp"));
        }
        
        if (params.containsKey("username") && params.get("username") != null) {
            wrapper.eq(ApiAccessLog::getUsername, params.get("username"));
        }
        
        if (params.containsKey("startTime") && params.get("startTime") != null) {
            wrapper.ge(ApiAccessLog::getCreateTime, params.get("startTime"));
        }
        
        if (params.containsKey("endTime") && params.get("endTime") != null) {
            wrapper.le(ApiAccessLog::getCreateTime, params.get("endTime"));
        }
        
        if (params.containsKey("responseStatus") && params.get("responseStatus") != null) {
            wrapper.eq(ApiAccessLog::getResponseStatus, params.get("responseStatus"));
        }
        
        return wrapper;
    }
}
