package com.newx.api.auth.controller;

import com.newx.api.auth.entity.AuthorizationToken;
import com.newx.api.auth.entity.AuthorizationRule;
import com.newx.api.auth.entity.User;
import com.newx.api.auth.service.AuthorizationTokenService;
import com.newx.api.auth.service.AuthorizationRuleService;
import com.newx.api.auth.service.UserService;
import com.newx.api.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> createToken(@RequestBody AuthorizationToken token) {
        try {
            AuthorizationToken createdToken = tokenService.createToken(token);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建授权令牌失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取授权令牌详情
     */
    @GetMapping("/token/{id}")
    public ResponseEntity<Map<String, Object>> getToken(@PathVariable Long id) {
        try {
            AuthorizationToken token = tokenService.getTokenById(id);
            if (token != null) {
                // 获取关联的API ID列表
                token.setApiIds(tokenService.getApiIdsByTokenId(id));
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", token);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "授权令牌不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取授权令牌失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据API ID获取相关的授权令牌
     */
    @GetMapping("/tokens/api/{apiId}")
    public ResponseEntity<Map<String, Object>> getTokensByApiId(@PathVariable Long apiId) {
        try {
            List<AuthorizationToken> tokens = tokenService.getTokensByApiId(apiId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tokens);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取授权令牌列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取所有授权令牌
     */
    @GetMapping("/tokens")
    public ResponseEntity<Map<String, Object>> getAllTokens() {
        try {
            List<AuthorizationToken> tokens = tokenService.getAllTokens();
            // 为每个令牌获取关联的API ID列表
            for (AuthorizationToken token : tokens) {
                token.setApiIds(tokenService.getApiIdsByTokenId(token.getId()));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tokens);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取授权令牌列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除授权令牌
     */
    @DeleteMapping("/token/{id}")
    public ResponseEntity<Map<String, Object>> deleteToken(@PathVariable Long id) {
        try {
            boolean result = tokenService.deleteToken(id);
            if (result) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "授权令牌删除成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "授权令牌删除失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除授权令牌失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 授权规则管理
    
    /**
     * 创建授权规则
     */
    @PostMapping("/rule/create")
    public ResponseEntity<Map<String, Object>> createRule(@RequestBody AuthorizationRule rule) {
        try {
            AuthorizationRule createdRule = ruleService.createRule(rule);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdRule);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建授权规则失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }



    /**
     * 切换API密钥状态
     */
    @PutMapping("/token/toggle-status/{id}")
    public ResponseEntity<Map<String, Object>> toggleTokenStatus(@PathVariable Long id) {
        try {
            boolean result = tokenService.toggleTokenStatus(id);
            if (result) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "API密钥状态切换成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "API密钥状态切换失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "切换API密钥状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}