package com.newx.api.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.api.datasource.entity.DataSourceInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源信息Mapper接口
 */
@Mapper
public interface DataSourceInfoMapper extends BaseMapper<DataSourceInfo> {
}