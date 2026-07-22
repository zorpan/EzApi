package com.newx.ezapi.api.log;

import com.newx.ezapi.common.entity.ApiAccessLog;
import com.newx.ezapi.common.service.ApiAccessLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API访问日志测试
 */
@SpringBootTest
public class ApiAccessLogTest {
    
    @Autowired
    private ApiAccessLogService apiAccessLogService;
    
    @Test
    public void testSaveLog() {
        ApiAccessLog log = new ApiAccessLog();
        log.setRequestId("test-request-id-001");
        log.setHttpMethod("GET");
        log.setRequestUrl("/api/test");
        log.setClientIp("127.0.0.1");
        log.setCreateTime(LocalDateTime.now());
        log.setExecutionTime(100L);
        
        ApiAccessLog savedLog = apiAccessLogService.saveLog(log);
        
        // 由于是异步保存，等待一下
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 验证日志是否保存成功
        ApiAccessLog retrievedLog = apiAccessLogService.getLogByRequestId("test-request-id-001");
        assertNotNull(retrievedLog, "日志应该被保存");
        assertEquals("GET", retrievedLog.getHttpMethod());
        assertEquals("/api/test", retrievedLog.getRequestUrl());
        assertEquals("127.0.0.1", retrievedLog.getClientIp());
    }
    
    @Test
    public void testGetLogList() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", 1);
        params.put("pageSize", 10);
        
        List<ApiAccessLog> logs = apiAccessLogService.getLogList(params);
        assertNotNull(logs, "日志列表不应为空");
    }
    
    @Test
    public void testDeleteLogsBefore() {
        // 删除一天前的日志
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(1);
        int deletedCount = apiAccessLogService.deleteLogsBefore(beforeTime);
        assertTrue(deletedCount >= 0, "删除的日志数量应大于等于0");
    }
}
