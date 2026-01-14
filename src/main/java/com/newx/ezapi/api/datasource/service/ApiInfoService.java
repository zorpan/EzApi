package com.newx.ezapi.api.datasource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.newx.ezapi.api.datasource.entity.ApiInfo;
import java.util.List;

/**
 * API信息服务接口
 */
public interface ApiInfoService extends IService<ApiInfo> {
    /**
     * 获取所有API信息
     */
    List<ApiInfo> getAllApiInfos();

    /**
     * 根据ID获取API信息
     */
    ApiInfo getApiInfoById(Long id);

    /**
     * 保存API信息
     */
    boolean saveApiInfo(ApiInfo apiInfo);

    /**
     * 更新API信息
     */
    boolean updateApiInfo(ApiInfo apiInfo);

    /**
     * 删除API信息
     */
    boolean deleteApiInfo(Long id);

    /**
     * 根据ID上线或下线API
     */
    boolean toggleApiStatus(Long id, Integer status);

    /**
     * 根据路径和方法查找API
     */
    ApiInfo findByApiPathAndMethod(String apiPath, String apiMethod);
}