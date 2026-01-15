package com.newx.ezapi.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.newx.ezapi.core.entity.ApiInfo;
import com.newx.ezapi.core.entity.ApiParameter;
import com.newx.ezapi.core.mapper.ApiInfoMapper;
import com.newx.ezapi.core.service.ApiInfoService;
import com.newx.ezapi.core.service.ApiParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * API信息服务实现类
 */
@Service
public class ApiInfoServiceImpl extends ServiceImpl<ApiInfoMapper, ApiInfo> implements ApiInfoService {

    @Autowired
    private ApiParameterService apiParameterService;

    @Override
    public List<ApiInfo> getAllApiInfos() {
        List<ApiInfo> apiInfos = this.list();
        // 为每个API加载参数
        for (ApiInfo apiInfo : apiInfos) {
            List<ApiParameter> parameters = apiParameterService.getParametersByApiId(apiInfo.getId());
            apiInfo.setParameters(parameters);
        }
        return apiInfos;
    }

    @Override
    public ApiInfo getApiInfoById(Long id) {
        ApiInfo apiInfo = this.getById(id);
        if (apiInfo != null) {
            // 加载API的参数
            List<ApiParameter> parameters = apiParameterService.getParametersByApiId(id);
            apiInfo.setParameters(parameters);
        }
        return apiInfo;
    }

    @Override
    public boolean saveApiInfo(ApiInfo apiInfo) {
        // 设置创建时间
        if (apiInfo.getId() == null) {
            apiInfo.setCreatedTime(System.currentTimeMillis());
        }
        apiInfo.setUpdatedTime(System.currentTimeMillis());
        boolean result = this.save(apiInfo);
        
        // 如果API保存成功且包含参数，保存参数
        if (result && apiInfo.getParameters() != null) {
            // 先删除现有参数
            apiParameterService.deleteParametersByApiId(apiInfo.getId());
            
            // 保存新参数
            for (ApiParameter param : apiInfo.getParameters()) {
                param.setApiId(apiInfo.getId());
                apiParameterService.saveApiParameter(param);
            }
        }
        
        return result;
    }

    @Override
    public boolean updateApiInfo(ApiInfo apiInfo) {
        apiInfo.setUpdatedTime(System.currentTimeMillis());
        boolean result = this.updateById(apiInfo);
        
        // 如果API更新成功且包含参数，更新参数
        if (result && apiInfo.getParameters() != null) {
            // 先删除现有参数
            apiParameterService.deleteParametersByApiId(apiInfo.getId());
            
            // 保存新参数
            for (ApiParameter param : apiInfo.getParameters()) {
                param.setApiId(apiInfo.getId());
                apiParameterService.saveApiParameter(param);
            }
        }
        
        return result;
    }

    @Override
    public boolean deleteApiInfo(Long id) {
        // 删除API前先删除参数
        apiParameterService.deleteParametersByApiId(id);
        return this.removeById(id);
    }

    @Override
    public boolean toggleApiStatus(Long id, Integer status) {
        ApiInfo apiInfo = this.getById(id);
        if (apiInfo != null) {
            apiInfo.setStatus(status);
            apiInfo.setUpdatedTime(System.currentTimeMillis());
            return this.updateById(apiInfo);
        }
        return false;
    }

    @Override
    public ApiInfo findByApiPathAndMethod(String apiPath, String apiMethod) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ApiInfo> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("api_path", apiPath).eq("api_method", apiMethod);
        ApiInfo apiInfo = this.getOne(wrapper);
        if (apiInfo != null) {
            // 加载API的参数
            List<ApiParameter> parameters = apiParameterService.getParametersByApiId(apiInfo.getId());
            apiInfo.setParameters(parameters);
        }
        return apiInfo;
    }
}