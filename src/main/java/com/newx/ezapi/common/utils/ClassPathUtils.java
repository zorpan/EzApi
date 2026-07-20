package com.newx.ezapi.common.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 类路径工具类，用于从外部 JAR 文件创建 ClassLoader。
 * 主要服务于运行时动态加载 JDBC 驱动场景。
 */
public class ClassPathUtils {

    private ClassPathUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 为指定 JAR 文件创建 URLClassLoader。
     *
     * @param jarPath JAR 文件绝对路径
     * @return 可加载该 JAR 中类的 ClassLoader
     * @throws IllegalArgumentException JAR 文件不存在或路径非法
     */
    public static ClassLoader createClassLoader(String jarPath) {
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            throw new IllegalArgumentException("JAR file does not exist: " + jarPath);
        }

        URL jarUrl;
        try {
            jarUrl = jarFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid JAR path: " + jarPath, e);
        }

        return new URLClassLoader(new URL[]{jarUrl}, Thread.currentThread().getContextClassLoader());
    }
}
