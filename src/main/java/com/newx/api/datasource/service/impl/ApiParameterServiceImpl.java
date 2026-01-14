package com.newx.api.datasource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.newx.api.datasource.entity.ApiParameter;
import com.newx.api.datasource.mapper.ApiParameterMapper;
import com.newx.api.datasource.service.ApiParameterService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * API参数服务实现类
 */
@Service
public class ApiParameterServiceImpl extends ServiceImpl<ApiParameterMapper, ApiParameter> implements ApiParameterService {

    @Override
    public List<ApiParameter> getParametersByApiId(Long apiId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ApiParameter> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("api_id", apiId);
        return this.list(wrapper);
    }

    @Override
    public boolean saveApiParameter(ApiParameter apiParameter) {
        // 设置创建时间
        if (apiParameter.getId() == null) {
            apiParameter.setCreatedTime(System.currentTimeMillis());
        }
        apiParameter.setUpdatedTime(System.currentTimeMillis());
        return this.save(apiParameter);
    }

    @Override
    public boolean saveApiParameters(List<ApiParameter> apiParameters) {
        for (ApiParameter param : apiParameters) {
            if (param.getId() == null) {
                param.setCreatedTime(System.currentTimeMillis());
            }
            param.setUpdatedTime(System.currentTimeMillis());
            this.save(param);
        }
        return true;
    }

    @Override
    public boolean deleteParametersByApiId(Long apiId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ApiParameter> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("api_id", apiId);
        return this.remove(wrapper);
    }

    @Override
    public boolean deleteParameterById(Long id) {
        return this.removeById(id);
    }
}