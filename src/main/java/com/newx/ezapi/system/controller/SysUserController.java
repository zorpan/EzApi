package com.newx.ezapi.system.controller;

import com.newx.ezapi.common.result.Result;
import com.newx.ezapi.system.entity.SysUser;
import com.newx.ezapi.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    @Autowired
    private SysUserService userService;


    /**
     * 创建用户
     */
    @PostMapping("/user/create")
    public Result<SysUser> createUser(@RequestBody SysUser user) {
        try {
            SysUser createdUser = userService.createUser(user);
            return Result.success(createdUser, "创建用户成功");
        } catch (Exception e) {
            return Result.error("创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有用户
     */
    @GetMapping("/users")
    public Result<List<SysUser>> getAllUsers() {
        try {
            List<SysUser> users = userService.getAllUsers();
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }


    /**
     * 更新用户信息
     */
    @PutMapping("/user/update")
    public Result<String> updateUser(@RequestBody SysUser user) {
        try {
            // 获取原用户信息以保留密码
            SysUser existingUser = userService.findByUsername(user.getUsername());
            if (existingUser == null) {
                return Result.error("用户不存在");
            }

            // 保留原密码
            user.setPassword(existingUser.getPassword());
            // 保留原ID
            user.setId(existingUser.getId());

            boolean result = userService.updateUser(user);
            if (result) {
                return Result.success("用户信息更新成功", "用户信息更新成功");
            } else {
                return Result.error("用户信息更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 修改用户密码
     */
    @PostMapping("/user/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            if (username == null || oldPassword == null || newPassword == null) {
                return Result.error("用户名、旧密码和新密码不能为空");
            }

            boolean result = userService.changePassword(username, oldPassword, newPassword);
            if (result) {
                return Result.success("密码修改成功", "密码修改成功");
            } else {
                return Result.error("密码修改失败，请检查旧密码是否正确");
            }
        } catch (Exception e) {
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/user/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        try {
            // 查找用户
            List<SysUser> allUsers = userService.getAllUsers();
            SysUser userToDelete = null;
            for (SysUser user : allUsers) {
                if (user.getId().equals(id)) {
                    userToDelete = user;
                    break;
                }
            }
            
            if (userToDelete == null) {
                return Result.error(404, "用户不存在");
            }

            // 在实际应用中，通常采用软删除，即将用户状态改为INACTIVE
            userToDelete.setStatus("INACTIVE");
            boolean result = userService.updateUser(userToDelete);

            if (result) {
                return Result.success("用户删除成功", "用户删除成功");
            } else {
                return Result.error("用户删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除用户失败: " + e.getMessage());
        }
    }
}