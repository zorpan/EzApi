package com.newx.ezapi.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.newx.ezapi.core.entity.DataSourceInfo;
import com.newx.ezapi.core.mapper.DataSourceInfoMapper;
import com.newx.ezapi.core.service.DataSourceInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据源信息服务实现类
 */
@Service
public class DataSourceInfoServiceImpl extends ServiceImpl<DataSourceInfoMapper, DataSourceInfo> implements DataSourceInfoService {

    @Override
    public List<DataSourceInfo> getAllDataSourceInfos() {
        return this.list();
    }

    @Override
    public DataSourceInfo getDataSourceInfoById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean saveDataSourceInfo(DataSourceInfo dataSourceInfo) {
        // 设置创建时间
        if (dataSourceInfo.getId() == null) {
            dataSourceInfo.setCreatedTime(System.currentTimeMillis());
        }
        dataSourceInfo.setUpdatedTime(System.currentTimeMillis());
        
        // 如果ID存在且数据库中已有该记录，则更新；否则插入新记录
        if (dataSourceInfo.getId() != null && this.getById(dataSourceInfo.getId()) != null) {
            return this.updateById(dataSourceInfo);
        } else {
            return this.save(dataSourceInfo);
        }
    }

    @Override
    public boolean updateDataSourceInfo(DataSourceInfo dataSourceInfo) {
        dataSourceInfo.setUpdatedTime(System.currentTimeMillis());
        return this.updateById(dataSourceInfo);
    }

    @Override
    public boolean deleteDataSourceInfo(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean toggleDataSourceStatus(Long id, boolean enabled) {
        DataSourceInfo dataSourceInfo = this.getById(id);
        if (dataSourceInfo != null) {
            dataSourceInfo.setEnabled(enabled);
            dataSourceInfo.setUpdatedTime(System.currentTimeMillis());
            return this.updateById(dataSourceInfo);
        }
        return false;
    }
}