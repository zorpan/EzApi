package com.newx.ezapi.auth.controller;

import com.newx.ezapi.auth.entity.AuthorizationToken;
import com.newx.ezapi.auth.service.AuthorizationTokenService;
import com.newx.ezapi.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthorizationTokenController {
    
    @Autowired
    private AuthorizationTokenService tokenService;
    
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
    

}