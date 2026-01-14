package com.newx.ezapi.api.datasource.controller;

import com.newx.ezapi.api.datasource.entity.ApiInfo;
import com.newx.ezapi.api.datasource.entity.ApiParameter;
import com.newx.ezapi.api.datasource.service.ApiInfoService;
import com.newx.ezapi.api.datasource.service.ApiParameterService;
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
    public List<ApiInfo> getAllApis() {
        return apiInfoService.getAllApiInfos();
    }

    /**
     * 获取单个API信息
     */
    @GetMapping("/api/{id}")
    public ResponseEntity<ApiInfo> getApiById(@PathVariable Long id) {
        ApiInfo apiInfo = apiInfoService.getApiInfoById(id);
        if (apiInfo != null) {
            return ResponseEntity.ok(apiInfo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建或更新API
     */
    @PostMapping("/api")
    public ResponseEntity<String> saveApi(@RequestBody ApiInfo apiInfo) {
        try {
            // 保存API基本信息
            boolean result = false;
            if(apiInfo.getId() == null) {
                result = apiInfoService.saveApiInfo(apiInfo);
            } else {
                result = apiInfoService.updateApiInfo(apiInfo);
            }
            if (!result) {
                return ResponseEntity.badRequest().body("保存API失败");
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

            return ResponseEntity.ok("API保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("保存API失败: " + e.getMessage());
        }
    }

    /**
     * 删除API
     */
    @DeleteMapping("/api/{id}")
    public ResponseEntity<String> deleteApi(@PathVariable Long id) {
        try {
            // 先删除关联的参数
            apiParameterService.deleteParametersByApiId(id);
            
            // 再删除API本身
            boolean result = apiInfoService.deleteApiInfo(id);
            if (result) {
                return ResponseEntity.ok("API删除成功");
            } else {
                return ResponseEntity.badRequest().body("API删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("API删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取API的参数
     */
    @GetMapping("/api/{id}/parameters")
    public ResponseEntity<List<ApiParameter>> getApiParameters(@PathVariable Long id) {
        try {
            List<ApiParameter> parameters = apiParameterService.getParametersByApiId(id);
            return ResponseEntity.ok(parameters);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新API状态
     */
    @PutMapping("/api/{id}/status")
    public ResponseEntity<String> updateApiStatus(@PathVariable Long id, @RequestBody StatusRequest statusRequest) {
        try {
            ApiInfo apiInfo = apiInfoService.getApiInfoById(id);
            if (apiInfo != null) {
                apiInfo.setStatus(statusRequest.getStatus());
                boolean result = apiInfoService.updateApiInfo(apiInfo);
                if (result) {
                    return ResponseEntity.ok("API状态更新成功");
                } else {
                    return ResponseEntity.badRequest().body("API状态更新失败");
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("API状态更新失败: " + e.getMessage());
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