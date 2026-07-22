package com.newx.ezapi.core.service.impl;

import com.newx.ezapi.core.entity.ApiInfo;
import com.newx.ezapi.core.entity.enums.ProtocolType;
import com.newx.ezapi.core.service.ApiExecutor;
import com.newx.ezapi.core.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * WebService SOAP 协议执行器
 * <p>
 * 将 HTTP 请求转换为 SOAP/XML 调用，并将 XML 响应自动转 JSON 返回。
 * 当 ApiInfo.protocolType = 'WS' 时使用此执行器。
 * </p>
 *
 * <h3>SOAP 模板示例：</h3>
 * <pre>
 * &lt;soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"&gt;
 *   &lt;soap:Body&gt;
 *     &lt;GetUserInfo xmlns="http://example.com/"&gt;
 *       &lt;userId&gt;#{userId}&lt;/userId&gt;
 *     &lt;/GetUserInfo&gt;
 *   &lt;/soap:Body&gt;
 * &lt;/soap:Envelope&gt;
 * </pre>
 *
 * <h3>参数占位符：</h3>
 * 模板中使用 <code>#{paramName}</code> 语法引用 API 参数，
 * 系统会自动替换为实际传入的参数值。
 */
@Component
public class WsApiExecutor implements ApiExecutor {

    private static final Logger log = LoggerFactory.getLogger(WsApiExecutor.class);

    /** SOAP 请求默认超时时间（毫秒） */
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 30000;

    /** 参数占位符前缀 */
    private static final String PLACEHOLDER_PREFIX = "#{";
    /** 参数占位符后缀 */
    private static final String PLACEHOLDER_SUFFIX = "}";

    @Override
    public Object execute(ApiInfo apiInfo, Map<String, Object> parameters) throws Exception {
        String wsdlUrl = apiInfo.getWsdlUrl();
        String soapBodyTemplate = apiInfo.getSoapBodyTemplate();
        String soapAction = apiInfo.getSoapAction();

        if (wsdlUrl == null || wsdlUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("WebService WSDL 地址未配置");
        }
        if (soapBodyTemplate == null || soapBodyTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("SOAP 请求体模板未配置");
        }

        // 1. 替换模板中的参数占位符
        String requestXml = replacePlaceholders(soapBodyTemplate, parameters);

        // 2. 如果模板没有包含完整的 SOAP Envelope，自动包裹
        if (!requestXml.trim().startsWith("<soap:Envelope")
                && !requestXml.trim().startsWith("<SOAP-ENV:Envelope")) {
            requestXml = buildSoapEnvelope(requestXml);
        }

        log.debug("WebService 请求 URL: {}", wsdlUrl);
        log.debug("WebService 请求 XML: {}", requestXml);

        // 3. 发送 SOAP 请求
        String responseXml = sendSoapRequest(wsdlUrl, requestXml, soapAction);

        log.debug("WebService 响应 XML: {}", responseXml);

        // 4. 将 XML 响应转换为 JSON（Map 结构）
        return XmlUtils.xmlToMap(responseXml);
    }

    @Override
    public String getProtocolType() {
        return ProtocolType.WS.getCode();
    }

    /**
     * 替换模板中的 #{paramName} 占位符
     *
     * @param template   SOAP 模板
     * @param parameters 参数 Map
     * @return 替换后的 XML
     */
    private String replacePlaceholders(String template, Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = PLACEHOLDER_PREFIX + entry.getKey() + PLACEHOLDER_SUFFIX;
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            // XML 转义
            value = escapeXml(value);
            result = result.replace(placeholder, value);
        }
        return result;
    }

    /**
     * 构建完整的 SOAP Envelope
     *
     * @param soapBody SOAP Body 内容
     * @return 完整的 SOAP XML
     */
    private String buildSoapEnvelope(String soapBody) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap:Header/>"
                + "<soap:Body>"
                + soapBody
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    /**
     * 发送 SOAP 请求到 WebService 端点
     *
     * @param targetUrl  WebService 地址
     * @param soapXml    完整的 SOAP XML
     * @param soapAction SOAPAction 头（可选）
     * @return 响应的 XML 字符串
     * @throws Exception 请求异常
     */
    private String sendSoapRequest(String targetUrl, String soapXml, String soapAction) throws Exception {
        URL url = new URL(targetUrl);
        URLConnection connection = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) connection;

        httpConn.setRequestMethod("POST");
        httpConn.setConnectTimeout(CONNECT_TIMEOUT);
        httpConn.setReadTimeout(READ_TIMEOUT);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setUseCaches(false);

        // 设置 SOAP 请求头
        httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        httpConn.setRequestProperty("Accept", "text/xml, application/xml");

        if (soapAction != null && !soapAction.trim().isEmpty()) {
            httpConn.setRequestProperty("SOAPAction", soapAction);
        } else {
            httpConn.setRequestProperty("SOAPAction", "");
        }

        // 发送请求
        byte[] requestBytes = soapXml.getBytes(StandardCharsets.UTF_8);
        httpConn.setRequestProperty("Content-Length", String.valueOf(requestBytes.length));

        try (OutputStream os = httpConn.getOutputStream()) {
            os.write(requestBytes);
            os.flush();
        }

        // 读取响应
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK
                || responseCode == HttpURLConnection.HTTP_ACCEPTED
                || responseCode == 202) {
            try (java.util.Scanner scanner = new java.util.Scanner(
                    httpConn.getInputStream(), StandardCharsets.UTF_8.name())) {
                scanner.useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            }
        } else {
            // 读取错误流
            String errorBody;
            try (java.util.Scanner scanner = new java.util.Scanner(
                    httpConn.getErrorStream(), StandardCharsets.UTF_8.name())) {
                scanner.useDelimiter("\\A");
                errorBody = scanner.hasNext() ? scanner.next() : "";
            }
            throw new RuntimeException("WebService 调用失败，HTTP " + responseCode
                    + ", 响应: " + errorBody);
        }
    }

    /**
     * XML 转义，防止特殊字符破坏 XML 结构
     */
    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
