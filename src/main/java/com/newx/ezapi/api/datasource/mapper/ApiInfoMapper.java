package com.newx.ezapi.api.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.ezapi.api.datasource.entity.ApiInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * API信息Mapper接口
 */
@Mapper
public interface ApiInfoMapper extends BaseMapper<ApiInfo> {
}