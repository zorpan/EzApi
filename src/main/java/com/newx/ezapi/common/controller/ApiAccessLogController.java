package com.newx.ezapi.common.controller;

import com.newx.ezapi.common.entity.ApiAccessLog;
import com.newx.ezapi.common.result.Result;
import com.newx.ezapi.common.service.ApiAccessLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API访问日志控制器
 */
@RestController
@RequestMapping("/api/logs")
public class ApiAccessLogController {
    
    @Autowired
    private ApiAccessLogService apiAccessLogService;
    
    /**
     * 获取日志列表（分页）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getLogList(
            @RequestParam(required = false) Long apiId,
            @RequestParam(required = false) String apiPath,
            @RequestParam(required = false) String httpMethod,
            @RequestParam(required = false) String clientIp,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer responseStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("apiId", apiId);
            params.put("apiPath", apiPath);
            params.put("httpMethod", httpMethod);
            params.put("clientIp", clientIp);
            params.put("username", username);
            params.put("responseStatus", responseStatus);
            params.put("startTime", startTime);
            params.put("endTime", endTime);
            params.put("pageNum", pageNum);
            params.put("pageSize", pageSize);
            
            List<ApiAccessLog> logs = apiAccessLogService.getLogList(params);
            Long total = apiAccessLogService.getLogCount(params);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", logs);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取日志列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取日志详情
     */
    @GetMapping("/{id}")
    public Result<ApiAccessLog> getLogById(@PathVariable Long id) {
        try {
            ApiAccessLog log = apiAccessLogService.getLogById(id);
            if (log != null) {
                return Result.success(log);
            } else {
                return Result.error(404, "日志不存在");
            }
        } catch (Exception e) {
            return Result.error("获取日志详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据请求ID获取日志
     */
    @GetMapping("/request/{requestId}")
    public Result<ApiAccessLog> getLogByRequestId(@PathVariable String requestId) {
        try {
            ApiAccessLog log = apiAccessLogService.getLogByRequestId(requestId);
            if (log != null) {
                return Result.success(log);
            } else {
                return Result.error(404, "日志不存在");
            }
        } catch (Exception e) {
            return Result.error("获取日志详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除指定时间之前的日志
     */
    @DeleteMapping("/before")
    public Result<String> deleteLogsBefore(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        try {
            int deletedCount = apiAccessLogService.deleteLogsBefore(beforeTime);
            return Result.success("成功删除 " + deletedCount + " 条日志");
        } catch (Exception e) {
            return Result.error("删除日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 清空所有日志
     */
    @DeleteMapping("/clear")
    public Result<String> clearAllLogs() {
        try {
            int deletedCount = apiAccessLogService.clearAllLogs();
            return Result.success("成功清空 " + deletedCount + " 条日志");
        } catch (Exception e) {
            return Result.error("清空日志失败: " + e.getMessage());
        }
    }
}
