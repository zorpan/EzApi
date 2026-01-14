package com.newx.ezapi.api.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.ezapi.api.auth.entity.AuthorizationRule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthorizationRuleMapper extends BaseMapper<AuthorizationRule> {
}