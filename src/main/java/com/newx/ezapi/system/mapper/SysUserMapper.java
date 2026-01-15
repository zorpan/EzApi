package com.newx.ezapi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.ezapi.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("select * from sys_user where username = #{username}")
    SysUser findBySysUsername(@Param("username") String username);

    @Select("select * from sys_user where username = #{username} and password = #{password}")
    SysUser findBySysUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}