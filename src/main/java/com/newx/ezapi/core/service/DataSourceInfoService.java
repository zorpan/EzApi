package com.newx.ezapi.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.newx.ezapi.core.entity.DataSourceInfo;
import java.util.List;

/**
 * 数据源信息服务接口
 */
public interface DataSourceInfoService extends IService<DataSourceInfo> {
    /**
     * 获取所有数据源信息
     */
    List<DataSourceInfo> getAllDataSourceInfos();

    /**
     * 根据ID获取数据源信息
     */
    DataSourceInfo getDataSourceInfoById(Long id);

    /**
     * 保存数据源信息
     */
    boolean saveDataSourceInfo(DataSourceInfo dataSourceInfo);

    /**
     * 更新数据源信息
     */
    boolean updateDataSourceInfo(DataSourceInfo dataSourceInfo);

    /**
     * 删除数据源信息
     */
    boolean deleteDataSourceInfo(Long id);

    /**
     * 根据ID启用或禁用数据源
     */
    boolean toggleDataSourceStatus(Long id, boolean enabled);
}