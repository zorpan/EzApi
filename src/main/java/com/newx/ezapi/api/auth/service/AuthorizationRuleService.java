package com.newx.ezapi.api.auth.service;

import com.newx.ezapi.api.auth.entity.AuthorizationRule;

import java.util.List;

public interface AuthorizationRuleService {
    /**
     * 创建新的授权规则
     */
    AuthorizationRule createRule(AuthorizationRule rule);
    
    /**
     * 根据ID获取授权规则
     */
    AuthorizationRule getRuleById(Long id);
    
    /**
     * 获取所有授权规则
     */
    List<AuthorizationRule> getAllRules();
    
    /**
     * 根据API ID获取相关的授权规则
     */
    List<AuthorizationRule> getRulesByApiId(Long apiId);
    
    /**
     * 根据API ID和规则类型获取授权规则
     */
    List<AuthorizationRule> getRulesByApiIdAndType(Long apiId, String ruleType);
    
    /**
     * 更新授权规则
     */
    boolean updateRule(AuthorizationRule rule);
    
    /**
     * 删除授权规则
     */
    boolean deleteRule(Long id);
    
    /**
     * 检查指定用户是否有访问API的权限
     */
    boolean hasPermission(String username, Long apiId);
    
    /**
     * 检查IP地址是否有访问API的权限
     */
    boolean hasIpPermission(String ipAddress, Long apiId);
    
    /**
     * 检查用户角色是否有访问API的权限
     */
    boolean hasRolePermission(String username, Long apiId);
}