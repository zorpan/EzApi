package com.newx.ezapi.api.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.ezapi.api.datasource.entity.ApiParameter;
import org.apache.ibatis.annotations.Mapper;

/**
 * API参数Mapper接口
 */
@Mapper
public interface ApiParameterMapper extends BaseMapper<ApiParameter> {
}