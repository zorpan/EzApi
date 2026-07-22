package com.newx.ezapi.common.service;

import com.newx.ezapi.common.entity.ApiAccessLog;

import java.util.List;
import java.util.Map;

/**
 * API访问日志服务接口
 */
public interface ApiAccessLogService {
    
    /**
     * 保存访问日志
     * @param log 日志对象
     * @return 保存后的日志对象
     */
    ApiAccessLog saveLog(ApiAccessLog log);
    
    /**
     * 根据ID获取日志
     * @param id 日志ID
     * @return 日志对象
     */
    ApiAccessLog getLogById(Long id);
    
    /**
     * 根据请求ID获取日志
     * @param requestId 请求ID
     * @return 日志对象
     */
    ApiAccessLog getLogByRequestId(String requestId);
    
    /**
     * 获取日志列表（分页）
     * @param params 查询参数
     * @return 日志列表
     */
    List<ApiAccessLog> getLogList(Map<String, Object> params);
    
    /**
     * 获取日志总数
     * @param params 查询参数
     * @return 总数
     */
    Long getLogCount(Map<String, Object> params);
    
    /**
     * 删除指定时间之前的日志
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    int deleteLogsBefore(java.time.LocalDateTime beforeTime);
    
    /**
     * 清空所有日志
     * @return 删除的记录数
     */
    int clearAllLogs();
}
