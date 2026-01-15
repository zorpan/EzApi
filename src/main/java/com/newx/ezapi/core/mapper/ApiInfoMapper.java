package com.newx.ezapi.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.ezapi.core.entity.ApiInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * API信息Mapper接口
 */
@Mapper
public interface ApiInfoMapper extends BaseMapper<ApiInfo> {
}