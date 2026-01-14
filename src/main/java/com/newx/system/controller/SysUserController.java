package com.newx.system.controller;


import com.newx.api.auth.entity.User;
import com.newx.api.auth.service.UserService;
import com.newx.api.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    @Autowired
    private UserService userService;


    /**
     * 创建用户
     */
    @PostMapping("/user/create")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取所有用户
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    /**
     * 更新用户信息
     */
    @PutMapping("/user/update")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody User user) {
        try {
            // 获取原用户信息以保留密码
            User existingUser = userService.findByUsername(user.getUsername());
            if (existingUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 保留原密码
            user.setPassword(existingUser.getPassword());
            // 保留原ID
            user.setId(existingUser.getId());

            boolean result = userService.updateUser(user);
            if (result) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "用户信息更新成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户信息更新失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新用户信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 修改用户密码
     */
    @PostMapping("/user/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            if (username == null || oldPassword == null || newPassword == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户名、旧密码和新密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            boolean result = userService.changePassword(username, oldPassword, newPassword);
            if (result) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "密码修改成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "密码修改失败，请检查旧密码是否正确");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "修改密码失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


}
