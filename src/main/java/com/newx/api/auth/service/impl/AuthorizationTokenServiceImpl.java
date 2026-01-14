package com.newx.api.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.newx.api.auth.entity.AuthorizationToken;
import com.newx.api.auth.entity.TokenApiRelation;
import com.newx.api.auth.mapper.AuthorizationTokenMapper;
import com.newx.api.auth.mapper.TokenApiRelationMapper;
import com.newx.api.auth.service.AuthorizationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AuthorizationTokenServiceImpl implements AuthorizationTokenService {
    
    @Autowired
    private AuthorizationTokenMapper tokenMapper;
    
    @Autowired
    private TokenApiRelationMapper tokenApiRelationMapper;
    
    // 存储令牌的访问计数，实际应用中可能需要使用Redis
    private final Map<String, Integer> accessCountMap = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAccessTimeMap = new ConcurrentHashMap<>();
    
    @Override
    public AuthorizationToken createToken(AuthorizationToken token) {
        if (token.getTokenValue() == null || token.getTokenValue().isEmpty()) {
            // 自动生成令牌值
            token.setTokenValue(generateTokenValue());
        }
        if (token.getCreateTime() == null) {
            token.setCreateTime(LocalDateTime.now());
        }
        if (token.getStatus() == null) {
            token.setStatus("ACTIVE");
        }
        
        // 保存令牌
        if(token.getId() == null) {
            tokenMapper.insert(token);
        }else {
            tokenMapper.updateById(token);
        }
        
        // 如果提供了API ID列表，则建立关联
        if (token.getApiIds() != null && !token.getApiIds().isEmpty()) {
            associateApisToToken(token.getId(), token.getApiIds());
        }
        
        return token;
    }
    
    @Override
    public AuthorizationToken getTokenById(Long id) {
        return tokenMapper.selectById(id);
    }
    
    @Override
    public AuthorizationToken getValidTokenByValue(String tokenValue) {
        // 根据token value 查询数据
        AuthorizationToken token = tokenMapper.selectOne(Wrappers.<AuthorizationToken>lambdaQuery()
                .eq(AuthorizationToken::getTokenValue, tokenValue));
        if (token != null &&
            "ACTIVE".equals(token.getStatus()) && 
            (token.getExpireTime() == null || token.getExpireTime().isAfter(LocalDateTime.now()))) {
            return token;
        }
        return null;
    }
    
    @Override
    public List<AuthorizationToken> getTokensByApiId(Long apiId) {
        // 通过关联表查询与指定API ID关联的令牌
        List<TokenApiRelation> relations = tokenApiRelationMapper.selectList(
            Wrappers.<TokenApiRelation>lambdaQuery().eq(TokenApiRelation::getApiId, apiId)
        );
        
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> tokenIds = relations.stream()
            .map(TokenApiRelation::getTokenId)
            .collect(Collectors.toList());
            
        QueryWrapper<AuthorizationToken> wrapper = new QueryWrapper<>();
        wrapper.in("id", tokenIds);
        wrapper.eq("status", "ACTIVE"); // 只返回活跃的令牌
        
        return tokenMapper.selectList(wrapper);
    }
    
    @Override
    public List<AuthorizationToken> getAllTokens() {
        LambdaQueryWrapper<AuthorizationToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AuthorizationToken::getId); // 按ID降序排列，最新的在前
        return tokenMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional
    public boolean updateToken(AuthorizationToken token) {
        // 更新令牌基本信息
        boolean tokenUpdated = tokenMapper.updateById(token) > 0;
        
        if (tokenUpdated && token.getApiIds() != null) {
            // 删除原有的关联关系
            removeTokenApiAssociations(token.getId());
            
            // 如果提供了新的API ID列表，则建立新关联
            if (!token.getApiIds().isEmpty()) {
                associateApisToToken(token.getId(), token.getApiIds());
            }
        }
        
        return tokenUpdated;
    }
    
    @Override
    public boolean deleteToken(Long id) {
        // 删除关联关系
        removeTokenApiAssociations(id);
        
        // 删除令牌
        return tokenMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean isValidToken(String tokenValue) {
        return getValidTokenByValue(tokenValue) != null;
    }
    
    @Override
    public boolean hasAccessPermission(String tokenValue, Long apiId) {
        AuthorizationToken token = getValidTokenByValue(tokenValue);
        if (token == null) {
            return false;
        }
        
        // 检查是否为通用令牌（未关联任何特定API）
        List<Long> tokenApiIds = getApiIdsByTokenId(token.getId());
        if (tokenApiIds.isEmpty()) {
            // 如果没有关联任何API，则允许访问任何API
            return true;
        }
        
        // 检查令牌是否关联了指定的API
        return tokenApiIds.contains(apiId);
    }
    
    @Override
    public boolean checkRateLimit(String tokenValue, Long apiId) {
        AuthorizationToken token = getValidTokenByValue(tokenValue);
        if (token == null || !token.getEnableRateLimit()) {
            return true; // 如果没有启用速率限制，则允许访问
        }
        
        String key = tokenValue + ":" + apiId;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastHour = now.minusHours(1);
        
        // 检查是否在缓存中记录了上次访问时间
        LocalDateTime lastAccess = lastAccessTimeMap.get(key);
        if (lastAccess == null || lastAccess.isBefore(lastHour)) {
            // 如果超过一小时没访问，重置计数
            accessCountMap.put(key, 1);
            lastAccessTimeMap.put(key, now);
            return true;
        }
        
        // 检查当前小时内访问次数
        int currentCount = accessCountMap.getOrDefault(key, 0);
        if (currentCount >= token.getRateLimitPerHour()) {
            return false; // 超出访问限制
        }
        
        // 增加访问计数
        accessCountMap.put(key, currentCount + 1);
        return true;
    }
    
    @Override
    public void incrementAccessCount(String tokenValue, Long apiId) {
        String key = tokenValue + ":" + apiId;
        int currentCount = accessCountMap.getOrDefault(key, 0);
        accessCountMap.put(key, currentCount + 1);
        lastAccessTimeMap.put(key, LocalDateTime.now());
    }
    
    @Override
    public boolean toggleTokenStatus(Long id) {
        AuthorizationToken token = tokenMapper.selectById(id);
        if (token == null) {
            return false;
        }
        
        // 切换状态：ACTIVE <-> INACTIVE
        if ("ACTIVE".equals(token.getStatus())) {
            token.setStatus("INACTIVE");
        } else {
            token.setStatus("ACTIVE");
        }
        
        return tokenMapper.updateById(token) > 0;
    }
    
    @Override
    @Transactional
    public boolean associateApisToToken(Long tokenId, List<Long> apiIds) {
        // 先移除旧的关联关系
        removeTokenApiAssociations(tokenId);

        for (Long apiId : apiIds) {
            // 创建新的关联关系
            TokenApiRelation relation = new TokenApiRelation();
            relation.setTokenId(tokenId);
            relation.setApiId(apiId);
            relation.setCreateTime(LocalDateTime.now());
            tokenApiRelationMapper.insert(relation);
        }
        return true;
    }
    
    @Override
    public List<Long> getApiIdsByTokenId(Long tokenId) {
        List<TokenApiRelation> relations = tokenApiRelationMapper.selectList(
            Wrappers.<TokenApiRelation>lambdaQuery().eq(TokenApiRelation::getTokenId, tokenId)
        );
        
        return relations.stream()
            .map(TokenApiRelation::getApiId)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean removeTokenApiAssociation(Long tokenId, Long apiId) {
        return tokenApiRelationMapper.delete(
            Wrappers.<TokenApiRelation>lambdaQuery()
                .eq(TokenApiRelation::getTokenId, tokenId)
                .eq(TokenApiRelation::getApiId, apiId)
        ) > 0;
    }
    
    private String generateTokenValue() {
        // 生成随机令牌值，实际应用中应该使用更安全的方法
        return "token_" + System.currentTimeMillis() + "_" + Math.random();
    }
    
    private void removeTokenApiAssociations(Long tokenId) {
        tokenApiRelationMapper.delete(
            Wrappers.<TokenApiRelation>lambdaQuery().eq(TokenApiRelation::getTokenId, tokenId)
        );
    }
}