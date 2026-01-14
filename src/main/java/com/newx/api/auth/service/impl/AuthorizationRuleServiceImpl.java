package com.newx.api.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.newx.api.auth.entity.AuthorizationRule;
import com.newx.api.auth.entity.User;
import com.newx.api.auth.mapper.AuthorizationRuleMapper;
import com.newx.api.auth.mapper.UserMapper;
import com.newx.api.auth.service.AuthorizationRuleService;
import com.newx.api.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthorizationRuleServiceImpl implements AuthorizationRuleService {
    
    @Autowired
    private AuthorizationRuleMapper ruleMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserService userService;
    
    @Override
    public AuthorizationRule createRule(AuthorizationRule rule) {
        if (rule.getCreateTime() == null) {
            rule.setCreateTime(LocalDateTime.now());
        }
        if (rule.getUpdateTime() == null) {
            rule.setUpdateTime(LocalDateTime.now());
        }
        if (rule.getStatus() == null) {
            rule.setStatus("ACTIVE");
        }
        
        ruleMapper.insert(rule);
        return rule;
    }
    
    @Override
    public AuthorizationRule getRuleById(Long id) {
        return ruleMapper.selectById(id);
    }
    
    @Override
    public List<AuthorizationRule> getRulesByApiId(Long apiId) {
        QueryWrapper<AuthorizationRule> wrapper = new QueryWrapper<>();
        wrapper.eq("api_id", apiId).eq("status", "ACTIVE");
        return ruleMapper.selectList(wrapper);
    }
    
    @Override
    public List<AuthorizationRule> getRulesByApiIdAndType(Long apiId, String ruleType) {
        QueryWrapper<AuthorizationRule> wrapper = new QueryWrapper<>();
        wrapper.eq("api_id", apiId).eq("rule_type", ruleType).eq("status", "ACTIVE");
        return ruleMapper.selectList(wrapper);
    }
    
    @Override
    public List<AuthorizationRule> getAllRules() {
        LambdaQueryWrapper<AuthorizationRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AuthorizationRule::getId); // 按ID降序排列，最新的在前
        return ruleMapper.selectList(wrapper);
    }
    
    @Override
    public boolean updateRule(AuthorizationRule rule) {
        rule.setUpdateTime(LocalDateTime.now());
        return ruleMapper.updateById(rule) > 0;
    }
    
    @Override
    public boolean deleteRule(Long id) {
        return ruleMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean hasPermission(String username, Long apiId) {
        // 检查IP白名单规则
        if (!hasIpPermission(getUserIpAddress(username), apiId)) {
            return false;
        }
        
        // 检查角色权限规则
        if (!hasRolePermission(username, apiId)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean hasIpPermission(String ipAddress, Long apiId) {
        List<AuthorizationRule> ipWhitelistRules = getRulesByApiIdAndType(apiId, "IP_WHITELIST");
        if (ipWhitelistRules.isEmpty()) {
            // 如果没有IP白名单规则，则允许访问
            return true;
        }
        
        // 检查IP是否在白名单中
        for (AuthorizationRule rule : ipWhitelistRules) {
            if (rule.getRuleContent() != null) {
                JSONObject ruleJson = JSON.parseObject(rule.getRuleContent());
                String[] whitelist = ruleJson.getString("whitelist").split(",");
                for (String whitelistedIp : whitelist) {
                    if (whitelistedIp.trim().equals(ipAddress)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean hasRolePermission(String username, Long apiId) {
        List<AuthorizationRule> roleRules = getRulesByApiIdAndType(apiId, "ROLE_BASED");
        if (roleRules.isEmpty()) {
            // 如果没有角色权限规则，则允许访问
            return true;
        }
        
        User user = userService.findByUsername(username);
        if (user == null) {
            return false;
        }
        
        // 检查用户角色是否满足规则
        for (AuthorizationRule rule : roleRules) {
            if (rule.getRuleContent() != null) {
                JSONObject ruleJson = JSON.parseObject(rule.getRuleContent());
                String[] allowedRoles = ruleJson.getString("allowedRoles").split(",");
                for (String allowedRole : allowedRoles) {
                    if (allowedRole.trim().equals(user.getRole())) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    // 辅助方法，获取用户IP地址（模拟）
    private String getUserIpAddress(String username) {
        // 在实际应用中，这里应该是从请求上下文获取IP地址
        // 这里只是一个模拟实现
        return "192.168.1.1";
    }
}