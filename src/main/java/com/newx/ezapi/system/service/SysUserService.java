package com.newx.ezapi.system.service;

import com.newx.ezapi.system.entity.SysUser;

public interface SysUserService {
    /**
     * 根据用户名查找用户
     */
    SysUser findByUsername(String username);
    
    /**
     * 根据用户名和密码验证用户
     */
    SysUser authenticate(String username, String password);
    
    /**
     * 创建新用户
     */
    SysUser createUser(SysUser user);
    
    /**
     * 更新用户信息
     */
    boolean updateUser(SysUser user);
    
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
    java.util.List<SysUser> getAllUsers();
}