package com.newx.ezapi.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.ezapi.common.entity.ApiAccessLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * API访问日志Mapper接口
 */
@Mapper
public interface ApiAccessLogMapper extends BaseMapper<ApiAccessLog> {
}
