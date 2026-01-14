package com.newx.api.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.api.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User findByUsername(@Param("username") String username);
    User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}