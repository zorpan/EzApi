package com.newx.ezapi.system.controller;

import com.newx.ezapi.api.auth.entity.User;
import com.newx.ezapi.api.auth.service.UserService;
import com.newx.ezapi.api.auth.util.JwtUtil;
import com.newx.ezapi.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            User user = userService.authenticate(username, password);
            if (user != null && "ACTIVE".equals(user.getStatus())) {
                // 生成JWT令牌
                String token = jwtUtil.generateToken(username);

                Map<String, Object> data = new HashMap<>();
                data.put("user", user);
                data.put("token", token);
                
                return Result.success(data, "登录成功");
            } else {
                return Result.error(401, "用户名或密码错误，或账户已被禁用");
            }
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }


}