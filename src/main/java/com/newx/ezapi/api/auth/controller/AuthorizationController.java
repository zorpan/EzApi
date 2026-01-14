package com.newx.ezapi.api.auth.controller;

import com.newx.ezapi.api.auth.entity.AuthorizationRule;
import com.newx.ezapi.api.auth.entity.AuthorizationToken;
import com.newx.ezapi.api.auth.service.AuthorizationRuleService;
import com.newx.ezapi.api.auth.service.AuthorizationTokenService;
import com.newx.ezapi.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthorizationController {
    
    @Autowired
    private AuthorizationTokenService tokenService;
    
    @Autowired
    private AuthorizationRuleService ruleService;

    
    // 授权令牌管理
    
    /**
     * 创建授权令牌
     */
    @PostMapping("/token/create")
    public Result<AuthorizationToken> createToken(@RequestBody AuthorizationToken token) {
        try {
            AuthorizationToken createdToken = tokenService.createToken(token);
            return Result.success(createdToken, "创建授权令牌成功");
        } catch (Exception e) {
            return Result.error("创建授权令牌失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取授权令牌详情
     */
    @GetMapping("/token/{id}")
    public Result<AuthorizationToken> getToken(@PathVariable Long id) {
        try {
            AuthorizationToken token = tokenService.getTokenById(id);
            if (token != null) {
                // 获取关联的API ID列表
                token.setApiIds(tokenService.getApiIdsByTokenId(id));
                
                return Result.success(token);
            } else {
                return Result.error(404, "授权令牌不存在");
            }
        } catch (Exception e) {
            return Result.error("获取授权令牌失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据API ID获取相关的授权令牌
     */
    @GetMapping("/tokens/api/{apiId}")
    public Result<List<AuthorizationToken>> getTokensByApiId(@PathVariable Long apiId) {
        try {
            List<AuthorizationToken> tokens = tokenService.getTokensByApiId(apiId);
            return Result.success(tokens);
        } catch (Exception e) {
            return Result.error("获取授权令牌列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有授权令牌
     */
    @GetMapping("/tokens")
    public Result<List<AuthorizationToken>> getAllTokens() {
        try {
            List<AuthorizationToken> tokens = tokenService.getAllTokens();
            // 为每个令牌获取关联的API ID列表
            for (AuthorizationToken token : tokens) {
                token.setApiIds(tokenService.getApiIdsByTokenId(token.getId()));
            }
            
            return Result.success(tokens);
        } catch (Exception e) {
            return Result.error("获取授权令牌列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除授权令牌
     */
    @DeleteMapping("/token/{id}")
    public Result<String> deleteToken(@PathVariable Long id) {
        try {
            boolean result = tokenService.deleteToken(id);
            if (result) {
                return Result.success("授权令牌删除成功", "授权令牌删除成功");
            } else {
                return Result.error("授权令牌删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除授权令牌失败: " + e.getMessage());
        }
    }
    
    /**
     * 切换API密钥状态
     */
    @PutMapping("/token/toggle-status/{id}")
    public Result<String> toggleTokenStatus(@PathVariable Long id) {
        try {
            boolean result = tokenService.toggleTokenStatus(id);
            if (result) {
                return Result.success("API密钥状态切换成功", "API密钥状态切换成功");
            } else {
                return Result.error("API密钥状态切换失败");
            }
        } catch (Exception e) {
            return Result.error("切换API密钥状态失败: " + e.getMessage());
        }
    }
    
    // 授权规则管理
    
    /**
     * 创建授权规则
     */
    @PostMapping("/rule/create")
    public Result<AuthorizationRule> createRule(@RequestBody AuthorizationRule rule) {
        try {
            AuthorizationRule createdRule = ruleService.createRule(rule);
            return Result.success(createdRule, "创建授权规则成功");
        } catch (Exception e) {
            return Result.error("创建授权规则失败: " + e.getMessage());
        }
    }

    /**
     * 更新授权规则
     */
    @PutMapping("/rule/update")
    public Result<String> updateRule(@RequestBody AuthorizationRule rule) {
        try {
            boolean result = ruleService.updateRule(rule);
            if (result) {
                return Result.success("授权规则更新成功", "授权规则更新成功");
            } else {
                return Result.error("授权规则更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新授权规则失败: " + e.getMessage());
        }
    }

    /**
     * 获取授权规则详情
     */
    @GetMapping("/rule/{id}")
    public Result<AuthorizationRule> getRule(@PathVariable Long id) {
        try {
            AuthorizationRule rule = ruleService.getRuleById(id);
            if (rule != null) {
                return Result.success(rule);
            } else {
                return Result.error(404, "授权规则不存在");
            }
        } catch (Exception e) {
            return Result.error("获取授权规则失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有授权规则
     */
    @GetMapping("/rules")
    public Result<List<AuthorizationRule>> getAllRules() {
        try {
            List<AuthorizationRule> rules = ruleService.getAllRules();
            return Result.success(rules);
        } catch (Exception e) {
            return Result.error("获取授权规则列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除授权规则
     */
    @DeleteMapping("/rule/{id}")
    public Result<String> deleteRule(@PathVariable Long id) {
        try {
            boolean result = ruleService.deleteRule(id);
            if (result) {
                return Result.success("授权规则删除成功", "授权规则删除成功");
            } else {
                return Result.error("授权规则删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除授权规则失败: " + e.getMessage());
        }
    }
}