package com.newx.ezapi.api.datasource.controller;

import com.newx.ezapi.api.datasource.entity.ApiInfo;
import com.newx.ezapi.api.datasource.entity.ApiParameter;
import com.newx.ezapi.api.datasource.service.ApiInfoService;
import com.newx.ezapi.api.datasource.service.ApiParameterService;
import com.newx.ezapi.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/management")
public class ApiManagementController {

    @Autowired
    private ApiInfoService apiInfoService;

    @Autowired
    private ApiParameterService apiParameterService;

    /**
     * 获取所有API信息
     */
    @GetMapping("/apis")
    public Result<List<ApiInfo>> getAllApis() {
        try {
            List<ApiInfo> apis = apiInfoService.getAllApiInfos();
            return Result.success(apis);
        } catch (Exception e) {
            return Result.error("获取API列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个API信息
     */
    @GetMapping("/api/{id}")
    public Result<ApiInfo> getApiById(@PathVariable Long id) {
        try {
            ApiInfo apiInfo = apiInfoService.getApiInfoById(id);
            if (apiInfo != null) {
                return Result.success(apiInfo);
            } else {
                return Result.error(404, "API不存在");
            }
        } catch (Exception e) {
            return Result.error("获取API信息失败: " + e.getMessage());
        }
    }

    /**
     * 创建或更新API
     */
    @PostMapping("/api")
    public Result<String> saveApi(@RequestBody ApiInfo apiInfo) {
        try {
            // 保存API基本信息
            boolean result = false;
            if(apiInfo.getId() == null) {
                result = apiInfoService.saveApiInfo(apiInfo);
            } else {
                result = apiInfoService.updateApiInfo(apiInfo);
            }
            if (!result) {
                return Result.error("保存API失败");
            }

            // 保存API参数
            if (apiInfo.getParameters() != null && !apiInfo.getParameters().isEmpty()) {
                // 先删除现有的参数
                apiParameterService.deleteParametersByApiId(apiInfo.getId());
                
                // 保存新的参数
                for (ApiParameter param : apiInfo.getParameters()) {
                    param.setApiId(apiInfo.getId()); // 确保关联到正确的API
                    apiParameterService.saveApiParameter(param);
                }
            }

            return Result.success("API保存成功", "API保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存API失败: " + e.getMessage());
        }
    }

    /**
     * 删除API
     */
    @DeleteMapping("/api/{id}")
    public Result<String> deleteApi(@PathVariable Long id) {
        try {
            // 先删除关联的参数
            apiParameterService.deleteParametersByApiId(id);
            
            // 再删除API本身
            boolean result = apiInfoService.deleteApiInfo(id);
            if (result) {
                return Result.success("API删除成功", "API删除成功");
            } else {
                return Result.error("API删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("API删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取API的参数
     */
    @GetMapping("/api/{id}/parameters")
    public Result<List<ApiParameter>> getApiParameters(@PathVariable Long id) {
        try {
            List<ApiParameter> parameters = apiParameterService.getParametersByApiId(id);
            return Result.success(parameters);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取API参数失败: " + e.getMessage());
        }
    }

    /**
     * 更新API状态
     */
    @PutMapping("/api/{id}/status")
    public Result<String> updateApiStatus(@PathVariable Long id, @RequestBody StatusRequest statusRequest) {
        try {
            ApiInfo apiInfo = apiInfoService.getApiInfoById(id);
            if (apiInfo != null) {
                apiInfo.setStatus(statusRequest.getStatus());
                boolean result = apiInfoService.updateApiInfo(apiInfo);
                if (result) {
                    return Result.success("API状态更新成功", "API状态更新成功");
                } else {
                    return Result.error("API状态更新失败");
                }
            } else {
                return Result.error(404, "API不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("API状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 用于接收状态更新请求的内部类
     */
    private static class StatusRequest {
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}