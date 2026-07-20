package com.newx.ezapi.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * API信息实体类
 */
@Data
@TableName("api_info")
public class ApiInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("api_name")
    private String apiName; // API名称

    @TableField("api_path")
    private String apiPath; // API路径

    @TableField("api_method")
    private String apiMethod; // 请求方法 GET/POST/PUT/DELETE

    @TableField("data_source_id")
    private String dataSourceId; // 数据源ID

    @TableField("sql_content")
    private String sqlContent; // SQL内容

    @TableField("description")
    private String description; // 描述

    @TableField("status")
    private Integer status; // 状态 0-下线 1-上线

    @TableField("protocol_type")
    private String protocolType; // 协议类型：SQL / WS / HTTP_PROXY 等

    @TableField("ws_wsdl_url")
    private String wsdlUrl; // WebService WSDL 地址

    @TableField("ws_soap_body_template")
    private String soapBodyTemplate; // SOAP 请求体模板

    @TableField("ws_soap_action")
    private String soapAction; // SOAPAction HTTP 头

    @TableField("created_time")
    private Long createdTime;

    @TableField("updated_time")
    private Long updatedTime;
    
    @TableField(exist = false)  // 不映射到数据库字段
    private List<ApiParameter> parameters;  // 关联的参数列表

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(String apiMethod) {
        this.apiMethod = apiMethod;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getSqlContent() {
        return sqlContent;
    }

    public void setSqlContent(String sqlContent) {
        this.sqlContent = sqlContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getSoapBodyTemplate() {
        return soapBodyTemplate;
    }

    public void setSoapBodyTemplate(String soapBodyTemplate) {
        this.soapBodyTemplate = soapBodyTemplate;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public List<ApiParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ApiParameter> parameters) {
        this.parameters = parameters;
    }
}