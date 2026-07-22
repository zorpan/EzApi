package com.newx.ezapi.core.service;

import com.newx.ezapi.core.entity.ApiInfo;

import java.util.Map;

/**
 * API 协议执行器策略接口
 * <p>
 * 每种协议类型（SQL、WebService、HTTP Proxy 等）实现该接口，
 * 由 ApiExecuteOrchestrator 根据 protocolType 路由到对应的执行器。
 * </p>
 */
public interface ApiExecutor {

    /**
     * 执行 API 调用
     *
     * @param apiInfo    API 定义
     * @param parameters 经过类型转换后的参数
     * @return 执行结果（通常是 Map 或 List 结构，最终由 Controller 转为 JSON）
     * @throws Exception 执行过程中的任何异常
     */
    Object execute(ApiInfo apiInfo, Map<String, Object> parameters) throws Exception;

    /**
     * 返回此执行器支持的协议类型
     *
     * @return 协议类型代码，对应 ProtocolType 的 code
     */
    String getProtocolType();
}
