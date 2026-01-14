package com.newx.api.auth.service;

import com.newx.api.auth.entity.User;

public interface UserService {
    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);
    
    /**
     * 根据用户名和密码验证用户
     */
    User authenticate(String username, String password);
    
    /**
     * 创建新用户
     */
    User createUser(User user);
    
    /**
     * 更新用户信息
     */
    boolean updateUser(User user);
    
    /**
     * 更改用户密码
     */
    boolean changePassword(String username, String oldPassword, String newPassword);
    
    /**
     * 启用/禁用用户
     */
    boolean toggleUserStatus(String username, String status);
    
    /**
     * 获取所有用户
     */
    java.util.List<User> getAllUsers();
}