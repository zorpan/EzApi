package com.newx.system.controller;

import com.newx.api.auth.entity.User;
import com.newx.api.auth.service.UserService;
import com.newx.api.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            User user = userService.authenticate(username, password);
            if (user != null && "ACTIVE".equals(user.getStatus())) {
                // 生成JWT令牌
                String token = jwtUtil.generateToken(username);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", user);
                response.put("token", token); // 添加JWT令牌到响应
                response.put("message", "登录成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户名或密码错误，或账户已被禁用");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


}
