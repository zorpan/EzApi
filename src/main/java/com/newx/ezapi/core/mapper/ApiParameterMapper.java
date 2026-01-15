package com.newx.ezapi.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.ezapi.core.entity.ApiParameter;
import org.apache.ibatis.annotations.Mapper;

/**
 * API参数Mapper接口
 */
@Mapper
public interface ApiParameterMapper extends BaseMapper<ApiParameter> {
}