package com.newx.api.auth.service;

import com.newx.api.auth.entity.AuthorizationToken;

import java.util.List;

public interface AuthorizationTokenService {
    /**
     * 创建新的授权令牌
     */
    AuthorizationToken createToken(AuthorizationToken token);
    
    /**
     * 根据ID获取授权令牌
     */
    AuthorizationToken getTokenById(Long id);
    
    /**
     * 根据令牌值获取有效的授权令牌
     */
    AuthorizationToken getValidTokenByValue(String tokenValue);
    
    /**
     * 根据API ID获取相关的令牌
     */
    List<AuthorizationToken> getTokensByApiId(Long apiId);
    
    /**
     * 获取所有授权令牌
     */
    List<AuthorizationToken> getAllTokens();
    
    /**
     * 更新授权令牌
     */
    boolean updateToken(AuthorizationToken token);
    
    /**
     * 删除授权令牌
     */
    boolean deleteToken(Long id);
    
    /**
     * 检查令牌是否有效
     */
    boolean isValidToken(String tokenValue);
    
    /**
     * 检查令牌是否有访问指定API的权限
     */
    boolean hasAccessPermission(String tokenValue, Long apiId);
    
    /**
     * 检查令牌的访问频率限制
     */
    boolean checkRateLimit(String tokenValue, Long apiId);
    
    /**
     * 增加令牌的访问计数
     */
    void incrementAccessCount(String tokenValue, Long apiId);
    
    /**
     * 切换令牌状态
     */
    boolean toggleTokenStatus(Long id);
    
    /**
     * 关联API到令牌
     */
    boolean associateApisToToken(Long tokenId, List<Long> apiIds);
    
    /**
     * 获取令牌关联的API ID列表
     */
    List<Long> getApiIdsByTokenId(Long tokenId);
    
    /**
     * 移除令牌与API的关联
     */
    boolean removeTokenApiAssociation(Long tokenId, Long apiId);
}