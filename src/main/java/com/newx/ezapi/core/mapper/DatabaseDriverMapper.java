package com.newx.ezapi.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newx.ezapi.core.entity.DatabaseDriver;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据库驱动Mapper接口
 */
@Mapper
public interface DatabaseDriverMapper extends BaseMapper<DatabaseDriver> {
}