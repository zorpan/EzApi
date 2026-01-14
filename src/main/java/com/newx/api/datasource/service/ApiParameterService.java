package com.newx.api.datasource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.newx.api.datasource.entity.ApiParameter;
import java.util.List;

/**
 * API参数服务接口
 */
public interface ApiParameterService extends IService<ApiParameter> {
    /**
     * 根据API ID获取参数列表
     */
    List<ApiParameter> getParametersByApiId(Long apiId);

    /**
     * 保存API参数
     */
    boolean saveApiParameter(ApiParameter apiParameter);

    /**
     * 批量保存API参数
     */
    boolean saveApiParameters(List<ApiParameter> apiParameters);

    /**
     * 根据API ID删除参数
     */
    boolean deleteParametersByApiId(Long apiId);

    /**
     * 根据参数ID删除参数
     */
    boolean deleteParameterById(Long id);
}