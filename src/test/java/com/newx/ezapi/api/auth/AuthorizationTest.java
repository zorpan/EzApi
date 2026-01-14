package com.newx.ezapi.api.auth;

import com.newx.ezapi.api.auth.entity.AuthorizationToken;
import com.newx.ezapi.api.auth.service.AuthorizationTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthorizationTest {
    
    @Autowired
    private AuthorizationTokenService tokenService;
    
    @Test
    public void testTokenCreation() {
        // 创建一个测试令牌
        AuthorizationToken token = new AuthorizationToken();
        token.setTokenName("Test Token");
        token.setTokenType("API_KEY");
        token.setStatus("ACTIVE");
        token.setDescription("Test token for API access");
        
        AuthorizationToken createdToken = tokenService.createToken(token);
        
        assertNotNull(createdToken);
        assertNotNull(createdToken.getTokenValue());
        assertEquals("ACTIVE", createdToken.getStatus());
    }
}