package com.newx.ezapi.core.service;

import com.newx.ezapi.core.entity.ApiInfo;
import com.newx.ezapi.core.entity.enums.ProtocolType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API 执行编排器
 * <p>
 * 根据 ApiInfo 的 protocolType 将执行请求路由到对应的 {@link ApiExecutor} 实现。
 * 新增协议类型时，只需创建新的 ApiExecutor 实现并注册到 Spring 容器即可自动被识别。
 * </p>
 */
@Component
public class ApiExecuteOrchestrator {

    /**
     * 协议类型 → 执行器的映射缓存
     */
    private final Map<String, ApiExecutor> executorMap = new HashMap<>();

    @Autowired
    private List<ApiExecutor> executorList;

    /**
     * 初始化：将所有 ApiExecutor 实现按 protocolType 注册到缓存
     */
    @PostConstruct
    public void init() {
        for (ApiExecutor executor : executorList) {
            String protocolType = executor.getProtocolType();
            if (executorMap.containsKey(protocolType)) {
                throw new IllegalStateException(
                        "重复的协议执行器: protocolType=" + protocolType
                                + ", 冲突类=" + executor.getClass().getName()
                                + " 和 " + executorMap.get(protocolType).getClass().getName()
                );
            }
            executorMap.put(protocolType, executor);
        }
    }

    /**
     * 根据 API 定义的 protocolType 执行对应协议
     *
     * @param apiInfo    API 定义
     * @param parameters 经过类型转换后的参数
     * @return 执行结果
     * @throws Exception 执行异常
     */
    public Object execute(ApiInfo apiInfo, Map<String, Object> parameters) throws Exception {
        String protocolCode = apiInfo.getProtocolType();
        if (protocolCode == null || protocolCode.isEmpty()) {
            protocolCode = ProtocolType.SQL.getCode(); // 默认 SQL
        }

        ApiExecutor executor = executorMap.get(protocolCode);
        if (executor == null) {
            throw new UnsupportedOperationException("不支持的协议类型: " + protocolCode);
        }

        return executor.execute(apiInfo, parameters);
    }
}
