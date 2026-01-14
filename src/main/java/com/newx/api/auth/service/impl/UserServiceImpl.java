package com.newx.api.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.newx.api.auth.entity.User;
import com.newx.api.auth.mapper.UserMapper;
import com.newx.api.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    public User authenticate(String username, String password) {
        // 对密码进行MD5加密后再比较
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        return userMapper.findByUsernameAndPassword(username, encryptedPassword);
    }
    
    @Override
    public User createUser(User user) {
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        if (user.getUpdateTime() == null) {
            user.setUpdateTime(LocalDateTime.now());
        }
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        
        // 对密码进行加密
        if (user.getPassword() != null) {
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }
        
        userMapper.insert(user);
        return user;
    }
    
    @Override
    public boolean updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }
    
    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = findByUsername(username);
        if (user == null) {
            return false;
        }
        
        String encryptedOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!user.getPassword().equals(encryptedOldPassword)) {
            return false; // 旧密码不正确
        }
        
        String encryptedNewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        user.setPassword(encryptedNewPassword);
        user.setUpdateTime(LocalDateTime.now());
        
        return userMapper.updateById(user) > 0;
    }
    
    @Override
    public boolean toggleUserStatus(String username, String status) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        
        User user = new User();
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        
        return userMapper.update(user, wrapper) > 0;
    }
    
    @Override
    public java.util.List<User> getAllUsers() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id"); // 按ID降序排列，最新的在前
        return userMapper.selectList(wrapper);
    }
}