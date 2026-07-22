package com.newx.ezapi.common.wrapper;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 可缓存请求体的请求包装器
 */
public class ContentCachingRequestWrapper extends HttpServletRequestWrapper {
    
    private byte[] content;
    private boolean cached;
    
    public ContentCachingRequestWrapper(HttpServletRequest request) {
        super(request);
    }
    
    /**
     * 获取请求体内容
     */
    public byte[] getContentAsByteArray() {
        if (!cached) {
            cacheContent();
        }
        return content != null ? content : new byte[0];
    }
    
    /**
     * 缓存请求体内容
     */
    private void cacheContent() {
        cached = true;
        try {
            InputStream inputStream = super.getInputStream();
            if (inputStream == null) {
                return;
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            content = outputStream.toByteArray();
        } catch (IOException e) {
            content = new byte[0];
        }
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!cached) {
            cacheContent();
        }
        
        return new ServletInputStream() {
            private int index = 0;
            
            @Override
            public boolean isFinished() {
                return index >= content.length;
            }
            
            @Override
            public boolean isReady() {
                return true;
            }
            
            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public int read() throws IOException {
                if (index >= content.length) {
                    return -1;
                }
                return content[index++] & 0xff;
            }
        };
    }
    
    @Override
    public BufferedReader getReader() throws IOException {
        if (!cached) {
            cacheContent();
        }
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }
}
