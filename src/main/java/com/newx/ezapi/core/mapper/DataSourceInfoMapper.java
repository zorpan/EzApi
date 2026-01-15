package com.newx.ezapi.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.ezapi.core.entity.DataSourceInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源信息Mapper接口
 */
@Mapper
public interface DataSourceInfoMapper extends BaseMapper<DataSourceInfo> {
}