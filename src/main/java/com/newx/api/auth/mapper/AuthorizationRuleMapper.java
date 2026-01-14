package com.newx.api.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.api.auth.entity.AuthorizationRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuthorizationRuleMapper extends BaseMapper<AuthorizationRule> {
}